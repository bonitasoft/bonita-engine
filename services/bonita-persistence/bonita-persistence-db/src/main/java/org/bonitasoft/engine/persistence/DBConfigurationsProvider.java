/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
 **/
package org.bonitasoft.engine.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Charles Souillard
 * @author Celine Souchet
 */
public class DBConfigurationsProvider {

    private List<DBConfiguration> tenantConfigurations;

    public void setTenantConfigurations(final List<DBConfiguration> tenantConfigurations) {
        this.tenantConfigurations = tenantConfigurations;
        Collections.sort(this.tenantConfigurations, new Comparator<DBConfiguration>() {

            @Override
            public int compare(final DBConfiguration dbConfiguration1, final DBConfiguration dbConfiguration2) {
                final int priority1 = dbConfiguration1.getDeleteTenantObjectsPriority();
                final int priority2 = dbConfiguration2.getDeleteTenantObjectsPriority();

                return priority1 - priority2;
            }
        });
    }

    public List<DBConfiguration> getTenantConfigurations() {
        return tenantConfigurations;
    }

    public List<DBConfiguration> getMatchingTenantConfigurations(final String filter) {
        if (filter == null || filter.isEmpty()) {
            return getTenantConfigurations();
        }
        return filterTenantConfiguration(filter);
    }

    private List<DBConfiguration> filterTenantConfiguration(final String filter) {
        final List<DBConfiguration> matchingTenantConfiguration = new ArrayList<DBConfiguration>();
        for (final DBConfiguration dbConfiguration : tenantConfigurations) {
            if (dbConfiguration.matchesFilter(filter)) {
                matchingTenantConfiguration.add(dbConfiguration);
            }
        }
        return matchingTenantConfiguration;
    }

}
