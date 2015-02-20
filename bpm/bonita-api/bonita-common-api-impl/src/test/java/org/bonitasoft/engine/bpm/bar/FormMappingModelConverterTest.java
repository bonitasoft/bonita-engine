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
import static org.bonitasoft.engine.bpm.bar.formmapping.builder.FormMappingBuilder.aFormMapping;
import static org.bonitasoft.engine.bpm.bar.formmapping.builder.FormMappingModelBuilder.aFormMappingModel;

import javax.xml.bind.JAXBException;

import org.bonitasoft.engine.bpm.bar.formmapping.builder.FormMappingModelBuilder;
import org.bonitasoft.engine.bpm.bar.formmapping.model.FormMappingModel;
import org.junit.Test;

public class FormMappingModelConverterTest {

    @Test
    public void serializeThenDeserializeModelShouldReturnTheOriginalModel() throws Exception {
        final FormMappingModelConverter convertor = new FormMappingModelConverter();
        final FormMappingModel model = new FormMappingModelBuilder().buildDefaultModelWithOneFormMapping();
        final byte[] serialize = convertor.serializeToXML(model);
        final FormMappingModel actual = convertor.deserializeFromXML(serialize);

        assertThat(actual).isEqualTo(model);
    }

    @Test
    public void serializeThenDeserializeEmptyModelShouldReturnTheOriginalEmptyModel() throws Exception {
        final FormMappingModelConverter convertor = new FormMappingModelConverter();
        final FormMappingModel model = new FormMappingModelBuilder().buildEmptyDefaultModel();
        final byte[] serialize = convertor.serializeToXML(model);
        final FormMappingModel actual = convertor.deserializeFromXML(serialize);

        assertThat(actual).isEqualTo(model);
    }

    @Test
    public void serializeThenDeserializeModelWithoutTaskNameShouldReturnTheOriginalModel() throws Exception {
        final FormMappingModelConverter convertor = new FormMappingModelConverter();
        final FormMappingModel model = aFormMappingModel().withFormMapping(aFormMapping("page", null, true).build()).build();
        final byte[] serialize = convertor.serializeToXML(model);
        final FormMappingModel actual = convertor.deserializeFromXML(serialize);

        assertThat(actual).isEqualTo(model);
    }

    @Test(expected = JAXBException.class)
    public void deserializeADifferentModelShouldThrowAnException() throws Exception {
        final byte[] serialize = "<someXML />".getBytes();
        final FormMappingModelConverter convertor = new FormMappingModelConverter();
        convertor.deserializeFromXML(serialize);
    }

    //    @Test
    //    public void should_be_backward_compatible() throws Exception {
    //        final byte[] xml = org.apache.commons.io.IOUtils.toByteArray(FormMappingModelConverterTest.class.getResourceAsStream("/form-mapping_7.0.0.xml"));
    //        final FormMappingModelConverter convertor = new FormMappingModelConverter();
    //        convertor.unmarshall(xml);
    //        // expect no unmarshalling exception
    //    }

}
