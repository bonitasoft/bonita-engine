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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Emmanuel Duchastenier
 */
public class BonitaConfiguration implements Serializable {

    private String resourceName;
    private byte[] resourceContent;

    public BonitaConfiguration(String resourceName, byte[] resourceContent) {
        this.resourceName = resourceName;
        this.resourceContent = resourceContent;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public byte[] getResourceContent() {
        return resourceContent;
    }

    public void setResourceContent(byte[] resourceContent) {
        this.resourceContent = resourceContent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BonitaConfiguration that = (BonitaConfiguration) o;
        return Objects.equals(resourceName, that.resourceName) &&
                Arrays.equals(resourceContent, that.resourceContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceName, resourceContent);
    }

    @Override
    public String toString() {
        return "BonitaConfiguration{" +
                "resourceName='" + resourceName + '\'' +
                '}';
    }
}
