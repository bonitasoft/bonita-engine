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
package org.bonitasoft.engine.data.instance.model.impl;

import java.io.Serializable;

import org.bonitasoft.engine.data.definition.model.SXMLDataDefinition;
import org.bonitasoft.engine.data.instance.model.SXMLDataInstance;

/**
 * @author Zhao Na
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class SXMLDataInstanceImpl extends SDataInstanceImpl implements SXMLDataInstance {

    private static final long serialVersionUID = 5646725264914760542L;

    private String value;

    private String namespace;

    private String element;

    public SXMLDataInstanceImpl() {
        super();
    }

    public SXMLDataInstanceImpl(final SXMLDataDefinition dataDefinition) {
        super(dataDefinition);
        namespace = dataDefinition.getNamespace();
        element = dataDefinition.getElement();
    }

    @Override
    public void setValue(final Serializable value) {
        this.value = (String) value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getDiscriminator() {
        return SXMLDataInstanceImpl.class.getSimpleName();
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getElement() {
        return element;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    public void setElement(final String element) {
        this.element = element;
    }

}
