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

import org.bonitasoft.engine.business.application.impl.ApplicationMenuImpl;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.business.application.ApplicationMenu;

public class ApplicationMenuConverterTest {

    private ApplicationMenuConverter converter;

    @Before
    public void setUp() throws Exception {
        converter = new ApplicationMenuConverter();

    }

    @Test
    public void convert_should_map_all_fields() throws Exception {
        //given
        long applicationId = 4;
        long pageId = 50;
        int index = 2;
        long id = 10L;
        long parentId = 25L;
        ApplicationMenuImpl toConvert = new ApplicationMenuImpl("main", applicationId, pageId, index);
        toConvert.setId(id);
        toConvert.setParentId(parentId);

        //when
        ApplicationMenu convertedMenu = converter.convert(toConvert);

        //then
        assertThat(convertedMenu).isNotNull();
        assertThat(convertedMenu.getDisplayName()).isEqualTo("main");
        assertThat(convertedMenu.getApplicationPageId()).isEqualTo(pageId);
        assertThat(convertedMenu.getApplicationId()).isEqualTo(applicationId);
        assertThat(convertedMenu.getParentId()).isEqualTo(parentId);
        assertThat(convertedMenu.getIndex()).isEqualTo(index);
        assertThat(convertedMenu.getId()).isEqualTo(id);

    }

}
