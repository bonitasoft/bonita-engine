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

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.bpm.bar.ActorMappingMarshaller;
import org.bonitasoft.engine.bpm.bar.XmlMarshallException;
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor;
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

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
    public void should_Correctly_Read_an_Old_Actor_Mapping_Xml() throws Exception {
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
    public void should_Throw_Exception_On_Non_Valid_Xml_Reading() throws Exception {
        InputStream xmlStream = ActorMappingMarshallerTest.class.getResourceAsStream("/testThatShouldFail.xml");
        String xmlContent = IOUtils.toString(xmlStream);
            ActorMapping actorMappingFromXML = marshaller.deserializeFromXML(xmlContent.getBytes());
    }

    @Test
    public void should_Correctly_Read_a_Null_Actor_Mapping() throws Exception {
        ActorMapping employeeActor = new ActorMapping();
        byte[] bytes = marshaller.serializeToXML(employeeActor);
        Object actors = marshaller.deserializeFromXML(bytes);
        assertThat(employeeActor).isEqualTo(actors);
    }

    @Test
    public void should_Read_Correctly_an_Actor_Mapping_With_an_Empty_Actor() throws Exception {
        ActorMapping employeeActor = new ActorMapping();
        Actor actor = new Actor("lulu");
        employeeActor.addActor(actor);
        byte[] result;
        result = marshaller.serializeToXML(employeeActor);
        ActorMapping result2 = marshaller.deserializeFromXML(result);
        assertThat(employeeActor).isEqualTo(result2);
    }

    @Test
    public void should_Read_and_Write_Correctly_a_Complete_Actor_Mapping() throws Exception {
        ActorMapping employeeActor = new ActorMapping();
        employeeActor.addActor(new Actor("EmployeeMembership1"));
        employeeActor.addActor(new Actor("EmployeeMembership2"));
        employeeActor.getActors().get(0).addUser("william.jobs");
        employeeActor.getActors().get(0).addGroup("RD");
        employeeActor.getActors().get(0).addRole("dev");
        employeeActor.getActors().get(1).addUser("lala.ru");
        employeeActor.getActors().get(1).addMembership("group1", "role1");
        employeeActor.getActors().get(0).setDescription("Just here for the tests");
        byte[] result;
        result = marshaller.serializeToXML(employeeActor);
        Object actors = marshaller.deserializeFromXML(result);
        assertThat(employeeActor).isEqualToComparingFieldByField((ActorMapping) actors);
    }
}
