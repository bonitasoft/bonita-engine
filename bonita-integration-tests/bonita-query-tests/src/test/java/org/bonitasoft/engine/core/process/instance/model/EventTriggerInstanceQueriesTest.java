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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.bonitasoft.engine.core.process.instance.model.event.SIntermediateCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.test.persistence.builder.PersistentObjectBuilder;
import org.bonitasoft.engine.test.persistence.repository.ProcessInstanceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class EventTriggerInstanceQueriesTest {

    @Autowired
    private ProcessInstanceRepository repository;

    @Test
    public void getNumberOfTimerEventTriggerInstances_should_return_number_of_event_trigger_instance_attached_to_the_process_instance() {
        // Given
        final long processInstanceId = 9634L;

        final SIntermediateCatchEventInstance eventInstanceImpl = (SIntermediateCatchEventInstance) repository
                .add(buildSIntermediateCatchEventInstanceImpl(101L, "name", processInstanceId));

        repository.add(buildSTimerEventTriggerInstance(87L, eventInstanceImpl.getId(), "name", "jobTriggerName"));

        // Then
        final long numberOfEventTriggerInstances = repository.getNumberOfTimerEventTriggerInstances(processInstanceId,
                null);

        // When
        assertEquals(1, numberOfEventTriggerInstances);
    }

    @Test
    public void getNumberOfTimerEventTriggerInstances_should_return_number_of_event_trigger_instance_attached_to_the_process_instance_filter_by_event_instance_name() {
        // Given
        final long processInstanceId = 9634L;

        final SIntermediateCatchEventInstance eventInstanceImpl = (SIntermediateCatchEventInstance) repository
                .add(buildSIntermediateCatchEventInstanceImpl(101L, "name", processInstanceId));
        repository.add(buildSTimerEventTriggerInstance(96L, eventInstanceImpl.getId(), "name", "jobTriggerName"));

        final SIntermediateCatchEventInstance eventInstanceImpl2 = (SIntermediateCatchEventInstance) repository
                .add(buildSIntermediateCatchEventInstanceImpl(104L, "toto", processInstanceId));
        repository.add(buildSTimerEventTriggerInstance(98L, eventInstanceImpl2.getId(), "toto", "plop"));

        // Then
        final long numberOfEventTriggerInstances = repository.getNumberOfTimerEventTriggerInstances(processInstanceId,
                "name");

        // When
        assertEquals(1, numberOfEventTriggerInstances);
    }

    @Test
    public void searchTimerEventTriggerInstances_should_return_event_trigger_instance_attached_to_the_process_instance() {
        // Given
        final long processInstanceId = 9634L;

        final SIntermediateCatchEventInstance eventInstanceImpl = (SIntermediateCatchEventInstance) repository
                .add(buildSIntermediateCatchEventInstanceImpl(101L, "name", processInstanceId));

        repository.add(buildSTimerEventTriggerInstance(87L, eventInstanceImpl.getId(), "name", "jobTriggerName"));

        // Then
        final List<STimerEventTriggerInstance> sTimerEventTriggerInstances = repository
                .searchTimerEventTriggerInstances(processInstanceId, null);

        // When
        assertEquals(1, sTimerEventTriggerInstances.size());
    }

    @Test
    public void searchTimerEventTriggerInstances_should_should_return_event_trigger_instance_attached_to_the_process_instance_filter_by_event_instance_name() {
        // Given
        final long processInstanceId = 9634L;

        final SIntermediateCatchEventInstance eventInstanceImpl = (SIntermediateCatchEventInstance) repository
                .add(buildSIntermediateCatchEventInstanceImpl(101L, "name", processInstanceId));
        repository.add(buildSTimerEventTriggerInstance(96L, eventInstanceImpl.getId(), "name", "jobTriggerName"));

        final SIntermediateCatchEventInstance eventInstanceImpl2 = (SIntermediateCatchEventInstance) repository
                .add(buildSIntermediateCatchEventInstanceImpl(103L, "toto", processInstanceId));
        repository.add(buildSTimerEventTriggerInstance(98L, eventInstanceImpl2.getId(), "toto", "plop"));

        // Then
        final List<STimerEventTriggerInstance> sTimerEventTriggerInstances = repository
                .searchTimerEventTriggerInstances(processInstanceId, "toto");

        // When
        assertEquals(1, sTimerEventTriggerInstances.size());
        assertEquals(98L, sTimerEventTriggerInstances.get(0).getId());
    }

    private STimerEventTriggerInstance buildSTimerEventTriggerInstance(final long id, final long eventInstanceId,
            final String eventInstanceName,
            final String jobTriggerName) {
        final STimerEventTriggerInstance sTimerEventTriggerInstance = new STimerEventTriggerInstance(eventInstanceId,
                eventInstanceName, 96L,
                jobTriggerName);
        sTimerEventTriggerInstance.setId(id);
        sTimerEventTriggerInstance.setTenantId(PersistentObjectBuilder.DEFAULT_TENANT_ID);
        return sTimerEventTriggerInstance;
    }

    private SIntermediateCatchEventInstance buildSIntermediateCatchEventInstanceImpl(final long id, final String name,
            final long processInstanceId) {
        final SIntermediateCatchEventInstance sIntermediateCatchEventInstance = new SIntermediateCatchEventInstance(
                name, 9, 6, 8, 4, 2);
        sIntermediateCatchEventInstance.setLogicalGroup(3, processInstanceId);
        sIntermediateCatchEventInstance.setId(id);
        sIntermediateCatchEventInstance.setTenantId(PersistentObjectBuilder.DEFAULT_TENANT_ID);
        return sIntermediateCatchEventInstance;
    }

}
