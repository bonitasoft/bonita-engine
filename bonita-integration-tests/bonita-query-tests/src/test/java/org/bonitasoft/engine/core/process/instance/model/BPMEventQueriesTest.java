/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import static org.bonitasoft.engine.test.persistence.builder.MessageInstanceBuilder.aMessageInstance;
import static org.bonitasoft.engine.test.persistence.builder.WaitingMessageEventBuilder.aWaitingEvent;

import java.util.List;

import javax.inject.Inject;

import org.bonitasoft.engine.test.persistence.repository.BPMEventRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class BPMEventQueriesTest {

    private static final int SOME_NUMBER_OF_MESSAGE_INSTANCES = 5;

    private static final int MORE_THAN_DEFAULT_PAGE_SIZE = 42;

    @Inject
    private BPMEventRepository bPMEventRepository;

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

}
