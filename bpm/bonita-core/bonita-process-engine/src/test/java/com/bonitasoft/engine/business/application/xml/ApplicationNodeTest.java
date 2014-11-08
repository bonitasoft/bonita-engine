/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

public class ApplicationNodeTest {

    @Test
    public void getApplicationPages_should_return_empty_list_when_no_elements_were_added() throws Exception {
        //given
        ApplicationNode applicationNode = new ApplicationNode();

        //when
        List<ApplicationPageNode> applicationPages = applicationNode.getApplicationPages();

        //then
        assertThat(applicationPages).isEmpty();
    }

    @Test
    public void getApplicationMenus_should_return_empty_list_when_no_elements_were_added() throws Exception {
        //given
        ApplicationNode applicationNode = new ApplicationNode();

        //when
        List<ApplicationMenuNode> applicationMenus = applicationNode.getApplicationMenus();

        //then
        assertThat(applicationMenus).isEmpty();
    }
}
