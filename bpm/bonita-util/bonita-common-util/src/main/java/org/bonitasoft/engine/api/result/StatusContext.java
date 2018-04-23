/**
 * Copyright (C) 2017 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.api.result;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Emmanuel Duchastenier
 */
public class StatusContext extends HashMap<String, Serializable> {

    public static final String BUSINESS_OBJECT_NAME_KEY = "businessObjectName";
    public static final String ACCESS_RULE_NAME_KEY = "accessRuleName";
    public static final String ATTRIBUTE_NAME_KEY = "attributeName";
    public static final String PROFILE_NAME_KEY = "profileName";
    public static final String BDM_ARTIFACT_KEY = "bdmArtifact";
    public static final String BDM_ARTIFACT_NAME_KEY = "bdmArtifactName";
    public static final String INVALID_NAME_KEY = "invalidName";

    public StatusContext() {
        super();
    }

    public StatusContext(Map<String, Serializable> context) {
        super(context);
    }
}
