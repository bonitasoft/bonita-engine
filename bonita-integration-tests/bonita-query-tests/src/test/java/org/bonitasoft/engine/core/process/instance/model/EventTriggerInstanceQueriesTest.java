/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.core.process.instance.model;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.inject.Inject;

import org.bonitasoft.engine.core.process.instance.model.event.impl.SIntermediateCatchEventInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.SThrowErrorEventTriggerInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.STimerEventTriggerInstanceImpl;
import org.bonitasoft.engine.test.persistence.builder.PersistentObjectBuilder;
import org.bonitasoft.engine.test.persistence.repository.ProcessInstanceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class EventTriggerInstanceQueriesTest {

    @Inject
    private ProcessInstanceRepository repository;

    @Test
    public void getNumberOfTimerEventTriggerInstances_should_return_number_of_event_trigger_instance_attached_to_the_process_instance() {
        // Given
        final long processInstanceId = 9634L;

        final SIntermediateCatchEventInstanceImpl eventInstanceImpl = (SIntermediateCatchEventInstanceImpl) repository
                .add(buildSIntermediateCatchEventInstanceImpl(101L, "name", processInstanceId));

        repository.add(buildSTimerEventTriggerInstance(87L, eventInstanceImpl.getId(), "name", "jobTriggerName"));
        repository.add(buildSThrowErrorEventTriggerInstanceImpl(89L, eventInstanceImpl.getId()));

        // Then
        final long numberOfEventTriggerInstances = repository.getNumberOfTimerEventTriggerInstances(processInstanceId, null);

        // When
        assertEquals(2, numberOfEventTriggerInstances);
    }

    @Test
    public void getNumberOfTimerEventTriggerInstances_should_return_number_of_event_trigger_instance_attached_to_the_process_instance_filter_by_event_instance_name() {
        // Given
        final long processInstanceId = 9634L;

        final SIntermediateCatchEventInstanceImpl eventInstanceImpl = (SIntermediateCatchEventInstanceImpl) repository
                .add(buildSIntermediateCatchEventInstanceImpl(101L, "name", processInstanceId));
        repository.add(buildSTimerEventTriggerInstance(96L, eventInstanceImpl.getId(), "name", "jobTriggerName"));

        final SIntermediateCatchEventInstanceImpl eventInstanceImpl2 = (SIntermediateCatchEventInstanceImpl) repository
                .add(buildSIntermediateCatchEventInstanceImpl(104L, "toto", processInstanceId));
        repository.add(buildSTimerEventTriggerInstance(98L, eventInstanceImpl2.getId(), "toto", "plop"));

        // Then
        final long numberOfEventTriggerInstances = repository.getNumberOfTimerEventTriggerInstances(processInstanceId, "name");

        // When
        assertEquals(1, numberOfEventTriggerInstances);
    }

    @Test
    public void searchTimerEventTriggerInstances_should_return_event_trigger_instance_attached_to_the_process_instance() {
        // Given
        final long processInstanceId = 9634L;

        final SIntermediateCatchEventInstanceImpl eventInstanceImpl = (SIntermediateCatchEventInstanceImpl) repository
                .add(buildSIntermediateCatchEventInstanceImpl(101L, "name", processInstanceId));

        repository.add(buildSTimerEventTriggerInstance(87L, eventInstanceImpl.getId(), "name", "jobTriggerName"));
        repository.add(buildSThrowErrorEventTriggerInstanceImpl(89L, eventInstanceImpl.getId()));

        // Then
        final List<SEventTriggerInstance> sEventTriggerInstances = repository.searchTimerEventTriggerInstances(processInstanceId, null);

        // When
        assertEquals(2, sEventTriggerInstances.size());
    }

    @Test
    public void searchTimerEventTriggerInstances_should_should_return_event_trigger_instance_attached_to_the_process_instance_filter_by_event_instance_name() {
        // Given
        final long processInstanceId = 9634L;

        final SIntermediateCatchEventInstanceImpl eventInstanceImpl = (SIntermediateCatchEventInstanceImpl) repository
                .add(buildSIntermediateCatchEventInstanceImpl(101L, "name", processInstanceId));
        repository.add(buildSTimerEventTriggerInstance(96L, eventInstanceImpl.getId(), "name", "jobTriggerName"));

        final SIntermediateCatchEventInstanceImpl eventInstanceImpl2 = (SIntermediateCatchEventInstanceImpl) repository
                .add(buildSIntermediateCatchEventInstanceImpl(103L, "toto", processInstanceId));
        repository.add(buildSTimerEventTriggerInstance(98L, eventInstanceImpl2.getId(), "toto", "plop"));

        // Then
        final List<SEventTriggerInstance> sEventTriggerInstances = repository.searchTimerEventTriggerInstances(processInstanceId, "toto");

        // When
        assertEquals(1, sEventTriggerInstances.size());
        assertEquals(98L, sEventTriggerInstances.get(0).getId());
    }

    private STimerEventTriggerInstanceImpl buildSTimerEventTriggerInstance(final long id, final long eventInstanceId, final String eventInstanceName,
            final String jobTriggerName) {
        final STimerEventTriggerInstanceImpl sTimerEventTriggerInstanceImpl = new STimerEventTriggerInstanceImpl(eventInstanceId, eventInstanceName, 96L,
                jobTriggerName);
        sTimerEventTriggerInstanceImpl.setId(id);
        sTimerEventTriggerInstanceImpl.setTenantId(PersistentObjectBuilder.DEFAULT_TENANT_ID);
        return sTimerEventTriggerInstanceImpl;
    }

    private SThrowErrorEventTriggerInstanceImpl buildSThrowErrorEventTriggerInstanceImpl(final long id, final long eventInstanceId) {
        final SThrowErrorEventTriggerInstanceImpl sThrowErrorEventTriggerInstanceImpl = new SThrowErrorEventTriggerInstanceImpl(eventInstanceId, "errorCode");
        sThrowErrorEventTriggerInstanceImpl.setId(id);
        sThrowErrorEventTriggerInstanceImpl.setTenantId(PersistentObjectBuilder.DEFAULT_TENANT_ID);
        return sThrowErrorEventTriggerInstanceImpl;
    }

    private SIntermediateCatchEventInstanceImpl buildSIntermediateCatchEventInstanceImpl(final long id, final String name, final long processInstanceId) {
        final SIntermediateCatchEventInstanceImpl sIntermediateCatchEventInstanceImpl = new SIntermediateCatchEventInstanceImpl(name, 9, 6, 8, 4, 2);
        sIntermediateCatchEventInstanceImpl.setLogicalGroup(3, processInstanceId);
        sIntermediateCatchEventInstanceImpl.setId(id);
        sIntermediateCatchEventInstanceImpl.setTenantId(PersistentObjectBuilder.DEFAULT_TENANT_ID);
        return sIntermediateCatchEventInstanceImpl;
    }

}
