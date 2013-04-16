/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.StopNodeException;

import com.bonitasoft.engine.api.impl.PlatformAPIExt;
import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;

/**
 * @author Matthieu Chaffotte
 */
public class LicenseChecker {

    private final Map<String, String> exceptions;

    private final Random random;

    private static class LicenseCheckerHolder {

        public static final LicenseChecker INSTANCE = new LicenseChecker();

    }

    public static LicenseChecker getInstance() {
        return LicenseCheckerHolder.INSTANCE;
    }

    private LicenseChecker() {
        super();
        exceptions = new HashMap<String, String>(5);
        exceptions.put(Features.CREATE_MANUAL_TASK, "Create/delete a manual task is not an active feature");
        exceptions.put(Features.CREATE_PARAMETER, "Create a parameter is not an active feature");
        exceptions.put(Features.CREATE_TENANT, "Create a tenant is not an active feature");
        exceptions.put(Features.REPLAY_ACTIVITY, "Replay an activity is not an active feature");
        exceptions.put(Features.SET_CONNECTOR_STATE, "Set the connector state is not an active feature");
        random = new Random();
    }

    public void checkLicence() {
        final int count = random.nextInt(2);
        if (count == 0 && !Manager.isValid()) {
            stopNode();
            throw new IllegalStateException(Manager.getErrorMessage());
        }
    }

    public void checkLicence(final String feature) {
        checkLicence();
        if (!Manager.isFeatureActive(feature)) {
            final String message = exceptions.get(feature);
            throw new IllegalStateException(message);
        }
    }

    private void stopNode() {
        final PlatformAPIExt platformAPI = new PlatformAPIExt();
        try {
            platformAPI.stopNode();
        } catch (final InvalidSessionException ise) {
            throw new IllegalStateException(ise);
        } catch (final StopNodeException sne) {
            throw new IllegalStateException(sne);
        }
    }

}
