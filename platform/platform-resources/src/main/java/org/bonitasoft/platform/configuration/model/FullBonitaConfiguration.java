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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bonitasoft.platform.configuration.type.ConfigurationType;

/**
 * @author Laurent Leseigneur
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class FullBonitaConfiguration extends BonitaConfiguration {

    private final String configurationType;

    public FullBonitaConfiguration(String resourceName, byte[] resourceContent, String configurationType) {
        super(resourceName, resourceContent);
        this.configurationType = configurationType;
    }

    public boolean isLicenseFile() {
        return getConfigurationType().equals(ConfigurationType.LICENSES.name());
    }

    @Override
    public String toString() {
        return String.format("FullBonitaConfiguration{ resourceName='%s' , configurationType='%s' }",
                getResourceName(), getConfigurationType());
    }

}
