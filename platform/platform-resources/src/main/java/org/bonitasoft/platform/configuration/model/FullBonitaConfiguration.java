/**
 * Copyright (C) 2016 Bonitasoft S.A.
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
package org.bonitasoft.platform.configuration.model;

import java.util.Arrays;
import java.util.Objects;

import org.bonitasoft.platform.configuration.type.ConfigurationType;

/**
 * @author Laurent Leseigneur
 */
public class FullBonitaConfiguration extends BonitaConfiguration {

    private final String configurationType;

    private final Long tenantId;

    public FullBonitaConfiguration(String resourceName, byte[] resourceContent, String configurationType, Long tenantId) {
        super(resourceName, resourceContent);
        this.configurationType = configurationType;
        this.tenantId = tenantId;
    }

    public String getConfigurationType() {
        return configurationType;
    }

    public Long getTenantId() {
        return tenantId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FullBonitaConfiguration that = (FullBonitaConfiguration) o;
        return Objects.equals(getResourceName(), that.getResourceName()) &&
                Arrays.equals(getResourceContent(), that.getResourceContent()) &&
                Objects.equals(getResourceName(), that.getResourceName()) &&
                Objects.equals(getTenantId(), that.getTenantId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getResourceName(), getResourceContent(), configurationType);
    }

    @Override
    public String toString() {
        return String.format("FullBonitaConfiguration{ resourceName='%s' , configurationType='%s' , tenantId=%d }",
                getResourceName(),
                getConfigurationType(),
                getTenantId());
    }

    public boolean isLicenseFile() {
        return getConfigurationType().equals(ConfigurationType.LICENSES.name());
    }

    public boolean isTenantFile() {
        return getTenantId() > 0;
    }
}
