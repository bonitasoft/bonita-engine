package com.bonitasoft.engine.licence;

import org.apache.commons.log.ConfigurationError;
import org.junit.Assert;
import org.junit.Test;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;

public class LicenseTest {

    @Test
    public void validateLicence() {
        Assert.assertTrue("The license should be valid because of: " + Manager.getErrorMessage(), Manager.isValid());
    }

    private void valideFeature(final String feature) {
        try {
            Assert.assertTrue("The feature: ' " + feature + "' is not active.", Manager.isFeatureActive(feature));
        } catch (final ConfigurationError exception) {
            Assert.assertTrue("The feature : '" + feature + "' is not supported due to an invalid license.", false);
        }
    }

    @Test
    public void createManualTaskFeature() {
        valideFeature(Features.CREATE_MANUAL_TASK);
    }

    @Test
    public void createParameterFeature() {
        valideFeature(Features.CREATE_PARAMETER);
    }

    @Test
    public void createTenantFeature() {
        valideFeature(Features.CREATE_TENANT);
    }

    @Test
    public void replayActivityFeature() {
        valideFeature(Features.REPLAY_ACTIVITY);
    }

    @Test
    public void setConnectorStateFeature() {
        valideFeature(Features.SET_CONNECTOR_STATE);
    }

}
