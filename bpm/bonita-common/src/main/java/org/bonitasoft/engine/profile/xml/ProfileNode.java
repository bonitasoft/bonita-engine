/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.profile.xml;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {})
public class ProfileNode {

    @XmlAttribute
    private String name;
    @XmlAttribute
    private boolean isDefault;
    @XmlElement
    private String description;

    @XmlElement(name = "profileMapping")
    private ProfileMappingNode profileMapping;

    public ProfileNode() {
    }

    public ProfileNode(final String name, final boolean isDefault) {
        this.name = name;
        this.isDefault = isDefault;
        profileMapping = new ProfileMappingNode();
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(final boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public ProfileMappingNode getProfileMapping() {
        return profileMapping;
    }

    public void setProfileMapping(final ProfileMappingNode profileMapping) {
        this.profileMapping = profileMapping;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasCustomPages() {
        return false; //FIXME: remove this method and all that is linked (profile auto-update at startup)
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProfileNode that = (ProfileNode) o;
        return isDefault == that.isDefault &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(profileMapping, that.profileMapping);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isDefault, description, profileMapping);
    }

    @Override
    public String toString() {
        return "ProfileNode{" +
                "name='" + name + '\'' +
                ", isDefault=" + isDefault +
                ", description='" + description + '\'' +
                ", profileMapping=" + profileMapping +
                '}';
    }
}
