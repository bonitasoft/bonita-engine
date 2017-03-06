package org.bonitasoft.engine.business.data.impl.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author Laurent Leseigneur
 */
public class JsonNumberSerializerHelperTest {

    // This fields are used by reflection in the test:
    public Long longField;
    public Double doubleField;
    public Float floatField;
    public Integer intField;
    public String stringField;

    public List<Long> longList;
    public List<Double> doubleList;
    public List<Float> floatList;
    public List<Integer> intList;
    public List<String> stringList;

    @Test
    public void should_check_is_string_representation_is_needed() throws Exception {
        //given
        JsonNumberSerializerHelper jsonNumberSerializerHelper = new JsonNumberSerializerHelper();
        String[] fieldsThatNeedStringRepresentation = { "long", "double", "float" };
        String[] fieldsThatDoNotNeedStringRepresentation = { "int", "string" };

        //then
        for (String field : fieldsThatNeedStringRepresentation) {
            checkStringRepresentation(jsonNumberSerializerHelper, field, true);
        }

        for (String field : fieldsThatDoNotNeedStringRepresentation) {
            checkStringRepresentation(jsonNumberSerializerHelper, field, false);
        }

    }

    @Test
    public void should_convert_list_to_string() throws Exception {
        //given
        JsonNumberSerializerHelper jsonNumberSerializerHelper = new JsonNumberSerializerHelper();
        List<Long> longList = new ArrayList<>();
        longList.add(1L);
        longList.add(2L);

        //when
        final List<String> strings = jsonNumberSerializerHelper.convertToStringList(longList);

        //then
        assertThat(strings).containsExactly("1", "2");

    }

    @Test
    public void should_convert_to_string_list_return_null() throws Exception {
        //given
        JsonNumberSerializerHelper jsonNumberSerializerHelper = new JsonNumberSerializerHelper();

        //when
        final List<String> strings = jsonNumberSerializerHelper.convertToStringList(null);

        //then
        assertThat(strings).isNotNull().isEmpty();

    }

    @Test
    public void should_convert_to_string() throws Exception {
        //given
        JsonNumberSerializerHelper jsonNumberSerializerHelper = new JsonNumberSerializerHelper();

        //then
        assertThat(jsonNumberSerializerHelper.convertToString(null)).isNull();
        assertThat(jsonNumberSerializerHelper.convertToString(5L)).isEqualTo("5");

    }

    private void checkStringRepresentation(JsonNumberSerializerHelper jsonNumberSerializerHelper, String fieldName, boolean expectedResult)
            throws NoSuchFieldException {
        assertThat(jsonNumberSerializerHelper.shouldAddStringRepresentationForNumber(this.getClass().getField(fieldName + "Field"))).isEqualTo(expectedResult);
        assertThat(jsonNumberSerializerHelper.shouldAddStringRepresentationForNumberList(this.getClass().getField(fieldName + "Field"))).isFalse();
        assertThat(jsonNumberSerializerHelper.shouldAddStringRepresentationForNumberList(this.getClass().getField(fieldName + "List"))).isEqualTo(
                expectedResult);
    }
}
