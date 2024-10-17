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
package org.bonitasoft.engine.core.process.instance.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType.INTERMEDIATE_THROW_EVENT;
import static org.bonitasoft.engine.test.persistence.builder.MessageInstanceBuilder.aMessageInstance;
import static org.bonitasoft.engine.test.persistence.builder.WaitingMessageEventBuilder.aWaitingEvent;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.test.persistence.repository.BPMEventRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class BPMEventQueriesTest {

    private static final int SOME_NUMBER_OF_MESSAGE_INSTANCES = 5;

    private static final int MORE_THAN_DEFAULT_PAGE_SIZE = 42;

    @Autowired
    private BPMEventRepository bPMEventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void getInProgressMessageInstancesShouldOnlyConsiderHandledMessages() {
        // given:
        for (int i = 0; i < SOME_NUMBER_OF_MESSAGE_INSTANCES; i++) {
            bPMEventRepository.add(aMessageInstance().handled(true).build());
        }
        // Add instances that should be ignored by getInProgressMessageInstances():
        bPMEventRepository.add(aMessageInstance().handled(false).build());
        bPMEventRepository.add(aMessageInstance().handled(false).build());

        // when:
        List<Long> inProgressMessageInstances = bPMEventRepository.getInProgressMessageInstances();

        // then:
        assertThat(inProgressMessageInstances).hasSize(SOME_NUMBER_OF_MESSAGE_INSTANCES);
    }

    @Test
    public void resetMessageInstancesShouldResetAllHandledFlagToFalse() {
        // given:
        for (int i = 0; i < MORE_THAN_DEFAULT_PAGE_SIZE; i++) {
            bPMEventRepository.add(aMessageInstance().handled(true).build());
        }

        // when:
        bPMEventRepository.resetProgressMessageInstances();

        // then:
        assertThat(bPMEventRepository.getInProgressMessageInstances()).hasSize(0);
    }

    @Test
    public void getInProgressWaitingEventsShouldOnlyConsiderInProgressElements() {
        // given:
        for (int i = 0; i < SOME_NUMBER_OF_MESSAGE_INSTANCES; i++) {
            bPMEventRepository.add(aWaitingEvent().inProgress(true).build());
        }
        // Add instances that should be ignored by getInProgressWaitingEvents():
        bPMEventRepository.add(aWaitingEvent().inProgress(false).build());
        bPMEventRepository.add(aWaitingEvent().inProgress(false).build());

        // when:
        List<Long> inProgressWaitingEvents = bPMEventRepository.getInProgressWaitingEvents();

        // then:
        assertThat(inProgressWaitingEvents).hasSize(SOME_NUMBER_OF_MESSAGE_INSTANCES);
    }

    @Test
    public void resetWaitingEventsShouldResetAllInProgressFlagToFalse() {
        // given:
        for (int i = 0; i < MORE_THAN_DEFAULT_PAGE_SIZE; i++) {
            bPMEventRepository.add(aWaitingEvent().inProgress(true).build());
        }

        // when:
        bPMEventRepository.resetInProgressWaitingEvents();

        // then:
        assertThat(bPMEventRepository.getInProgressWaitingEvents()).hasSize(0);
    }

    @Test
    public void resetWaitingEventsShouldReturnNumberOfRowsUpdated() {
        // given:
        for (int i = 0; i < MORE_THAN_DEFAULT_PAGE_SIZE; i++) {
            bPMEventRepository.add(aWaitingEvent().inProgress(true).build());
        }
        bPMEventRepository.add(aWaitingEvent().inProgress(false).build());
        bPMEventRepository.add(aWaitingEvent().inProgress(false).build());

        // when:
        int resetInProgressWaitingEvents = bPMEventRepository.resetInProgressWaitingEvents();

        // then:
        assertThat(resetInProgressWaitingEvents).as("wrong result").isEqualTo(MORE_THAN_DEFAULT_PAGE_SIZE);
    }

    @Test
    public void resetMessageInstancesShouldReturnNumberOfRowsUpdated() {
        // given:
        for (int i = 0; i < MORE_THAN_DEFAULT_PAGE_SIZE; i++) {
            bPMEventRepository.add(aMessageInstance().handled(true).build());
        }
        bPMEventRepository.add(aMessageInstance().handled(false).build());
        bPMEventRepository.add(aMessageInstance().handled(false).build());

        // when:
        int resetMessageInstances = bPMEventRepository.resetProgressMessageInstances();

        // then:
        assertThat(resetMessageInstances).as("wrong result").isEqualTo(MORE_THAN_DEFAULT_PAGE_SIZE);
    }

    @Test
    public void should_return_message_older_than_creationDate() {
        SMessageInstance messageInstance = bPMEventRepository.add(aMessageInstance().creationDate(100).build());
        SMessageInstance messageInstance1 = bPMEventRepository.add(aMessageInstance().creationDate(200).build());
        bPMEventRepository.add(aMessageInstance().creationDate(300).build());

        List<Long> messageInstanceIds = bPMEventRepository.getMessageInstanceIdOlderThanCreationDate(200);

        // then:
        assertThat(messageInstanceIds).containsOnly(messageInstance.getId(), messageInstance1.getId());
    }

    @Test
    public void should_delete_message_with_given_ids() {
        SMessageInstance messageInstance = bPMEventRepository.add(aMessageInstance().creationDate(100).build());
        SMessageInstance messageInstance1 = bPMEventRepository.add(aMessageInstance().creationDate(200).build());
        SMessageInstance messageInstance2 = bPMEventRepository.add(aMessageInstance().creationDate(300).build());

        bPMEventRepository.deleteMessageInstanceByIds(Arrays.asList(messageInstance.getId(), messageInstance1.getId()));

        List<Long> messageInstanceIds = bPMEventRepository
                .getMessageInstanceIdOlderThanCreationDate(Instant.now().toEpochMilli());

        // then:
        assertThat(messageInstanceIds).containsExactly(messageInstance2.getId());
    }

    @Test
    public void should_get_waitingEvents_by_eventType() {
        // Given
        final SWaitingEvent sWaitingEvent = bPMEventRepository
                .add(aWaitingEvent().withEventType(INTERMEDIATE_THROW_EVENT).build());
        bPMEventRepository.flush();
        final String eventType = jdbcTemplate.queryForObject("select eventType from waiting_event", String.class);
        assertThat(eventType).isEqualTo("INTERMEDIATE_THROW_EVENT");
        assertThat(sWaitingEvent.getEventType()).isEqualTo(INTERMEDIATE_THROW_EVENT);
    }

}
