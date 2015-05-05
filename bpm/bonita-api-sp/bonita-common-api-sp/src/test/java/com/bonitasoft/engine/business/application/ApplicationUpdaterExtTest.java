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

import org.bonitasoft.engine.business.application.ApplicationField;
import org.junit.Test;

public class ApplicationUpdaterExtTest {

    @Test
    public void setLayoutId_should_put_layout_in_fields() throws Exception {
        //given
        ApplicationUpdaterExt updater = new ApplicationUpdaterExt();

        //when
        updater.setLayoutId(7L);

        //then
        assertThat(updater.getFields()).hasSize(1);
        assertThat(updater.getFields().get(ApplicationField.LAYOUT_ID)).isEqualTo(7L);
    }

    @Test
    public void setThemeId_should_put_theme_in_fields() throws Exception {
        //given
        ApplicationUpdaterExt updater = new ApplicationUpdaterExt();

        //when
        updater.setThemeId(15L);

        //then
        assertThat(updater.getFields()).hasSize(1);
        assertThat(updater.getFields().get(ApplicationField.THEME_ID)).isEqualTo(15L);
    }

}