/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * @author Romain Bioteau
 */
public class PersistenceUnitBuilderTest {

    private PersistenceUnitBuilder builder;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        builder = new PersistenceUnitBuilder();
    }

    @Test
    public void shouldDone_WhenNothingIsCalledReturnsDefaultPersistenceUnits() {
        Document document = builder.done();
        assertThat(document).isNotNull();
        assertThat(document.getElementsByTagName("persistence-unit").getLength()).isEqualTo(1);
        assertThat(document.getElementsByTagName("class").getLength()).isEqualTo(0);
    }

    @Test
    public void shouldAddClass_AddOneClassTagByClassname() throws Exception {
        builder.addClass("org.bonitasoft.businessdata.Employee").addClass("org.bonitasoft.businessdata.LeaveRequesst");
        Document document = builder.done();
        assertThat(document.getElementsByTagName("class").getLength()).isEqualTo(2);
        NodeList nodeList = document.getElementsByTagName("class");
        Set<String> classnames = new HashSet<String>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            classnames.add(nodeList.item(i).getTextContent());
        }
        assertThat(classnames).containsOnly("org.bonitasoft.businessdata.Employee", "org.bonitasoft.businessdata.LeaveRequesst");
    }

}
