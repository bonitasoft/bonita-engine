package com.bonitasoft.engine.authentication.impl;

import static org.assertj.core.api.Assertions.*;

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
    public void testAuthenticateShouldReturnSameMapNotAltered() throws Exception {
        Map<String, Serializable> credentials = Collections.unmodifiableMap(new HashMap<String, Serializable>());
        assertThat(nullAuthenticationDelegate.authenticate(credentials)).isEqualTo(credentials);
    }

}
