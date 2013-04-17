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
    public void bpmMonitoringFeature() {
        valideFeature(Features.BPM_MONITORING);
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
    public void customProfilesFeature() {
        valideFeature(Features.CUSTOM_PROFILES);
    }

    @Test
    public void engineArchiveConfigFeature() {
        valideFeature(Features.ENGINE_ARCHIVE_CONFIG);
    }

    @Test
    public void engineClusteringFeature() {
        valideFeature(Features.ENGINE_CLUSTERING);
    }

    @Test
    public void postDeployConfigFeature() {
        valideFeature(Features.POST_DEPLOY_CONFIG);
    }

    @Test
    public void replayActivityFeature() {
        valideFeature(Features.REPLAY_ACTIVITY);
    }

    @Test
    public void resourceMonitoringFeature() {
        valideFeature(Features.RESOURCE_MONITORING);
    }

    @Test
    public void searchIndexFeature() {
        valideFeature(Features.SEARCH_INDEX);
    }

    @Test
    public void serviceMonitoringFeature() {
        valideFeature(Features.SERVICE_MONITORING);
    }

    @Test
    public void setConnectorStateFeature() {
        valideFeature(Features.SET_CONNECTOR_STATE);
    }

}
