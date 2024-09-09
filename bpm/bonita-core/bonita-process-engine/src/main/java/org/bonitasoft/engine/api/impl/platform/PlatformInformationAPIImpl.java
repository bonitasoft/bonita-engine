/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl.platform;

import java.util.Map;

import org.bonitasoft.engine.api.impl.AvailableInMaintenanceMode;
import org.bonitasoft.engine.api.platform.PlatformInformationAPI;

/**
 * Provides runtime information about the platform.
 * Information returned is dependent on the edition (Community or Subscription)
 * and the platform configuration / license.
 *
 * @author Emmanuel Duchastenier
 */
@AvailableInMaintenanceMode
public class PlatformInformationAPIImpl implements PlatformInformationAPI {

    @Override
    public Map<String, String> getPlatformInformation() {
        return Map.of(
                "edition", "subscription",
                "caseCounterLimit", "75",
                "caseCounter", "68",
                "subscriptionStartTimestamp", "1440806400000");
    }
}
