/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.datastore.applicationpage;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.business.application.ApplicationPageSearchDescriptor;
import org.bonitasoft.web.rest.model.applicationpage.ApplicationPageItem;
import org.junit.Test;

public class ApplicationPageSearchDescriptorConverterTest {

    private final ApplicationPageSearchDescriptorConverter converter = new ApplicationPageSearchDescriptorConverter();

    @Test
    public void should_return_ApplicationPageSearchDescriptor_id_on_convert_attribute_id() throws Exception {
        //when
        final String convertedValue = converter.convert(ApplicationPageItem.ATTRIBUTE_ID);

        //then
        assertThat(convertedValue).isEqualTo(ApplicationPageSearchDescriptor.ID);
    }

    @Test
    public void should_return_ApplicationPageSearchDescriptor_name_on_convert_attribute_name() throws Exception {
        //when
        final String convertedValue = converter.convert(ApplicationPageItem.ATTRIBUTE_TOKEN);

        //then
        assertThat(convertedValue).isEqualTo(ApplicationPageSearchDescriptor.TOKEN);
    }

    @Test
    public void should_return_ApplicationPageSearchDescriptor_applicationId_on_convert_attribute_applicationId()
            throws Exception {
        //when
        final String convertedValue = converter.convert(ApplicationPageItem.ATTRIBUTE_APPLICATION_ID);

        //then
        assertThat(convertedValue).isEqualTo(ApplicationPageSearchDescriptor.APPLICATION_ID);
    }

    @Test
    public void should_return_ApplicationPageSearchDescriptor_pageId_on_convert_attribute_pageId() throws Exception {
        //when
        final String convertedValue = converter.convert(ApplicationPageItem.ATTRIBUTE_PAGE_ID);

        //then
        assertThat(convertedValue).isEqualTo(ApplicationPageSearchDescriptor.PAGE_ID);
    }

}
