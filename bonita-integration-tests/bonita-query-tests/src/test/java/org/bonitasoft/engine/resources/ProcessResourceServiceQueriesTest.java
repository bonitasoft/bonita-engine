/**
 * Copyright (C) 2015 Bonitasoft S.A.
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
import static org.bonitasoft.engine.test.persistence.builder.BARResourceBuilder.aBARResource;

import java.util.List;
import javax.inject.Inject;

import org.bonitasoft.engine.test.persistence.repository.ProcessResourceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = {"/testContext.xml"})
@Transactional
public class ProcessResourceServiceQueriesTest {

    @Inject
    private ProcessResourceRepository repository;

    @Test
    public void getBarResource_should_get_one_resource() {
        // given
        SBARResource resource = repository.add(aBARResource().withName("MyResource").withContent("The content".getBytes()).withProcessDefinitionId(345L).withType(BARResourceType.EXTERNAL).build());
        repository.add(aBARResource().withName("MyResource2").withContent("The content@".getBytes()).withProcessDefinitionId(346L).withType(BARResourceType.EXTERNAL).build());

        //when
        SBARResource myResource = repository.getBARResource(345L, BARResourceType.EXTERNAL, "MyResource");
        // //then
        assertThat(myResource.getContent()).isEqualTo("The content".getBytes());
        assertThat(myResource.getId()).isEqualTo(resource.getId());
    }

    @Test
    public void getBarResourceOfType_should_get_all_resource_of_type() {
        // given
        SBARResource resource1 = repository.add(aBARResource().withName("MyResource").withContent("The content".getBytes()).withProcessDefinitionId(345L).withType(BARResourceType.EXTERNAL).build());
        repository.add(aBARResource().withName("MyResource2").withContent("The content@".getBytes()).withProcessDefinitionId(346L).withType(BARResourceType.EXTERNAL).build());
        SBARResource resource2 = repository.add(aBARResource().withName("MyResource3").withContent("The content3".getBytes()).withProcessDefinitionId(345L).withType(BARResourceType.EXTERNAL).build());
        repository.add(aBARResource().withName("MyConnector").withContent("The content".getBytes()).withProcessDefinitionId(345L).withType(BARResourceType.CONNECTOR).build());

        //when
        List<SBARResource> myResources = repository.getBARResourcesOfType(345L, BARResourceType.EXTERNAL);
        // //then
        assertThat(myResources).extracting("id", "content").containsOnly(tuple(resource1.getId(), resource1.getContent()), tuple(resource2.getId(), resource2.getContent()));
    }

    @Test
    public void getNumberBarResourceOfType_should_get_all_resource_of_type() {
        // given
        repository.add(aBARResource().withName("MyResource").withContent("The content".getBytes()).withProcessDefinitionId(345L).withType(BARResourceType.USER_FILTER).build());
        repository.add(aBARResource().withName("MyResource2").withContent("The content@".getBytes()).withProcessDefinitionId(346L).withType(BARResourceType.USER_FILTER).build());
        repository.add(aBARResource().withName("MyResource3").withContent("The content3".getBytes()).withProcessDefinitionId(345L).withType(BARResourceType.USER_FILTER).build());
        repository.add(aBARResource().withName("MyConnector").withContent("The content".getBytes()).withProcessDefinitionId(345L).withType(BARResourceType.CONNECTOR).build());

        //when
        long myResources = repository.getNumberOfBARResourcesOfType(345L, BARResourceType.USER_FILTER);
        // //then
        assertThat(myResources).isEqualTo(2);
    }

    @Test
    public void getBarResourceLightOfType_should_get_all_resource_of_type_without_content() {
        // given
        SBARResource resource1 = repository.add(aBARResource().withName("MyResource").withContent("The content".getBytes()).withProcessDefinitionId(345L).withType(BARResourceType.EXTERNAL).build());
        repository.add(aBARResource().withName("MyResource2").withContent("The content@".getBytes()).withProcessDefinitionId(346L).withType(BARResourceType.EXTERNAL).build());
        SBARResource resource2 = repository.add(aBARResource().withName("MyResource3").withContent("The content3".getBytes()).withProcessDefinitionId(345L).withType(BARResourceType.EXTERNAL).build());
        repository.add(aBARResource().withName("MyConnector").withContent("The content".getBytes()).withProcessDefinitionId(345L).withType(BARResourceType.CONNECTOR).build());

        //when
        List<SBARResourceLight> myResources = repository.getBARResourcesLightOfType(345L, BARResourceType.EXTERNAL);
        // //then
        assertThat(myResources).extracting("id").containsOnly(resource1.getId(), resource2.getId());
        for (SBARResourceLight myResource : myResources) {
            assertThat(myResource).isInstanceOf(SBARResourceLight.class);
            assertThat(myResource).isNotInstanceOf(SBARResource.class);
        }
    }

}
