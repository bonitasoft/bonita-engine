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
package org.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import org.bonitasoft.engine.bdm.builder.BusinessObjectModelBuilder;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.io.IOUtils;

public class BusinessObjectModelConverterTest {

    @Test
    public void zipThenUnzipBOMShouldReturnTheOriginalBOM() throws Exception {
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        final BusinessObjectModel bom = new BusinessObjectModelBuilder().buildDefaultBOM();
        final byte[] zip = convertor.zip(bom);
        final BusinessObjectModel actual = convertor.unzip(zip);

        assertThat(actual).isEqualTo(bom);
    }

    @Test
    public void zipThenUnzipBOMShouldReturnTheOriginalBOMWithUniqueConstraint() throws Exception {
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        final BusinessObjectModel bom = new BusinessObjectModelBuilder().buildBOMWithUniqueConstraint();
        final byte[] zip = convertor.zip(bom);
        final BusinessObjectModel actual = convertor.unzip(zip);

        assertThat(actual).isEqualTo(bom);
    }

    @Test
    public void zipThenUnzipBOMShouldReturnTheOriginalBOMWithQuery() throws Exception {
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        final BusinessObjectModel bom = new BusinessObjectModelBuilder().buildBOMWithQuery();
        final byte[] zip = convertor.zip(bom);
        final BusinessObjectModel actual = convertor.unzip(zip);

        assertThat(actual).isEqualTo(bom);
    }

    @Test(expected = JAXBException.class)
    public void zipAnBOMWithAnEmptyShouldThrowAnException() throws Exception {
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        final BusinessObjectModel bom = new BusinessObjectModelBuilder().buildBOMWithAnEmptyEntity();
        convertor.zip(bom);
    }

    @Test(expected = JAXBException.class)
    public void zipAnBOMWithAnEmptyFieldShouldThrowAnException() throws Exception {
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        final BusinessObjectModel bom = new BusinessObjectModelBuilder().buildBOMWithAnEmptyField();
        convertor.zip(bom);
    }

    @Test(expected = IOException.class)
    public void unzipADifferentZipThrowAnException() throws Exception {
        final byte[] zip = IOUtils.zip("bonita", "bpm".getBytes());
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        convertor.unzip(zip);
    }

    @Test
    public void should_be_backward_compatible() throws Exception {
        byte[] xml = org.apache.commons.io.IOUtils.toByteArray(BusinessObjectModelConverterTest.class.getResourceAsStream("/bom_6.3.0.xml"));
        BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        convertor.unmarshall(xml);
        // expect no unmarshalling exception
    }

    public void zipThenUnzipBOMShouldReturnTheOriginalBOMWithIndex() throws Exception {
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        final BusinessObjectModel bom = new BusinessObjectModelBuilder().buildBOMWithIndex();
        final byte[] zip = convertor.zip(bom);
        final BusinessObjectModel actual = convertor.unzip(zip);

        assertThat(actual).isEqualTo(bom);
    }

}
