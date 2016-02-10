/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.business.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.business.application.ApplicationField;
import org.junit.Test;

public class ApplicationCreatorExtTest {

    @Test
    public void using_constructor_containing_only_mandatory_info_should_set_madatory_info() throws Exception {
        //given
        ApplicationCreatorExt creator = new ApplicationCreatorExt("token", "display name", "7.0");

        //when
        Map<org.bonitasoft.engine.business.application.ApplicationField, Serializable> fields = creator.getFields();

        //then
        assertThat(fields).hasSize(3);
        assertThat(fields.get(ApplicationField.TOKEN)).isEqualTo("token");
        assertThat(fields.get(ApplicationField.DISPLAY_NAME)).isEqualTo("display name");
        assertThat(fields.get(ApplicationField.VERSION)).isEqualTo("7.0");
    }

    @Test
    public void using_constructor_containing_layout_and_theme_should_also_set_layout_and_theme() throws Exception {
        //given
        ApplicationCreatorExt creator = new ApplicationCreatorExt("token", "display name", "7.0", 50L, 100L);

        //when
        Map<org.bonitasoft.engine.business.application.ApplicationField, Serializable> fields = creator.getFields();

        //then
        assertThat(fields).hasSize(5);
        assertThat(fields.get(ApplicationField.TOKEN)).isEqualTo("token");
        assertThat(fields.get(ApplicationField.DISPLAY_NAME)).isEqualTo("display name");
        assertThat(fields.get(ApplicationField.VERSION)).isEqualTo("7.0");
        assertThat(fields.get(ApplicationField.LAYOUT_ID)).isEqualTo(50L);
        assertThat(fields.get(ApplicationField.THEME_ID)).isEqualTo(100L);
    }

}