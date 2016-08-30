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

package org.bonitasoft.engine.identity.xml;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Baptiste Mesta
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ExportedGroup {

    @XmlAttribute
    private String name;
    @XmlAttribute
    private String parentPath;
    @XmlElement
    private String displayName;
    @XmlElement
    private String description;
    @XmlElement
    private String iconName;
    @XmlElement
    private String iconPath;

    public ExportedGroup() {
    }

    public ExportedGroup(String name, String parentPath, String displayName, String description, String iconName, String iconPath) {
        this.name = name;
        this.parentPath = parentPath;
        this.displayName = displayName;
        this.description = description;
        this.iconName = iconName;
        this.iconPath = iconPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportedGroup that = (ExportedGroup) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(parentPath, that.parentPath) &&
                Objects.equals(displayName, that.displayName) &&
                Objects.equals(description, that.description) &&
                Objects.equals(iconName, that.iconName) &&
                Objects.equals(iconPath, that.iconPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parentPath, displayName, description, iconName, iconPath);
    }

    @Override
    public String toString() {
        return "ExportedGroup{" +
                "name='" + name + '\'' +
                ", parentPath='" + parentPath + '\'' +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", iconName='" + iconName + '\'' +
                ", iconPath='" + iconPath + '\'' +
                '}';
    }
}
