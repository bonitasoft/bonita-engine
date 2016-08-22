package org.bonitasoft.platform.configuration.model;

import org.junit.Test;

/**
 * @author Laurent Leseigneur
 */
public class BonitaConfigurationTest {

    @Test
    public void should_build_configuration() {
        //given
        BonitaConfiguration bonitaConfiguration = new BonitaConfiguration("my resource", "my content".getBytes());

        //then
        BonitaConfigurationAssert.assertThat(bonitaConfiguration)
                .hasResourceName("my resource")
                .hasResourceContent("my content".getBytes());

    }

}
