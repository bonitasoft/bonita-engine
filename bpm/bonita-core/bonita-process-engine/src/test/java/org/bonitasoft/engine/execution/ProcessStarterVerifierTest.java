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

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.bonitasoft.engine.execution.ProcessStarterVerifierImpl.LIMIT;
import static org.bonitasoft.engine.execution.ProcessStarterVerifierImpl.PERIOD_IN_MILLIS;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.platform.PlatformRetriever;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.service.platform.PlatformInformationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessStarterVerifierTest {

    @Mock
    private PlatformRetriever platformRetriever;

    @Mock
    private PlatformInformationService platformInformationService;

    ProcessStarterVerifierImpl processStarterVerifier;

    @BeforeEach
    void setUp() {
        processStarterVerifier = Mockito
                .spy(new ProcessStarterVerifierImpl(platformRetriever, platformInformationService));
    }

    @Test
    void should_be_able_to_decrypt_encrypted_value() throws Exception {
        //given
        final long currentTime = System.currentTimeMillis();
        final var originalValue = List.of(currentTime, currentTime + 1000L, currentTime + 2000L);

        //when
        final List<Long> computedValue = processStarterVerifier
                .decryptDataFromDatabase(processStarterVerifier.encryptDataBeforeSendingToDatabase(originalValue));

        //then
        assertThat(computedValue).isEqualTo(originalValue);
    }

    @Test
    void verify_should_not_remove_still_valid_values_from_counters() throws Exception {
        //given
        final long validTimestamp = System.currentTimeMillis() - PERIOD_IN_MILLIS + 86400000L; // plus 1 day
        processStarterVerifier.addCounter(validTimestamp);
        doNothing().when(processStarterVerifier).storeNewValueInDatabase(anyString());

        //when
        processStarterVerifier.verify(new SProcessInstance());

        //then
        assertThat(processStarterVerifier.getCounters()).size().isEqualTo(2);
        assertThat(processStarterVerifier.getCounters()).contains(validTimestamp);
    }

    @Test
    void verify_should_remove_old_values_from_counters() throws Exception {
        //given
        final long obsoleteValue = System.currentTimeMillis() - PERIOD_IN_MILLIS - 86400000L; // minus 1 day
        processStarterVerifier.addCounter(obsoleteValue);
        doNothing().when(processStarterVerifier).storeNewValueInDatabase(anyString());

        //when
        processStarterVerifier.verify(new SProcessInstance());

        //then
        assertThat(processStarterVerifier.getCounters()).size().isEqualTo(1);
        assertThat(processStarterVerifier.getCounters()).doesNotContain(obsoleteValue);
    }

    @Test
    void verify_should_throw_exception_if_limit_is_reached() throws Exception {
        //given
        for (int i = 0; i < LIMIT; i++) {
            processStarterVerifier.addCounter(System.currentTimeMillis());
        }

        //when - then
        assertThatExceptionOfType(SProcessInstanceCreationException.class)
                .isThrownBy(() -> processStarterVerifier.verify(new SProcessInstance()))
                .withMessageContaining("Process start limit");
        assertThat(processStarterVerifier.getCounters()).size().isEqualTo(LIMIT);
        verify(processStarterVerifier, never()).storeNewValueInDatabase(anyString());
    }

    @Test
    void should_log_when_80_percent_is_reached() throws Exception {
        var counters = mock(List.class);
        doReturn(LIMIT * 80 / 100).when(counters).size();
        doReturn(counters).when(processStarterVerifier).getCounters();

        // when
        final String log = tapSystemOut(() -> processStarterVerifier.logCaseLimitProgressIfThresholdReached());

        // then
        assertThat(log).containsPattern("WARN.*80%");
    }

    @Test
    void should_log_when_90_percent_is_reached() throws Exception {
        var counters = mock(List.class);
        doReturn(LIMIT * 90 / 100).when(counters).size();
        doReturn(counters).when(processStarterVerifier).getCounters();

        // when
        final String log = tapSystemOut(() -> processStarterVerifier.logCaseLimitProgressIfThresholdReached());

        // then
        assertThat(log)
                .containsPattern("WARN.*90%")
                .doesNotContain("80%");
    }

    @Test
    void should_not_log_when_80_percent_has_already_been_reached() throws Exception {
        var counters = mock(List.class);
        doReturn(121).when(counters).size();
        doReturn(counters).when(processStarterVerifier).getCounters();

        // when
        final String log = tapSystemOut(() -> processStarterVerifier.logCaseLimitProgressIfThresholdReached());

        // then
        assertThat(log).isBlank();
    }

    @Test
    void readCounters_should_fail_if_counters_are_not_set() throws Exception {
        // given
        final SPlatform platform = new SPlatform();
        platform.setInformation("");
        doReturn(platform).when(platformRetriever).getPlatform();

        // when - then
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> processStarterVerifier.readCounters());
    }

    @Test
    void readCounters_should_fail_if_counters_cannot_be_decrypted_from_database() throws Exception {
        // given
        final SPlatform platform = new SPlatform();
        final String encryptedValue = "encrypted value";
        platform.setInformation(encryptedValue);
        doReturn(platform).when(platformRetriever).getPlatform();
        doThrow(new IOException("Cannot decipher information")).when(processStarterVerifier)
                .decryptDataFromDatabase(encryptedValue);

        // when - then
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> processStarterVerifier.readCounters());
    }

    @Test
    void readCounters_should_succeed_if_counters_can_be_decrypted_from_database() throws Exception {
        // given
        final SPlatform platform = new SPlatform();
        final String encryptedValue = "encrypted value";
        platform.setInformation(encryptedValue);
        doReturn(platform).when(platformRetriever).getPlatform();
        final List<Long> countersFromDatabase = List.of(System.currentTimeMillis());
        doReturn(countersFromDatabase).when(processStarterVerifier).decryptDataFromDatabase(encryptedValue);

        // when
        final List<Long> counters = processStarterVerifier.readCounters();

        // then
        assertThat(counters).isEqualTo(countersFromDatabase);
    }
}
