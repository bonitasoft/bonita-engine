package org.bonitasoft.engine.spring.autoconfigure.properties;

import org.bonitasoft.engine.BonitaDatabaseConfiguration;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

public class BonitaDatabasesConfiguration {

    @NestedConfigurationProperty
    private BonitaDatabaseConfiguration bonita = new BonitaDatabaseConfiguration();
    @NestedConfigurationProperty
    private BonitaDatabaseConfiguration businessData = new BonitaDatabaseConfiguration();


    public BonitaDatabaseConfiguration getBonita() {
        return bonita;
    }

    public BonitaDatabaseConfiguration getBusinessData() {
        return businessData;
    }
}
