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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportError.Type;

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ParentProfileEntryNode extends ProfileEntryNode {

    @XmlElementWrapper(name = "childrenEntries")
    @XmlElement(name = "profileEntry")
    private List<ProfileEntryNode> childProfileEntries = Collections.emptyList();

    public ParentProfileEntryNode() {
        childProfileEntries = new ArrayList<>();
    }

    public List<ProfileEntryNode> getChildProfileEntries() {
        return childProfileEntries;
    }

    public void setChildProfileEntries(final List<ProfileEntryNode> childProfileEntries) {
        this.childProfileEntries = childProfileEntries;
    }

    public ParentProfileEntryNode(final String name) {
        super(name);
    }

    public List<ImportError> getErrors() {
        final List<ImportError> errors = new ArrayList<>();
        if (hasError()) {
            errors.add(getError());
        }
        final List<ProfileEntryNode> childProfileEntries = getChildProfileEntries();
        if (childProfileEntries != null) {
            for (final ProfileEntryNode childEntry : childProfileEntries) {
                if (childEntry.hasError()) {
                    errors.add(childEntry.getError());
                }
            }
        }
        if (errors.isEmpty()) {
            return null;
        }
        return errors;
    }

    public boolean hasErrors() {
        return getErrors() != null;
    }

    @Override
    public ImportError getError() {
        if (getName() == null) {
            return new ImportError(getName(), Type.PAGE);
        }
        return null;
    }

    public boolean hasCustomPages() {
        if (isCustom()) {
            return true;
        }
        if (getChildProfileEntries() != null) {
            for (final ProfileEntryNode profileEntryNode : getChildProfileEntries()) {
                if (profileEntryNode.isCustom()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        ParentProfileEntryNode that = (ParentProfileEntryNode) o;
        return Objects.equals(childProfileEntries, that.childProfileEntries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), childProfileEntries);
    }

    @Override
    public String toString() {
        return "ParentProfileEntryNode{" +
                super.toString() +
                "childProfileEntries=" + childProfileEntries +
                '}';
    }
}
