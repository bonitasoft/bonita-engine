/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.business.application.impl.ApplicationPageImpl;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.business.application.ApplicationPage;

public class ApplicationPageConverterTest {

    private ApplicationPageConverter converter;

    @Before
    public void setUp() throws Exception {
        converter = new ApplicationPageConverter();
    }

    @Test
    public void convert_should_set_all_fields() throws Exception {
        //given
        int applicationId = 1;
        int pageId = 2;
        ApplicationPageImpl toConvert = new ApplicationPageImpl(applicationId, pageId, "page");
        toConvert.setId(34);

        //when
        ApplicationPage convertedPage = converter.convert(toConvert);

        //then
        assertThat(convertedPage).isNotNull();
        assertThat(convertedPage.getApplicationId()).isEqualTo(applicationId);
        assertThat(convertedPage.getPageId()).isEqualTo(pageId);
        assertThat(convertedPage.getToken()).isEqualTo("page");
        assertThat(convertedPage.getId()).isEqualTo(34);
    }
}
