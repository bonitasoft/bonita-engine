/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.importer;

import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.exception.ExecutionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.application.model.SApplication;

@RunWith(MockitoJUnitRunner.class)
public class FailOnDuplicateApplicationImportStrategyTest {

    @InjectMocks
    private FailOnDuplicateApplicationImportStrategy strategy;

    @Test(expected = ExecutionException.class)
    public void whenApplicationExists_shold_throw() throws Exception {
        //given
        SApplication existingApplication = mock(SApplication.class);
        SApplication toBeImported = mock(SApplication.class);

        //when
        strategy.whenApplicationExists(existingApplication, toBeImported);

        //then exception
    }
}
