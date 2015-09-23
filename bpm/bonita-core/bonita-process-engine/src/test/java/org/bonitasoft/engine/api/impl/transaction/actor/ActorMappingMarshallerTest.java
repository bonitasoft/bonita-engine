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

package org.bonitasoft.engine.api.impl.transaction.actor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.bpm.bar.ActorMappingMarshaller;
import org.bonitasoft.engine.bpm.bar.XmlMarshallException;
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor;
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping;
import org.junit.Before;
import org.junit.Test;

/**
 * @author mazourd
 */
public class ActorMappingMarshallerTest {

    ActorMappingMarshaller marshaller;

    @Before
    public void initialization() {
        this.marshaller = new ActorMappingMarshaller();
    }

    @Test
    public void should_correctly_read_an_old_actor_mapping_xml() throws Exception {
        InputStream xmlStream = ActorMappingMarshallerTest.class.getResourceAsStream("/complexActorMapping.xml");
        ActorMapping actorMappingFromXML = marshaller.deserializeFromXML(IOUtils.toByteArray(xmlStream));
        Actor actorTest = new Actor("Employee actor");
        actorTest.addUser("john");
        actorTest.addRole("dev");
        actorTest.addGroup("/RD");
        actorTest.addMembership("/RD", "dev");
        assertThat(actorMappingFromXML.getActors()).contains(actorTest);
        xmlStream.close();
    }

    @Test(expected = XmlMarshallException.class)
    public void should_throw_exception_on_non_valid_xml_reading() throws Exception {
        InputStream xmlStream = ActorMappingMarshallerTest.class.getResourceAsStream("/testThatShouldFail.xml");
        String xmlContent = IOUtils.toString(xmlStream);
        marshaller.deserializeFromXML(xmlContent.getBytes());
    }

    @Test
    public void should_correctly_read_an_empty_actor_mapping() throws Exception {
        ActorMapping employeeActor = new ActorMapping();
        byte[] bytes = marshaller.serializeToXML(employeeActor);
        Object actors = marshaller.deserializeFromXML(bytes);
        assertThat(employeeActor).isEqualTo(actors);
    }

    @Test
    public void should_read_correctly_an_actor_mapping_with_an_empty_actor() throws Exception {
        ActorMapping employeeActor = new ActorMapping();
        Actor actor = new Actor("lulu");
        employeeActor.addActor(actor);
        byte[] result = marshaller.serializeToXML(employeeActor);
        ActorMapping result2 = marshaller.deserializeFromXML(result);
        assertThat(employeeActor).isEqualTo(result2);
    }

    @Test
    public void should_read_and_write_correctly_a_complete_actor_mapping() throws Exception {
        ActorMapping employeeActor = new ActorMapping();
        employeeActor.addActor(new Actor("EmployeeMembership1"));
        employeeActor.addActor(new Actor("EmployeeMembership2"));
        final Actor actor = employeeActor.getActors().get(0);
        actor.addUser("william.jobs");
        actor.addGroup("RD");
        actor.addRole("dev");
        actor.setDescription("Just here for the tests");
        final Actor actor1 = employeeActor.getActors().get(1);
        actor1.addUser("lala.ru");
        actor1.addMembership("group1", "role1");
        byte[] result = marshaller.serializeToXML(employeeActor);
        Object actors = marshaller.deserializeFromXML(result);
        assertThat(employeeActor).isEqualToComparingFieldByField((ActorMapping) actors);
    }
}
