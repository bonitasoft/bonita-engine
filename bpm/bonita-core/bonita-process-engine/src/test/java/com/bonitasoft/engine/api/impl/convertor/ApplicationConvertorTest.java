package com.bonitasoft.engine.api.impl.convertor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.bonitasoft.engine.business.application.Application;
import com.bonitasoft.engine.business.application.ApplicationCreator;
import com.bonitasoft.engine.business.application.SApplication;
import com.bonitasoft.engine.business.application.impl.SApplicationImpl;


public class ApplicationConvertorTest {

    private static final int TENANT_ID = 1;
    static final int ID = 11;
    private static final String APP_DESC = "app desc";
    private static final String APP_PATH = "/app";
    private static final String APP_VERSION = "1.0";
    private static final String APP_NAME = "app";
    private final ApplicationConvertor convertor = new ApplicationConvertor();

    @Test
    public void buildSApplication_should_map_all_information_from_creator() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator(APP_NAME, APP_VERSION, APP_PATH);
        creator.setDescription(APP_DESC);
        creator.setIconPath("/icon.jpg");
        final long userId = 10;

        //when
        final SApplication application = convertor.buildSApplication(creator, userId);

        //then
        assertThat(application).isNotNull();
        assertThat(application.getName()).isEqualTo(APP_NAME);
        assertThat(application.getVersion()).isEqualTo(APP_VERSION);
        assertThat(application.getPath()).isEqualTo(APP_PATH);
        assertThat(application.getDescription()).isEqualTo(APP_DESC);
    }

    @Test
    public void toAppplication_must_map_all_server_fields() throws Exception {
        //given
        final SApplicationImpl sApp = new SApplicationImpl(APP_NAME, APP_VERSION, APP_PATH);
        sApp.setDescription(APP_DESC);
        sApp.setId(ID);
        sApp.setTenantId(TENANT_ID);

        //when
        final Application application = convertor.toApplication(sApp);

        //then
        assertThat(application).isNotNull();
        assertThat(application.getId()).isEqualTo(ID);
        assertThat(application.getName()).isEqualTo(APP_NAME);
        assertThat(application.getVersion()).isEqualTo(APP_VERSION);
        assertThat(application.getPath()).isEqualTo(APP_PATH);
        assertThat(application.getDescription()).isEqualTo(APP_DESC);

    }

}
