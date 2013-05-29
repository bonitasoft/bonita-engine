/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.platform.StopNodeException;

import com.bonitasoft.engine.api.impl.PlatformAPIExt;
import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;

/**
 * @author Matthieu Chaffotte
 */
public class LicenseChecker {

    private final Map<String, String> exceptions;

    private static class LicenseCheckerHolder {

        public static final LicenseChecker INSTANCE = new LicenseChecker();
    }

    public static LicenseChecker getInstance() {
        return LicenseCheckerHolder.INSTANCE;
    }

    private LicenseChecker() {
        super();
        exceptions = new HashMap<String, String>(14);
        exceptions.put(Features.BPM_MONITORING, "The bpm monitoring is not an active feature.");
        exceptions.put(Features.CREATE_MANUAL_TASK, "The manual task creation/deletion is not an active feature.");
        exceptions.put(Features.CREATE_PARAMETER, "The parameter creation is not an active feature.");
        exceptions.put(Features.CREATE_TENANT, "The tenant creation is not an active feature.");
        exceptions.put(Features.CUSTOM_PROFILES, "The profile customization is not an active feature.");
        exceptions.put(Features.ENGINE_ARCHIVE_CONFIG, "The archive configuration is not an active feature.");
        exceptions.put(Features.ENGINE_CLUSTERING, "The clustering is not an active feature.");
        exceptions.put(Features.POST_DEPLOY_CONFIG, "The configuration change, after deployment, is not an active feature.");
        exceptions.put(Features.REPLAY_ACTIVITY, "Replay an activity is not an active feature.");
        exceptions.put(Features.RESOURCE_MONITORING, "The resource monitoring is not an active feature.");
        exceptions.put(Features.SEARCH_INDEX, "Search index is not an active feature.");
        exceptions.put(Features.SERVICE_MONITORING, "The service monitoring is not an active feature.");
        exceptions.put(Features.SET_CONNECTOR_STATE, "Set the connector state is not an active feature.");
        exceptions.put(Features.WEB_ORGANIZATION_EXCHANGE, "Export Organization is not an active feature.");
    }

    public boolean checkLicence() {
        if (!Manager.isValid()) {
            stopNode();
            return false;
        }
        return true;
    }

    public void checkLicenceAndFeature(final String feature) throws IllegalStateException {
        checkLicence();
        if (!Manager.isFeatureActive(feature)) {
            final String message = exceptions.get(feature);
            throw new IllegalStateException(message);
        }
    }

    private void stopNode() {
        final PlatformAPIExt platformAPI = new PlatformAPIExt();
        try {
            final PlatformServiceAccessor platformAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            if (platformAPI.isPlatformStarted(platformAccessor)) {
                platformAPI.stopNode("an invalid license");
            }
        } catch (final StopNodeException sne) {
            throw new IllegalStateException(sne);
        } catch (final BonitaHomeNotSetException bhnse) {
            throw new IllegalStateException(bhnse);
        } catch (final BonitaHomeConfigurationException bhce) {
            throw new IllegalStateException(bhce);
        } catch (final InstantiationException ie) {
            throw new IllegalStateException(ie);
        } catch (final IllegalAccessException iae) {
            throw new IllegalStateException(iae);
        } catch (final ClassNotFoundException cnfe) {
            throw new IllegalStateException(cnfe);
        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    /**
     * @return the license error message, if any.
     */
    public String getErrorMessage() {
        return Manager.getErrorMessage();
    }

}
