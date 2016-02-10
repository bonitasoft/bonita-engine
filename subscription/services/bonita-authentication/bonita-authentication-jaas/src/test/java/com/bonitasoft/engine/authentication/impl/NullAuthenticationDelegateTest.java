/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
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
