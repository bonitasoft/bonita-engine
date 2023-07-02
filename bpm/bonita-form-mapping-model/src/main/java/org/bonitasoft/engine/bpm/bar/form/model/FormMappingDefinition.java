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
package org.bonitasoft.engine.bpm.bar.form.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.form.FormMappingType;

/**
 * @author Emmanuel Duchastenier
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = { "form", "target", "type" })
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
public class FormMappingDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute
    private String form;

    @XmlAttribute
    private FormMappingType type;

    @XmlAttribute(required = true)
    private FormMappingTarget target;

    @Setter
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

}
