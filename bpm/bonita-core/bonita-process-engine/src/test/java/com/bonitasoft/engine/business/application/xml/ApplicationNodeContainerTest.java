/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.xml;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.Test;

public class ApplicationNodeContainerTest {

    @Test
    public void getApplications_should_return_empty_list_when_has_no_elements() throws Exception {
        //given
        ApplicationNodeContainer container = new ApplicationNodeContainer();

        //when
        List<ApplicationNode> applications = container.getApplications();

        //then
        assertThat(applications).isEmpty();
    }
}