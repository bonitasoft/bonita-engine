/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.authentication.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.Spy;

public class NullAuthenticationDelegateTest {

    @Spy
    NullAuthenticationDelegate nullAuthenticationDelegate = new NullAuthenticationDelegate();

    @Test
    public void testAuthenticateShouldReturnSameMapNotAltered() {
        Map<String, Serializable> credentials = Collections.unmodifiableMap(new HashMap<String, Serializable>());
        assertThat(nullAuthenticationDelegate.authenticate(credentials)).isEqualTo(credentials);
    }

}
