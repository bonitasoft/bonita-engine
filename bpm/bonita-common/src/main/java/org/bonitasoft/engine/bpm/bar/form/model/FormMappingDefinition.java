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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.form.FormMappingType;

/**
 * @author Emmanuel Duchastenier
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class FormMappingDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute
    private String form;

    @XmlAttribute(required = true)
    private FormMappingTarget target;

    @XmlAttribute
    private FormMappingType type;

    @XmlAttribute
    private String taskname;

    /**
     * This constructor is for JAXB
     */
    protected FormMappingDefinition() {
    }

    public FormMappingDefinition(final String form, final FormMappingType type, final FormMappingTarget target) {
        this.form = form;
        this.type = type;
        this.target = target;
    }

    public FormMappingDefinition(final String form, final FormMappingType type, final FormMappingTarget target, final String taskname) {
        this(form, type, target);
        setTaskname(taskname);
    }

    public String getTaskname() {
        return taskname;
    }

    public void setTaskname(final String taskname) {
        this.taskname = taskname;
    }

    public String getForm() {
        return form;
    }

    public FormMappingTarget getTarget() {
        return target;
    }

    public FormMappingType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 43).append(form).append(target).append(type).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof FormMappingDefinition) {
            final FormMappingDefinition other = (FormMappingDefinition) obj;
            return new EqualsBuilder().append(form, other.form).append(target, other.target).append(type, other.type).isEquals();
        }
        return false;
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("page", form).append("target", target).append("type", type).append("taskname", taskname);
        return builder.toString();
    }

}
