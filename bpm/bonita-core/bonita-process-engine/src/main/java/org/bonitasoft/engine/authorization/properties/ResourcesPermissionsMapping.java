/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.engine.authorization.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Anthony Birembaut
 * @author Baptiste Mesta
 * @author Fabio Lombardi
 */
public class ResourcesPermissionsMapping extends ConfigurationFile {

    public static final String RESOURCE_IDS_SEPARATOR = "/";

    public static final String API_METHOD_SEPARATOR = "|";

    public static final String WILDCARD = "*";

    /**
     * Default name of the preferences file
     */
    public static final String PROPERTIES_FILENAME = "resources-permissions-mapping.properties";

    public ResourcesPermissionsMapping(long tenantId) {
        super(PROPERTIES_FILENAME, tenantId);
    }

    public ResourcesPermissionsMapping(String propertiesFilename, long tenantId) {
        super(propertiesFilename, tenantId);
    }

    public Set<String> getResourcePermissions(final String method, final String apiName, final String resourceName,
            final List<String> resourceQualifiers) {
        final String key = buildResourceKey(method, apiName, resourceName, resourceQualifiers);
        return getPropertyAsSet(key);
    }

    public Set<String> getResourcePermissionsWithWildCard(final String method, final String apiName,
            final String resourceName,
            final List<String> resourceQualifiers) {
        if (resourceQualifiers != null && resourceQualifiers.size() > 0) {
            for (int i = resourceQualifiers.size() - 1; i >= 0; i--) {
                final List<String> resourceQualifiersWithWildCard = getResourceQualifiersWithWildCard(
                        resourceQualifiers, i);
                final String key = buildResourceKey(method, apiName, resourceName, resourceQualifiersWithWildCard);
                final Set<String> permissions = getPropertyAsSet(key);
                if (!permissions.isEmpty()) {
                    return permissions;
                }
            }
            final List<String> reducedResourceQualifiers = new ArrayList<>(resourceQualifiers);
            reducedResourceQualifiers.remove(resourceQualifiers.size() - 1);
            return getResourcePermissionsWithWildCard(method, apiName, resourceName, reducedResourceQualifiers);
        }
        return Collections.emptySet();
    }

    protected List<String> getResourceQualifiersWithWildCard(final List<String> resourceQualifiers,
            final int wildCardPosition) {
        final List<String> resourceQualifiersWithWildCard = new ArrayList<>(resourceQualifiers);
        resourceQualifiersWithWildCard.set(wildCardPosition, WILDCARD);
        return resourceQualifiersWithWildCard;
    }

    protected String buildResourceKey(final String method, final String apiName, final String resourceName,
            final List<String> resourceQualifiers) {
        String key = method + API_METHOD_SEPARATOR + apiName + "/" + resourceName;
        if (resourceQualifiers != null) {
            for (final String resourceQualifier : resourceQualifiers) {
                key += RESOURCE_IDS_SEPARATOR + resourceQualifier;
            }
        }
        return key;
    }

    public Set<String> getResourcePermissions(final String method, final String apiName, final String resourceName) {
        return getResourcePermissions(method, apiName, resourceName, null);
    }

}
