package com.bonitasoft.engine.business.data.impl;

import static net.javacrumbs.jsonunit.assertj.JsonAssert.assertThatJson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.JsonBusinessDataSerializer;
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
