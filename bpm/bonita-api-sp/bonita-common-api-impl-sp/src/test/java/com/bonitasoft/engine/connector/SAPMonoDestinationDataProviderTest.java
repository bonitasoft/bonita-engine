/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.connector;

import static org.junit.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.sap.conn.jco.ext.Environment;

/**
 * 
 * @author Aurelien Pupier
 * @author Baptiste Mesta
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Environment.class })
public class SAPMonoDestinationDataProviderTest {

    @Before
    public void init() {
        mockStatic(Environment.class);
        SAPMonoDestinationDataProvider.clear();
    }

    @Test(expected = IllegalStateException.class)
    public void testMultipleDifferentDestination() throws IllegalStateException {
        try {
            SAPMonoDestinationDataProvider.getInstance("name1");
        } catch (IllegalStateException e) {
            e.printStackTrace();
            fail("The first connection with name1 has failed!");
        }
        SAPMonoDestinationDataProvider.getInstance("name2");
    }

    @Test
    public void testMultipleSimilarDestination() throws IllegalStateException {
        SAPMonoDestinationDataProvider.getInstance("nameSimilar");
        SAPMonoDestinationDataProvider.getInstance("nameSimilar");
    }

    @After
    public void tearDown() {
        SAPMonoDestinationDataProvider.clear();
    }
}
