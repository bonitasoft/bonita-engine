/**
 * Copyright (C) 2017 Bonitasoft S.A.
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

package org.bonitasoft.engine.bdm.serialization;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.bdm.serialization.model.Invoice;
import org.junit.Test;

public class BusinessDataObjectMapperTest {

    private BusinessDataObjectMapper objectMapper = new BusinessDataObjectMapper();

    @Test
    public void should_serialize_object_with_custom_local_date_and_time_serializers() throws Exception {
        // given:
        Invoice invoice = new Invoice();
        invoice.setCustomerName("Bonitasoft");
        invoice.setDate(LocalDate.of(2018, 8, 15));
        invoice.setComments(null);

        // when:
        StringWriter writer = new StringWriter();
        objectMapper.writeValue(writer, invoice);

        // then:
        assertThatJson(writer.toString()).as("Serialization uses date and time custom serializers")
                .isEqualTo(getJsonContent("simpleInvoice.json"));
    }


    private static String getJsonContent(String jsonFileName) throws IOException {
        return new String(IOUtils.toByteArray(BusinessDataObjectMapperTest.class.getResourceAsStream(jsonFileName)));
    }



}