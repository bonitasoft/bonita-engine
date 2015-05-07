/*
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 */
package org.bonitasoft.engine.page;

import java.io.Serializable;
import java.util.Map;

/**
 * author Emmanuel Duchastenier, Anthony Birembaut
 */
public class IsAdminRule implements AuthorizationRule {

    @Override
    public boolean isAllowed(final String key, final Map<String, Serializable> context) {
        if (context.containsKey(AuthorizationRuleConstants.IS_ADMIN)) {
            return (Boolean) context.get(AuthorizationRuleConstants.IS_ADMIN);
        }
        return false;
    }

    @Override
    public String getId() {
        return AuthorizationRuleConstants.IS_ADMIN;
    }
}
