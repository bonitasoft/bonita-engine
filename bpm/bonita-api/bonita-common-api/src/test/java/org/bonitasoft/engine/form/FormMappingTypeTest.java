
/*******************************************************************************
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
 ******************************************************************************/

package org.bonitasoft.engine.form;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.bonitasoft.engine.bpm.bar.form.model.FormMappingDefinition;
import org.bonitasoft.engine.bpm.bar.form.model.FormMappingModel;
import org.junit.Test;

/**
 * author Emmanuel Duchastenier
 */
public class FormMappingTypeTest {

    @Test
    public void getTypeFromIdShouldReturnProperEnumValue() throws JAXBException {
        FormMappingModel formMappingModel = new FormMappingModel();
        formMappingModel.addFormMapping(new FormMappingDefinition("lala", null, null));
        /*
         * Employee_actor.addActor(new Actor("lala"));
         * Employee_actor.addActor(new Actor("tralala"));
         * Employee_actor.getActors().get(0).addUser("william.jobs");
         * Employee_actor.getActors().get(0).addGroup("RD");
         * Employee_actor.getActors().get(0).addRole("dev");
         * Employee_actor.getActors().get(1).addUser("lala.ru");
         */
        StringWriter result = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(FormMappingModel.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(formMappingModel, result);
        String result2 = result.toString();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object formMappingResult = unmarshaller.unmarshal(new StringReader(result2));
        assertThat(formMappingModel).isEqualTo(formMappingResult);
        /*
         * assertThat(FormMappingType.getTypeFromId(1)).isEqualTo(FormMappingType.PROCESS_START);
         * assertThat(FormMappingType.getTypeFromId(2)).isEqualTo(FormMappingType.PROCESS_OVERVIEW);
         * assertThat(FormMappingType.getTypeFromId(3)).isEqualTo(FormMappingType.TASK);
         */
    }
}
