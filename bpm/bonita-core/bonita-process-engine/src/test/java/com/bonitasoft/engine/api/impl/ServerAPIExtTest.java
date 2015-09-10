/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.bonitasoft.engine.exception.LicenseErrorException;
import com.bonitasoft.manager.ManagerIllegalStateException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.service.APIAccessResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServerAPIExtTest {

    @Mock
    private APIAccessResolver apiAccessResolver;

    private ServerAPIExt serverAPIExt;

    @Before
    public void setUp() throws Exception {
        serverAPIExt = new ServerAPIExt(true, apiAccessResolver);
    }

    @Test
    public void should_use_LicenseErrorException_when_is_ManagerIllegalStateManager() throws Exception {

        //when
        ManagerIllegalStateException cause = new ManagerIllegalStateException("error");
        BonitaRuntimeException wrappedException = serverAPIExt.wrapThrowable(cause);

        //then
        assertThat(wrappedException).isInstanceOf(LicenseErrorException.class);
        assertThat(wrappedException.getCause()).isNull();
        assertThat(wrappedException.getMessage()).isEqualTo("error");
        assertThat(wrappedException.getStackTrace()).isEmpty();
    }

    @Test
    public void should_use_BonitaRuntimeException_when_is_not_ManagerIllegalStateManager() throws Exception {

        //when
        Exception cause = new Exception("error");
        BonitaRuntimeException wrappedException = serverAPIExt.wrapThrowable(cause);

        //then
        assertThat(wrappedException).isInstanceOf(BonitaRuntimeException.class);
        assertThat(wrappedException.getCause()).isEqualTo(cause);
        assertThat(wrappedException.getStackTrace()).isNotEmpty();
    }

}