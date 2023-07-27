/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBException;

import org.bonitasoft.engine.bdm.builder.BusinessObjectModelBuilder;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.junit.Test;

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

    @Test
    public void unzipADifferentZipThrowAnException() throws Exception {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try (final ZipOutputStream zos = new ZipOutputStream(stream)) {
            zos.putNextEntry(new ZipEntry("bonita"));
            zos.write("bpm".getBytes());
        }
        final byte[] zip = stream.toByteArray();
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        assertThatExceptionOfType(IOException.class).isThrownBy(() -> convertor.unzip(zip))
                .withMessage("the file bom.xml is missing in the zip");
    }

    @Test
    public void should_be_backward_compatible() throws Exception {
        try (var resource = BusinessObjectModelConverterTest.class.getResourceAsStream("/bom_6.3.0.xml")) {
            assertThat(resource).isNotNull();
            final byte[] xml = resource.readAllBytes();
            final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
            final BusinessObjectModel bom = convertor.unmarshall(xml);
            // expect no unmarshalling exception

            assertThat(bom.getModelVersion()).isNullOrEmpty();
            assertThat(bom.getProductVersion()).isNullOrEmpty();
        }
    }

    @Test
    public void should_unmarshall_bom_with_namespace() throws Exception {
        try (var resource = BusinessObjectModelConverterTest.class.getResourceAsStream("/bom_7.11.0.xml")) {
            assertThat(resource).isNotNull();
            final byte[] xml = resource.readAllBytes();
            final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
            final BusinessObjectModel bom = convertor.unmarshall(xml);
            // expect no unmarshalling exception

            assertThat(bom.getModelVersion()).isEqualTo("1.0");
            assertThat(bom.getProductVersion()).isEqualTo("7.11.0");
        }
    }

    @Test
    public void should_add_namespace_when_missing() throws Exception {
        try (var resource = BusinessObjectModelConverterTest.class.getResourceAsStream("/bom_6.3.0.xml")) {
            assertThat(resource).isNotNull();
            byte[] xml = resource.readAllBytes();
            String str = new String(xml);
            assertThat(str).doesNotContain("xmlns=\"http://documentation.bonitasoft.com/bdm-xml-schema/1.0\"");

            BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
            BusinessObjectModel bom = convertor.unmarshall(xml);

            xml = convertor.marshall(bom);
            str = new String(xml);
            assertThat(str).contains("xmlns=\"http://documentation.bonitasoft.com/bdm-xml-schema/1.0\"");
        }
    }

    @Test
    public void should_be_backward_compatible_with_version() throws Exception {
        try (var resource = BusinessObjectModelConverterTest.class.getResourceAsStream("/bom_7.2.0.xml")) {
            assertThat(resource).isNotNull();
            final byte[] xml = resource.readAllBytes();
            final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
            final BusinessObjectModel bom = convertor.unmarshall(xml);
            // expect no unmarshalling exception

            assertThat(bom.getModelVersion()).isEqualTo("1.0");
            assertThat(bom.getProductVersion()).isEqualTo("7.2.0");

            final BusinessObjectModel unmarshallBom = convertor.unmarshall(convertor.marshall(bom));

            assertThat(unmarshallBom.getModelVersion()).isEqualTo("1.0");
            assertThat(unmarshallBom.getProductVersion()).isEqualTo("7.2.0");
        }
    }

    @Test
    public void should_set_current_model_version_when_marshalling() throws Exception {
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        final BusinessObjectModel bom = new BusinessObjectModelBuilder().buildBOMWithQuery();
        assertThat(bom.getModelVersion()).isNullOrEmpty();
        assertThat(bom.getProductVersion()).isNullOrEmpty();
        final BusinessObjectModel unmarshalledBom = convertor.unmarshall(convertor.marshall(bom));

        assertThat(unmarshalledBom.getModelVersion()).isEqualTo(BusinessObjectModel.CURRENT_MODEL_VERSION);
        assertThat(bom.getProductVersion()).isNotNull().isNotEmpty();
    }

    @Test
    public void zipThenUnzipBOMShouldReturnTheOriginalBOMWithIndex() throws Exception {
        final BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        final BusinessObjectModel bom = new BusinessObjectModelBuilder().buildBOMWithIndex();
        final byte[] zip = convertor.zip(bom);
        final BusinessObjectModel actual = convertor.unzip(zip);

        assertThat(actual).isEqualTo(bom);
    }

}
