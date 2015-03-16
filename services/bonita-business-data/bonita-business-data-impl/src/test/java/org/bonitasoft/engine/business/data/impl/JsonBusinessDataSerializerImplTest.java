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
package org.bonitasoft.engine.business.data.impl;

import static net.javacrumbs.jsonunit.assertj.JsonAssert.assertThatJson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.business.data.JsonBusinessDataSerializer;
import com.company.model.Person;
import com.company.model.Phone;

public class JsonBusinessDataSerializerImplTest {

    private static final String PARAMETER_BUSINESSDATA_CLASS_URI_VALUE = "/businessdata/{className}/{id}/{field}";

    JsonBusinessDataSerializer jsonBusinessDataSerializer;

    Entity person;

    List<Entity> personList;

    @Before
    public void setUp() throws Exception {
        jsonBusinessDataSerializer = new JsonBusinessDataSerializerImpl();

    }

    @Test
    public void testSerializeEntity() throws Exception {
        // given
        this.person = initPerson(1L);

        // when
        final String jsonPerson = jsonBusinessDataSerializer.serializeEntity(person, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        // then
        assertThatJson(jsonPerson).as("should get employee count ").isEqualTo(getJsonContent("singlePerson.json"));
    }

    @Test
    public void testSerializeEntityList() throws Exception {

        // given
        this.personList = new ArrayList<Entity>();
        for (long i = 1L; i < 3L; i++) {
            personList.add(initPerson(i));
        }

        // when
        final String jsonPersonList = jsonBusinessDataSerializer.serializeEntity(personList, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        // then
        assertThatJson(jsonPersonList).as("should get employee count ").isEqualTo(getJsonContent("multiplePerson.json"));
    }

    private Person initPerson(long persistenceId) {
        Person person;
        person = new Person();
        person.setPersistenceId(persistenceId);
        person.setPersistenceVersion(2L);
        person.setName("John");
        person.setAge(50);
        person.setBirthday(new Date(123456789L));
        person.setHasMobile(Boolean.TRUE);

        for (int i = 0; i < 3; i++) {
            Phone ph = new Phone();
            ph.setNumber("123456" + i);
            person.addToPhones(ph);
        }

        person.addToBools(true);
        person.addToBools(false);
        person.addToBools(true);
        person.addToBools(false);

        return person;
    }

    private String getJsonContent(String jsonFileName) throws IOException {
        final String json;
        json = new String(IOUtils.toByteArray(this.getClass().getResourceAsStream(jsonFileName)));
        return json;
    }
}
