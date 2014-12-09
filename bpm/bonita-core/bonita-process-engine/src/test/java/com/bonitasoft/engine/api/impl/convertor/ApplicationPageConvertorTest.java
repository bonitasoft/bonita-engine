package com.bonitasoft.engine.api.impl.convertor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.bonitasoft.engine.business.application.ApplicationPage;
import com.bonitasoft.engine.business.application.impl.ApplicationPageImpl;
import com.bonitasoft.engine.business.application.model.SApplicationPage;
import com.bonitasoft.engine.business.application.model.impl.SApplicationPageImpl;


public class ApplicationPageConvertorTest {

    private static final long ID = 11;
    private static final long APPLICATION_ID = 20;
    private static final long PAGE_ID = 30;
    private static final String APP_PAGE_NAME = "firstPage";
    private final ApplicationPageConvertor convertor = new ApplicationPageConvertor();

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
        final ApplicationPageConvertor convertorMock = spy(convertor);
        doReturn(appPage1).when(convertorMock).toApplicationPage(sAppPage1);
        doReturn(appPage2).when(convertorMock).toApplicationPage(sAppPage2);

        //when
        final List<ApplicationPage> applicationPages = convertorMock.toApplicationPage(Arrays.<SApplicationPage> asList(sAppPage1, sAppPage2));

        //then
        assertThat(applicationPages).containsExactly(appPage1, appPage2);
    }

}
