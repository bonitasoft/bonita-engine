/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.process.impl.internal;

import org.bonitasoft.engine.bpm.internal.NamedElementImpl;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessDefinitionImpl extends NamedElementImpl implements ProcessDefinition {
    private static final long serialVersionUID = -6292790136832048011L;
    @XmlAttribute(required = true)
    private final String version;
    @XmlAttribute(required = true)
    private String description;

    public ProcessDefinitionImpl(final String name, final String version) {
        super(name);
        this.version = version;
    }
    public ProcessDefinitionImpl(){
        super();
        this.version = "default version";
    }
    @Override
    public String getVersion() {
        return version;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (version == null ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProcessDefinitionImpl other = (ProcessDefinitionImpl) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("ProcessDefinitionImpl [name=");
        builder.append(getName());
        builder.append(", version=");
        builder.append(version);
        builder.append(", description=");
        builder.append(description);
        builder.append("]");
        return builder.toString();
    }

}
