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

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.company.model.Address;
import com.company.model.Person;
import com.company.model.PersonWithDetails;
import com.company.model.Phone;
import javassist.util.proxy.MethodHandler;
import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.junit.Before;
import org.junit.Test;

public class JsonBusinessDataSerializerImplTest {

    private static final String PARAMETER_BUSINESSDATA_CLASS_URI_VALUE = "/businessdata/{className}/{id}/{field}";

    private JsonBusinessDataSerializerImpl jsonBusinessDataSerializer;

    private ClassLoaderService classLoaderService = mock(ClassLoaderService.class);

    @Before
    public void setUp() throws Exception {
        jsonBusinessDataSerializer = new JsonBusinessDataSerializerImpl(classLoaderService);
    }

    @Test
    public void entity_should_be_serialized() throws Exception {
        // given
        Entity person = initPerson(1L);

        // when
        final String jsonPerson = jsonBusinessDataSerializer.serializeEntity(person, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        // then
        assertThatJson(jsonPerson).as("entity serialization").isEqualTo(getJsonContent("singlePerson.json"));
    }

    @Test
    public void entity_with_nested_entity_fields_should_be_serialized() throws Exception {
        // given
        PersonWithDetails person = initPersonWithDetails(666L);

        // when
        final String jsonPerson = jsonBusinessDataSerializer.serializeEntity(person, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        // then
        assertThatJson(jsonPerson).as("entity with nested entity fields serialization")
                .isEqualTo(getJsonContent("personWithDetails.json"));
    }

    @Test
    public void entity_list_should_be_serialized() throws Exception {
        // given
        List<Entity> persons = IntStream.range(1, 3).mapToObj(i -> initPerson(i)).collect(Collectors.toList());

        // when
        final String jsonPersonList = jsonBusinessDataSerializer.serializeEntities(persons, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        // then
        assertThatJson(jsonPersonList).as("entity list serialization").isEqualTo(getJsonContent("multiplePerson.json"));
    }

    @Test
    public void serialization_of_entity_should_use_fields_and_not_getters() throws Exception {
        //given
        EntitySerializerPojo value = createPojo();

        //when
        final String serializedEntity = jsonBusinessDataSerializer.serializeEntity(value,
                PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        //then
        assertThatJson(serializedEntity).as("serialization uses fields instead of getters to compute json properties")
                .isEqualTo(getJsonContent("EntitySerializerPojo.json"));
    }

    // =================================================================================================================
    // UTILS
    // =================================================================================================================

    private static EntitySerializerPojo createPojo() {
        final EntitySerializerPojo entitySerializerPojo = new EntitySerializerPojo();

        entitySerializerPojo.setPersistenceId(Long.MAX_VALUE);
        entitySerializerPojo.setPersistenceVersion(1L);
        entitySerializerPojo.setABoolean(Boolean.TRUE);
        entitySerializerPojo.setADate(new Date(123L));
        entitySerializerPojo.setALocalDate(LocalDate.of(2017, 3, 6));
        entitySerializerPojo.setALocalDateTime(LocalDateTime.of(1945, 5, 8, 12, 31, 17));
        entitySerializerPojo.setAnOffsetDateTime(OffsetDateTime.of(LocalDateTime.of(1949, 4, 9, 12, 14, 5), ZoneOffset.ofHours(1)));
        entitySerializerPojo.setADouble(Double.MAX_VALUE);
        entitySerializerPojo.setAFloat(Float.MAX_VALUE);
        entitySerializerPojo.setAInteger(Integer.MAX_VALUE);
        entitySerializerPojo.setALong(Long.MAX_VALUE);
        entitySerializerPojo.setAString("string");
        entitySerializerPojo.setAText("text");
        entitySerializerPojo.addToManyLong(1L);
        entitySerializerPojo.addToManyLong(2L);
        entitySerializerPojo.addToManyString("abc");
        entitySerializerPojo.addToManyString("def");

        return entitySerializerPojo;
    }

    private static Person initPerson(long persistenceId) {
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
        return new String(IOUtils.toByteArray(this.getClass().getResourceAsStream(jsonFileName)));
    }

    private static PersonWithDetails initPersonWithDetails(long persistenceId) {
        PersonWithDetails person = new PersonWithDetails();
        person.setPersistenceId(persistenceId);
        person.setPersistenceVersion(2L);
        person.setName("John");
        person.setAge(50);
        person.setBirthday(new Date(123456789L));
        person.setHasMobile(Boolean.TRUE);

        for (int i = 0; i < 3; i++) {
            Phone ph = new Phone();
            ph.setPersistenceVersion(365L + i);
            ph.setPersistenceId(22365L + i);
            ph.setNumber("123456" + i);
            person.addToPhones(ph);
        }

        person.addToBools(true);
        person.addToBools(false);
        person.addToBools(true);
        person.addToBools(false);

        person.addToIgnores(1L);
        person.addToIgnores(3L);
        person.addToIgnores(5L);
        person.addToIgnores(6L);

        Phone secretPhone = new Phone();
        secretPhone.setPersistenceId(3615L);
        secretPhone.setNumber("999999");
        person.setSecretPhone(secretPhone);

        person.addToIncludes(1235L);
        person.addToIncludes(6666L);
        person.addToIncludes(7777L);

        Address address1 = new Address();
        address1.setPersistenceId(2598L);
        address1.setPersistenceVersion(99992598L);
        address1.setStreet("Rue Gustave Eiffel");
        address1.setNumber(32f);
        address1.setFloors(Arrays.asList(1d, 4d));
        address1.setDoorCode("my-secret-password");
        person.setAddress1(address1);

        Address address2 = new Address();
        address2.setPersistenceId(358L);
        address2.setPersistenceVersion(118L);
        address2.setDoorCode(null);
        person.setAddress2(address2);

        person.setMethodHandlerObject(mock(MethodHandler.class));
        person.addToProxysAsMethodHandler("I love Maths");
        person.addToProxysAsProxyImpl(365L);
        person.addToProxysAsProxyObjectImpl(secretPhone);

        return person;
    }

}
