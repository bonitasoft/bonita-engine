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
package org.bonitasoft.engine.bpm.bar.form.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Emmanuel Duchastenier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FormMappingModel implements Serializable {


    private static final long serialVersionUID = 1L;

    @XmlElementWrapper(name = "form-mappings", required = true)
    @XmlElement(name = "form-mapping", required = false)
    private List<FormMappingDefinition> formMappings;

    public FormMappingModel() {
        formMappings = new ArrayList<>();
    }

    public List<FormMappingDefinition> getFormMappings() {
        return formMappings;
    }

    public void setFormMappings(final List<FormMappingDefinition> FormMappings) {
        formMappings = FormMappings;
    }

    public void addFormMapping(final FormMappingDefinition mapping) {
        formMappings.add(mapping);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 37).append(formMappings).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof FormMappingModel) {
            final FormMappingModel other = (FormMappingModel) obj;
            return new EqualsBuilder().append(formMappings, other.formMappings).isEquals();
        }
        return false;
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("formMappings", formMappings);
        return builder.toString();
    }

}
