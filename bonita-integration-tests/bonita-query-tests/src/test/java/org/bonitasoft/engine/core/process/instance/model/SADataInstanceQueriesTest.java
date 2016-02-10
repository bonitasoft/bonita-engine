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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;
import org.bonitasoft.engine.data.instance.model.archive.impl.SAShortTextDataInstanceImpl;
import org.bonitasoft.engine.test.persistence.repository.SADataInstanceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class SADataInstanceQueriesTest {

    @Inject
    private SADataInstanceRepository repository;

    private void addSADataInstance(final int id, final String name, final int containerId, final String value, final long archiveDate, final long sourceObjectId) {
        final SAShortTextDataInstanceImpl dataInstance = new SAShortTextDataInstanceImpl();
        dataInstance.setTenantId(1);
        dataInstance.setId(id);
        dataInstance.setName(name);
        dataInstance.setClassName(String.class.getName());
        dataInstance.setContainerId(containerId);
        dataInstance.setContainerType("PROCESS_INSTANCE");
        dataInstance.setValue(value);
        dataInstance.setArchiveDate(archiveDate);
        dataInstance.setSourceObjectId(sourceObjectId);
        repository.add(dataInstance);
    }

    @Test
    public void getSADataInstancesByDataInstanceIdAndArchiveDate_should_return_an_empty_list_if_not_data_defined() {
        final List<Long> dataInstanceIds = new ArrayList<Long>();
        dataInstanceIds.add(4L);
        final long time = System.currentTimeMillis();

        final List<SADataInstance> dataInstances = repository.getSADataInstancesByDataInstanceIdAndArchiveDate(dataInstanceIds, time, 1);

        assertThat(dataInstances).isEmpty();
    }

    @Test
    public void getSADataInstancesByDataInstanceIdAndArchiveDate_should_return_an_empty_list_if_not_matching_identifiers() {
        addSADataInstance(1025244, "identifiant", 41008, null, 1411051738348L, 205093L);
        final List<Long> dataInstanceIds = new ArrayList<Long>();
        dataInstanceIds.add(4L);

        final List<SADataInstance> dataInstances = repository.getSADataInstancesByDataInstanceIdAndArchiveDate(dataInstanceIds, 1411051738348L, 1);

        assertThat(dataInstances).isEmpty();
    }

    @Test
    public void getSADataInstancesByDataInstanceIdAndArchiveDate_should_return_an_empty_list_if_not_matching_time() {
        final long archiveDate = 1411051738348L;
        addSADataInstance(1025244, "identifiant", 41008, null, archiveDate, 205093L);
        addSADataInstance(1025259, "identifiant", 41008, "matti", archiveDate + 1500, 205093L);

        final List<Long> dataInstanceIds = new ArrayList<Long>();
        dataInstanceIds.add(205093L);

        final long time = 0L;
        final List<SADataInstance> dataInstances = repository.getSADataInstancesByDataInstanceIdAndArchiveDate(dataInstanceIds, time, 1);
        assertThat(dataInstances).isEmpty();
    }

    @Test
    public void getSADataInstancesByDataInstanceIdAndArchiveDate_should_get_the_latest_upate_of_all_data_updates() {
        final long archiveDate = 1411051738348L;
        addSADataInstance(1025244, "identifiant", 41008, null, archiveDate, 205093L);
        addSADataInstance(1025259, "identifiant", 41008, "matti", archiveDate + 1500, 205093L);
        addSADataInstance(1025356, "identifiant", 41008, "hannu", archiveDate + 2500, 205093L);

        final List<Long> dataInstanceIds = new ArrayList<Long>();
        dataInstanceIds.add(205093L);

        final List<SADataInstance> dataInstances = repository.getSADataInstancesByDataInstanceIdAndArchiveDate(dataInstanceIds, archiveDate + 5000, 1);
        assertThat(dataInstances).hasSize(1);
        assertThat(dataInstances.get(0).getId()).isEqualTo(1025356);
    }

    @Test
    public void getSADataInstancesByDataInstanceIdAndArchiveDate_should_get_the_latest_upate_of_a_frame_of_data_updates() {
        final long archiveDate = 1411051738348L;
        addSADataInstance(1025244, "identifiant", 41008, null, archiveDate, 205093L);
        addSADataInstance(1025259, "identifiant", 41008, "matti", archiveDate + 1500, 205093L);
        addSADataInstance(1025356, "identifiant", 41008, "hannu", archiveDate + 2500, 205093L);

        final List<Long> dataInstanceIds = new ArrayList<Long>();
        dataInstanceIds.add(205093L);

        final List<SADataInstance> dataInstances = repository.getSADataInstancesByDataInstanceIdAndArchiveDate(dataInstanceIds, archiveDate + 2000, 1);
        assertThat(dataInstances).hasSize(1);
        assertThat(dataInstances.get(0).getId()).isEqualTo(1025259);
    }

}
