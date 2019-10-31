/*
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
 */
package org.bonitasoft.engine.profile.xml;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportError.Type;

/**
 * @author Zhao Na
 * @author Matthieu Chaffotte
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ProfileEntryNode {

    @XmlAttribute
    private String name;
    @XmlAttribute
    private boolean isCustom;
    //deprecated because it is not used but kept in xsd, this should be removed along with xsd change and migration
    @Deprecated
    @XmlElement
    private String parentName;
    @XmlElement
    private Long index = 0L;
    @XmlElement
    private String description;
    @XmlElement
    private String type;
    @XmlElement
    private String page;

    public ProfileEntryNode() {
    }

    public ProfileEntryNode(final String name) {
        this.name = name;
    }

    public final boolean isCustom() {
        return isCustom;
    }

    public final void setCustom(final boolean isCustom) {
        this.isCustom = isCustom;
    }

    public final String getDescription() {
        return description;
    }

    public final void setDescription(final String description) {
        this.description = description;
    }

    public final String getType() {
        return type;
    }

    public final void setType(final String type) {
        this.type = type;
    }

    public final long getIndex() {
        return index;
    }

    public final void setIndex(final long index) {
        this.index = index;
    }

    public final String getPage() {
        return page;
    }

    public final void setPage(final String page) {
        this.page = page;
    }

    public final String getName() {
        return name;
    }

    public boolean hasError() {
        return getError() != null;
    }

    public ImportError getError() {
        if (getName() == null) {
            return new ImportError(getName(), Type.PAGE);
        }
        if (getPage() == null || getPage().isEmpty()) {
            return new ImportError(getName(), Type.PAGE);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProfileEntryNode that = (ProfileEntryNode) o;
        return isCustom == that.isCustom &&
                Objects.equals(name, that.name) &&
                Objects.equals(index, that.index) &&
                Objects.equals(description, that.description) &&
                Objects.equals(type, that.type) &&
                Objects.equals(page, that.page);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isCustom, index, description, type, page);
    }

    @Override
    public String toString() {
        return "ProfileEntryNode{" +
                "name='" + name + '\'' +
                ", isCustom=" + isCustom +
                ", index=" + index +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", page='" + page + '\'' +
                '}';
    }
}
