/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.service.platform;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class PlatformInformationProviderTest {

    private PlatformInformationProvider provider;

    @Before
    public void setUp() throws Exception {
        provider = new PlatformInformationProvider();

    }

    @Test
    public void getAndReset_should_return_zero_when_no_element_was_registered() throws Exception {
        assertThat(provider.getAndReset()).isEqualTo(0);
    }

    @Test
    public void getAndReset_should_return_number_of_registered_elements_and_reset_to_zero() throws Exception {
        //given
        //register two elements
        provider.register();
        provider.register();

        //then
        //must return number of register elements
        assertThat(provider.getAndReset()).isEqualTo(2);
        //no elements registered after reset, so should return zero
        assertThat(provider.getAndReset()).isEqualTo(0);

        //given
        //register one more element
        provider.register();

        //then
        assertThat(provider.getAndReset()).isEqualTo(1);
    }

    @Test
    public void get_should_return_zero_when_no_element_was_registered() throws Exception {
        assertThat(provider.get()).isEqualTo(0);
    }

    @Test
    public void get_should_return_number_of_registered_elements() throws Exception {
        //given
        //register two elements
        provider.register();
        provider.register();

        //then
        //must return number of register elements
        assertThat(provider.get()).isEqualTo(2);
        //subsequent calls should return the same result
        assertThat(provider.get()).isEqualTo(2);

        //given
        //register one more element
        provider.register();

        //then
        assertThat(provider.get()).isEqualTo(3);
    }

}