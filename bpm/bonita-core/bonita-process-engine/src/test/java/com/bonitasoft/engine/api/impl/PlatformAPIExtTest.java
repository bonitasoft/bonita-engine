/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import static org.mockito.Mockito.when;

import org.bonitasoft.engine.exception.CreationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.service.impl.LicenseChecker;

@RunWith(MockitoJUnitRunner.class)
public class PlatformAPIExtTest {

    @Mock
    private LicenseChecker checker;

    @InjectMocks
    private PlatformAPIExt platformAPI;

    @Test(expected = CreationException.class)
    public void createPlatform_should_throw_an_exception_if_the_license_is_invalid() throws Exception {
        when(checker.checkLicense()).thenReturn(false);

        platformAPI.createPlatform();
    }

}
