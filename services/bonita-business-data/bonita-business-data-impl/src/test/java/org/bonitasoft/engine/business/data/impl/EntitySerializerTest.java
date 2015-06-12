package org.bonitasoft.engine.business.data.impl;

import static net.javacrumbs.jsonunit.assertj.JsonAssert.assertThatJson;

import java.io.StringWriter;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Laurent Leseigneur
 */
public class EntitySerializerTest {

    ObjectMapper mapper;

    EntitySerializer serializer;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        serializer = new EntitySerializer();
        final SimpleModule hbm = new SimpleModule();
        hbm.addSerializer(serializer);
        mapper.registerModule(hbm);
    }

    @Test
    public void patternUri_should_replace_field_value() throws Exception {
        //given
        final StringWriter writer = new StringWriter();
        EntitySerializerPojo value = createPojo();

        //when
        mapper.writeValue(writer, value);
        final String string = writer.toString();

        //then
        final String expectedJson = new String(IOUtils.toByteArray(this.getClass().getResourceAsStream("EntitySerializerPojo.json")));
        // assertThat(string).as("should replace field value").isEqualTo(expectedJson);
        assertThatJson(string).as("should replace field value").isEqualTo(expectedJson);
    }

    protected EntitySerializerPojo createPojo() {
        final EntitySerializerPojo entitySerializerPojo = new EntitySerializerPojo();

        entitySerializerPojo.setPersistenceId(Long.MAX_VALUE);
        entitySerializerPojo.setPersistenceVersion(1L);
        entitySerializerPojo.setABoolean(Boolean.TRUE);
        entitySerializerPojo.setADate(new Date(123L));
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

    @Test
    public void testSerialize() throws Exception {

    }

    @Test
    public void testHandledType() throws Exception {

    }
}
