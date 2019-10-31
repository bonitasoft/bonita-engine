package org.bonitasoft.engine.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
        "org.bonitasoft.engine.configuration.tenant",
        "org.bonitasoft.engine.tenant"
        })
public class EngineTenantConfiguration {

    //Add here tenant beans instead of adding it in xml file

}
