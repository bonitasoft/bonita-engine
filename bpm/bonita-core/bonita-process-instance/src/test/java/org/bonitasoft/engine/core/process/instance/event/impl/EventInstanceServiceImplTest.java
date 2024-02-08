/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.process.instance.event.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl.BONITA_BPMENGINE_MESSAGE_SENT;
import static org.bonitasoft.engine.data.instance.api.DataInstanceContainer.MESSAGE_INSTANCE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceRepository;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventInstanceServiceImplTest {

    @Mock
    private EventInstanceRepository instanceRepository;

    @Mock
    private DataInstanceService dataInstanceService;

    private EventInstanceServiceImpl eventInstanceServiceImpl;

    private MeterRegistry meterRegistry = new SimpleMeterRegistry(
            // So that micrometer updates its counters every 1 ms:
            k -> k.equals("simple.step") ? Duration.ofMillis(1).toString() : null,
            Clock.SYSTEM);

    @Before
    public void setUp() {
        eventInstanceServiceImpl = new EventInstanceServiceImpl(instanceRepository, dataInstanceService, meterRegistry,
                1L);
    }

    @Test
    public final void deleteMessageAndDataInstanceOlderCreationDate_should_call_expected_method_and_return_nbMessage_deleted()
            throws Exception {

        // Given
        long creationDate = Instant.now().toEpochMilli();

        List<Long> idsToBeDeleted = Arrays.asList(1L, 2L);
        when(instanceRepository.getMessageInstanceIdOlderThanCreationDate(eq(creationDate), any()))
                .thenReturn(idsToBeDeleted);

        Integer totalRemoved = eventInstanceServiceImpl.deleteMessageAndDataInstanceOlderThanCreationDate(creationDate,
                QueryOptions.countQueryOptions());

        assertThat(totalRemoved).isEqualTo(idsToBeDeleted.size());
        verify(instanceRepository).deleteMessageInstanceByIds(idsToBeDeleted);

        verify(dataInstanceService).deleteLocalDataInstances(1L, MESSAGE_INSTANCE.name(), true);
        verify(dataInstanceService).deleteLocalDataInstances(2L, MESSAGE_INSTANCE.name(), true);
        verify(dataInstanceService).deleteLocalArchivedDataInstances(1L,
                MESSAGE_INSTANCE.name());
        verify(dataInstanceService).deleteLocalArchivedDataInstances(2L,
                MESSAGE_INSTANCE.name());
    }

    @Test
    public final void should_count_message_thrown() throws SMessageInstanceCreationException {

        eventInstanceServiceImpl.createMessageInstance(new SMessageInstance());
        eventInstanceServiceImpl.createMessageInstance(new SMessageInstance());

        assertThat(meterRegistry.find(BONITA_BPMENGINE_MESSAGE_SENT).tag("tenant", "1").counter().count()).isEqualTo(2);
    }
}
