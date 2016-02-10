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
package org.bonitasoft.engine.api.impl.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.business.application.ApplicationPage;
import org.bonitasoft.engine.business.application.impl.ApplicationPageImpl;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.impl.SApplicationPageImpl;
import org.junit.Test;


public class ApplicationPageModelConverterTest {

    private static final long ID = 11;
    private static final long APPLICATION_ID = 20;
    private static final long PAGE_ID = 30;
    private static final String APP_PAGE_NAME = "firstPage";
    private final ApplicationPageModelConverter convertor = new ApplicationPageModelConverter();

    @Test
    public void toApplicationPage_should_map_all_server_fields() throws Exception {
        //given
        final SApplicationPageImpl sAppPage = new SApplicationPageImpl(APPLICATION_ID, PAGE_ID, APP_PAGE_NAME);
        sAppPage.setId(ID);

        //when
        final ApplicationPage appPage = convertor.toApplicationPage(sAppPage);

        //then
        assertThat(appPage).isNotNull();
        assertThat(appPage.getId()).isEqualTo(ID);
        assertThat(appPage.getApplicationId()).isEqualTo(APPLICATION_ID);
        assertThat(appPage.getPageId()).isEqualTo(PAGE_ID);
        assertThat(appPage.getToken()).isEqualTo(APP_PAGE_NAME);
    }

    @Test
    public void toApplicationPageList_should_call_toApplitionPage_for_each_element_and_return_the_list_of_converted_values() throws Exception {
        //given
        final SApplicationPageImpl sAppPage1 = new SApplicationPageImpl(10, 21, "appPage1");
        final SApplicationPageImpl sAppPage2 = new SApplicationPageImpl(10, 21, "appPage2");
        final ApplicationPageImpl appPage1 = new ApplicationPageImpl(10, 21, "appPage1");
        final ApplicationPageImpl appPage2 = new ApplicationPageImpl(10, 21, "appPage2");
        final ApplicationPageModelConverter convertorMock = spy(convertor);
        doReturn(appPage1).when(convertorMock).toApplicationPage(sAppPage1);
        doReturn(appPage2).when(convertorMock).toApplicationPage(sAppPage2);

        //when
        final List<ApplicationPage> applicationPages = convertorMock.toApplicationPage(Arrays.<SApplicationPage> asList(sAppPage1, sAppPage2));

        //then
        assertThat(applicationPages).containsExactly(appPage1, appPage2);
    }

}
