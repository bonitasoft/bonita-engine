/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class LicenseErrorExceptionTest {

    @Test
    public void should_not_keep_stack_trace() throws Exception {
        //given
        LicenseErrorException exception = new LicenseErrorException("error");

        //then
        assertThat(exception).hasMessage("error");
        assertThat(exception.getStackTrace()).isEmpty();
    }

}