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
package org.bonitasoft.engine.bpm.bar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.bpm.bar.actorMapping.Actor;
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor.Membership;
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping;
import org.bonitasoft.engine.bpm.bar.form.model.FormMappingModel;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BusinessArchiveBuilderTest {

    @Spy
    BusinessArchiveBuilder archive;

    @Before
    public void initialization() {
        archive.createNewBusinessArchive();
    }

    @Test
    public void addFormMappingsShouldAddFileWithProperName() throws Exception {
        final FormMappingModel inputModel = new FormMappingModel();
        // when:
        final BusinessArchive archive = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(new ProcessDefinitionBuilder().createNewInstance("proc", "1").done())
                .setFormMappings(inputModel).done();

        // then:
        assertThat(archive.getFormMappingModel()).isEqualTo(inputModel);
    }

    @Test
    public void ensure_deprecated_actorMapping_file_can_still_be_read() throws Exception {
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<actorMappings:actorMappings xmlns:actorMappings=\"http://www.bonitasoft.org/ns/actormapping/6.0\">"
                + "<actorMapping name=\"Employee actor\">"
                + "<users>"
                + "<user>john</user>"
                + "</users>"
                + "<roles>"
                + "<role>dev</role>"
                + "</roles>"
                + "<groups>"
                + "<group>/RD</group>"
                + "</groups>"
                + "<memberships>"
                + "<membership>"
                + "<role>dev</role>"
                + "<group>/RD</group>"
                + "</membership>"
                + "</memberships>"
                + "</actorMapping>"
                + "</actorMappings:actorMappings>";
        byte[] xmlContent = xmlString.getBytes();
        final ActorMapping actorMapping = archive.setActorMapping(xmlContent).getActorMapping();

        assertThat(actorMapping).isNotNull();
        assertThat(actorMapping.getActors()).hasSize(1);

        final Actor actor = actorMapping.getActors().get(0);
        assertThat(actor.getName()).isEqualTo("Employee actor");
        for (String userName : actor.getUsers()) {
            assertThat(userName).isEqualTo("john");
        }
        for (String role : actor.getRoles()) {
            assertThat(role).isEqualTo("dev");
        }
        for (String group : actor.getGroups()) {
            assertThat(group).isEqualTo("/RD");
        }
        for (Membership membership : actor.getMemberships()) {
            assertThat(membership.getGroup()).isEqualTo("/RD");
            assertThat(membership.getRole()).isEqualTo("dev");
        }

        verify(archive, times(1)).setActorMapping(actorMapping);

        final String generatedXMLActorMapping = new String(new ActorMappingMarshaller().serializeToXML(actorMapping));
        assertThat(generatedXMLActorMapping).doesNotContain("ns2");
        assertThat(generatedXMLActorMapping).contains("actorMappings:");
    }
}
