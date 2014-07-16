/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.licence;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;

public class LicenseTest {

    @BeforeClass
    public static void setBonitaHome() {
        System.setProperty("bonita.home", "target/home");
    }

    @Test
    public void validateLicence() {
        final Manager manager = Manager.getInstance();
        Assert.assertTrue("The license should be valid because of: " + manager.getErrorMessage(), manager.isValid());
    }

    private void valideFeature(final String feature) {
        try {
            Assert.assertTrue("The feature: ' " + feature + "' is not active.", Manager.getInstance().isFeatureActive(feature));
        } catch (final Exception exception) {
            Assert.assertTrue("The feature : '" + feature + "' is not supported due to an invalid license." + exception.getMessage(), false);
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

    @Test
    public void traceability() {
        valideFeature(Features.TRACEABILITY);
    }

    @Test
    public void customPages() {
        valideFeature(Features.CUSTOM_PAGE);
    }

}
