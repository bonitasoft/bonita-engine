/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package org.bonitasoft.engine.business.application.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

public class ApplicationMenuNodeTest {

    @Test
    public void getApplicationMenus_should_return_empty_list_when_no_elements_were_added() throws Exception {
        //given
        ApplicationMenuNode menuNode = new ApplicationMenuNode();

        //when
        List<ApplicationMenuNode> applicationMenus = menuNode.getApplicationMenus();

        //then
        assertThat(applicationMenus).isEmpty();
    }
}
