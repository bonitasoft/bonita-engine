/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine.platform;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bonitasoft.engine.EngineInitializer;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.TaskResult;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.transaction.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 */
@Component
public class PlatformVersionChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(EngineInitializer.class);

    private final PlatformService platformService;
    private final BroadcastService broadcastService;
    private TransactionService transactionService;

    private String errorMessage;

    public PlatformVersionChecker(final PlatformService platformService, BroadcastService broadcastService,
            TransactionService transactionService) {
        this.platformService = platformService;
        this.broadcastService = broadcastService;
        this.transactionService = transactionService;
    }

    public boolean verifyPlatformVersion() throws Exception {
        return transactionService.executeInTransaction(this::execute);
    }

    boolean execute() throws SBonitaException {
        // the database  schema version:
        String databaseSchemaVersion = platformService.getPlatform().getDbSchemaVersion();
        // the version in jars
        final String platformVersionFromBinaries = platformService.getSPlatformProperties().getPlatformVersion();
        String supportedDatabaseSchemaVersion = extractMinorVersion(platformVersionFromBinaries);
        LOGGER.info("Bonita platform version (binaries): {}", platformVersionFromBinaries);
        LOGGER.info("Bonita database schema version: {}", databaseSchemaVersion);

        boolean isDatabaseSchemaSupported = databaseSchemaVersion.equals(supportedDatabaseSchemaVersion);
        if (!isDatabaseSchemaSupported) {
            errorMessage = MessageFormat.format("The version of the platform in database is not the same as expected:" +
                    " Supported database schema version is <{0}> and current database schema version is <{1}>",
                    supportedDatabaseSchemaVersion, databaseSchemaVersion);
            return false;
        }

        final Optional<String> versionFromOtherNodes = getVersionFromOtherNodes();
        if (versionFromOtherNodes.isPresent() && !platformVersionFromBinaries.equals(versionFromOtherNodes.get())) {
            errorMessage = MessageFormat.format(
                    "Node cannot be started as it is in version {0} whereas other nodes are in version {1}\n"
                            + "All nodes in the same cluster must execute the same Bonita runtime version",
                    platformVersionFromBinaries, versionFromOtherNodes.get());
            LOGGER.error(errorMessage);
            return false;
        }

        return true;
    }

    /**
     * Search for the first version found amongst other nodes.
     *
     * @return the first version encountered, if there are other nodes,
     *         or empty optional if there are no other nodes in the cluster,
     *         or throw exception if no version can be retrieved.
     */
    protected Optional<String> getVersionFromOtherNodes() {
        try {
            final Map<String, TaskResult<String>> nodeToVersionMap = broadcastService
                    .executeOnOthers(new GetPlatformVersionFromNode()).get();
            if (nodeToVersionMap.isEmpty()) {
                // There are no other nodes in the cluster, so ok:
                return Optional.empty();
            }
            for (Map.Entry<String, TaskResult<String>> nodeToVersion : nodeToVersionMap.entrySet()) {
                final TaskResult<String> result = nodeToVersion.getValue();
                if (result.isOk()) {
                    LOGGER.info("Found that Bonita node '{}' is in version {}", nodeToVersion.getKey(),
                            result.getResult());
                    return Optional.of(result.getResult());
                }
                // otherwise we check the next node, until finding a valid version
            }
        } catch (InterruptedException | ExecutionException ignored) {
        }
        final String msg = "Cannot access other node version in the cluster";
        LOGGER.error(msg);
        throw new RuntimeException(msg);
    }

    /**
     * This method is duplicate in class VersionServiceImpl.
     * This is accepted to limit over-engineering just to extract an util method.
     */
    private String extractMinorVersion(String version) {
        final Matcher matcher = Pattern.compile("(\\d+\\.\\d+).*").matcher(version);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException(version + " does not respect Semantic Versioning");
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    static class GetPlatformVersionFromNode implements Callable<String>, Serializable {

        @Override
        public String call() throws Exception {
            return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor()
                    .getPlatformService().getSPlatformProperties().getPlatformVersion();
        }
    }
}
