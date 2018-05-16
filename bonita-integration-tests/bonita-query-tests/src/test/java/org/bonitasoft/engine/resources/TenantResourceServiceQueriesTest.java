/**
 * Copyright (C) 2016 Bonitasoft S.A.
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
package org.bonitasoft.engine.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.bonitasoft.engine.test.persistence.builder.TenantResourceBuilder.aTenantResource;

import java.util.List;
import javax.inject.Inject;

import org.bonitasoft.engine.test.persistence.repository.TenantResourceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = {"/testContext.xml"})
@Transactional
public class TenantResourceServiceQueriesTest {

    @Inject
    private TenantResourceRepository repository;

    @Test
    public void getTenantResource_should_get_one_resource() {
        // given
        STenantResource resource = repository.add(aTenantResource().withName("MyResource").withContent("The content".getBytes()).withType(TenantResourceType.BDM).withState(STenantResourceState.INSTALLED).lastUpdatedBy(135L).withLastUpdateDate(1973L).build());
        repository.add(aTenantResource().withName("MyResource2").withContent("The content@".getBytes()).withType(TenantResourceType.BDM).build());

        //when
        STenantResource myResource = repository.getTenantResource(TenantResourceType.BDM, "MyResource");
        // //then
        assertThat(myResource.getContent()).isEqualTo("The content".getBytes());
        assertThat(myResource.lastUpdateDate).isEqualTo(1973L);
        assertThat(myResource.lastUpdatedBy).isEqualTo(135L);
        assertThat(myResource.state).isEqualTo(STenantResourceState.INSTALLED);
        assertThat(myResource.getId()).isEqualTo(resource.getId());
    }

    @Test
    public void getTenantResourceOfType_should_get_all_resource_of_type() {
        // given
        STenantResource resource1 = repository.add(aTenantResource().withName("MyResource").withContent("The content".getBytes()).withType(TenantResourceType.BDM).build());
        STenantResource resource2 = repository.add(aTenantResource().withName("MyResource2").withContent("The content@".getBytes()).withType(TenantResourceType.BDM).build());
        STenantResource resource3 = repository.add(aTenantResource().withName("MyResource3").withContent("The content3".getBytes()).withType(TenantResourceType.BDM).build());
        STenantResource resource4 = repository.add(aTenantResource().withName("MyConnector").withContent("The content".getBytes()).withType(TenantResourceType.BDM).build());

        //when
        List<STenantResource> myResources = repository.getTenantResourcesOfType(TenantResourceType.BDM);
        // //then
        assertThat(myResources).extracting("id", "content").containsOnly(tuple(resource1.getId(), resource1.getContent()),
                tuple(resource2.getId(), resource2.getContent()),
                tuple(resource3.getId(), resource3.getContent()),
                tuple(resource4.getId(), resource4.getContent()));
    }

    @Test
    public void getNumberTenantResourceOfType_should_get_all_resource_of_type() {
        // given
        repository.add(aTenantResource().withName("MyResource").withContent("The content".getBytes()).withType(TenantResourceType.BDM).build());
        repository.add(aTenantResource().withName("MyResource2").withContent("The content@".getBytes()).withType(TenantResourceType.BDM).build());
        repository.add(aTenantResource().withName("MyResource3").withContent("The content3".getBytes()).withType(TenantResourceType.BDM).build());
        repository.add(aTenantResource().withName("MyConnector").withContent("The content".getBytes()).withType(TenantResourceType.BDM).build());

        //when
        long myResources = repository.getNumberOfTenantResourcesOfType(TenantResourceType.BDM);
        // //then
        assertThat(myResources).isEqualTo(4);
    }

    @Test
    public void getTenantResourceLightOfType_should_get_all_resource_of_type_without_content() {
        // given
        STenantResource resource1 = repository.add(aTenantResource().withName("MyResource").withContent("The content".getBytes()).withType(TenantResourceType.BDM).build());
        STenantResource resource2 = repository.add(aTenantResource().withName("MyResource2").withContent("The content@".getBytes()).withType(TenantResourceType.BDM).build());
        STenantResource resource3 = repository.add(aTenantResource().withName("MyResource3").withContent("The content3".getBytes()).withType(TenantResourceType.BDM).build());
        STenantResource resource4 = repository.add(aTenantResource().withName("MyConnector").withContent("The content".getBytes()).withType(TenantResourceType.BDM).build());

        //when
        List<STenantResourceLight> myResources = repository.getTenantResourcesLightOfType(TenantResourceType.BDM);
        // //then
        assertThat(myResources).extracting("id").containsOnly(resource1.getId(),
                resource2.getId(),
                resource3.getId(),
                resource4.getId());
        for (STenantResourceLight myResource : myResources) {
            assertThat(myResource).isInstanceOf(STenantResourceLight.class);
            assertThat(myResource).isNotInstanceOf(STenantResource.class);
        }
    }

}
