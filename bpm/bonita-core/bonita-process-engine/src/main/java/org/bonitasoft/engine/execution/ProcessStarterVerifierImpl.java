/**
 * Copyright (C) 2024 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.execution;

import static java.lang.String.format;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.platform.PlatformRetriever;
import org.bonitasoft.engine.platform.exception.SPlatformNotFoundException;
import org.bonitasoft.engine.platform.exception.SPlatformUpdateException;
import org.bonitasoft.engine.service.platform.PlatformInformationService;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.platform.setup.SimpleEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnSingleCandidate(ProcessStarterVerifier.class)
@Slf4j
public class ProcessStarterVerifierImpl implements ProcessStarterVerifier {

    public static final int LIMIT = 150;
    protected static final int PERIOD_IN_DAYS = 30;
    protected static final long PERIOD_IN_MILLIS = PERIOD_IN_DAYS * 24L * 60L * 60L * 1000L;
    protected static final List<Integer> THRESHOLDS_IN_PERCENT = List.of(80, 90);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final PlatformRetriever platformRetriever;
    private final PlatformInformationService platformInformationService;

    private final List<Long> counters = Collections.synchronizedList(new ArrayList<>());

    ProcessStarterVerifierImpl(PlatformRetriever platformRetriever,
            PlatformInformationService platformInformationService) {
        this.platformRetriever = platformRetriever;
        this.platformInformationService = platformInformationService;
    }

    @Autowired
    ProcessStarterVerifierImpl(PlatformRetriever platformRetriever,
            PlatformInformationService platformInformationService,
            TransactionService transactionService) throws Exception {
        this(platformRetriever, platformInformationService);
        counters.addAll(transactionService.executeInTransaction(this::readCounters));
    }

    protected List<Long> getCounters() {
        return Collections.unmodifiableList(counters);
    }

    protected void addCounter(long counter) {
        synchronized (counters) {
            counters.add(counter);
        }
    }

    @Override
    public void verify(SProcessInstance processInstance) throws SProcessInstanceCreationException {
        log.debug("Verifying process instance {}", processInstance.getId());
        cleanupOldValues();
        log.debug("Found {} cases already started in the last {} days", counters.size(), PERIOD_IN_DAYS);
        if (counters.size() >= LIMIT) {
            final String nextValidTime = getStringRepresentation(getNextResetTimestamp(counters));
            throw new SProcessInstanceCreationException(
                    format("Process start limit (%s cases during last %s days) reached. You are not allowed to start a new process until %s.",
                            LIMIT, PERIOD_IN_DAYS, nextValidTime));
        }
        try {
            synchronized (counters) {
                counters.add(System.currentTimeMillis());
            }
            final String information = encryptDataBeforeSendingToDatabase(counters);
            // store in database:
            storeNewValueInDatabase(information);
            logCaseLimitProgressIfThresholdReached();
        } catch (IOException | SPlatformNotFoundException | SPlatformUpdateException e) {
            log.trace(e.getMessage(), e);
            throw new SProcessInstanceCreationException(
                    format("Unable to start the process instance %s", processInstance.getId()));
        }
    }

    void cleanupOldValues() {
        log.trace("Cleaning up old values for the last {} days", PERIOD_IN_DAYS);
        final long oldestValidTimestamp = System.currentTimeMillis() - PERIOD_IN_MILLIS;
        synchronized (counters) {
            counters.removeIf(timestamp -> timestamp < oldestValidTimestamp);
        }
    }

    void storeNewValueInDatabase(String information) throws SPlatformUpdateException, SPlatformNotFoundException {
        platformInformationService.updatePlatformInfo(platformRetriever.getPlatform(), information);
    }

    List<Long> readCounters() {
        try {
            String information = platformRetriever.getPlatform().getInformation();
            if (information == null || information.isBlank()) {
                throw new IllegalStateException("Invalid database. Please reset it and restart.");
            }
            return decryptDataFromDatabase(information);
        } catch (SPlatformNotFoundException | IOException e) {
            throw new IllegalStateException("Cannot read from database table 'platform'", e);
        }
    }

    String encryptDataBeforeSendingToDatabase(List<Long> counters) throws IOException {
        return encrypt(OBJECT_MAPPER.writeValueAsBytes(counters));
    }

    List<Long> decryptDataFromDatabase(String information) throws IOException {
        return OBJECT_MAPPER.readValue(decrypt(information), new TypeReference<>() {
        });
    }

    private static String encrypt(byte[] data) {
        try {
            return SimpleEncryptor.encrypt(data);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot cipher information", e);
        }
    }

    private static byte[] decrypt(String information) {
        try {
            return SimpleEncryptor.decrypt(information);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot decipher information", e);
        }
    }

    void logCaseLimitProgressIfThresholdReached() {
        var percentBeforeThisNewCase = (float) ((getCounters().size() - 1) * 100) / LIMIT;
        var percentWithThisNewCase = (float) ((getCounters().size()) * 100) / LIMIT;
        for (Integer threshold : THRESHOLDS_IN_PERCENT) {
            if (percentBeforeThisNewCase < threshold && percentWithThisNewCase >= threshold) {
                log.warn("You have started {}% of your allowed cases."
                        + "If you need more volume, please consider subscribing to an Enterprise edition.",
                        threshold);
            }
        }
    }

    /**
     * Returns a timestamp to a human-readable format
     */
    private String getStringRepresentation(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
    }

    private long getNextResetTimestamp(List<Long> timestamps) {
        return Collections.min(timestamps) + PERIOD_IN_MILLIS;
    }

}
