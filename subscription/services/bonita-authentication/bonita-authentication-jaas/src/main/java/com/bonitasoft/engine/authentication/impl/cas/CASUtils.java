/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.authentication.impl.cas;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;

/**
 * @author Julien Reboul
 *
 */
public class CASUtils {

    private static final CASUtils casUtils = new CASUtils();

    public static final String THE_CAS_AUTHENTICATOR_IS_NOT_AN_ACTIVE_FEATURE = "The CAS authenticator is not an active feature.";

    private CASUtils() {

    }

    public static CASUtils getInstance() {
        return casUtils;
    }

    public void checkLicense() {
        if (!Manager.getInstance().isFeatureActive(Features.SSO)) {
            throw new IllegalStateException(THE_CAS_AUTHENTICATOR_IS_NOT_AN_ACTIVE_FEATURE);
        }
    }

}
