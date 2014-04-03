/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
