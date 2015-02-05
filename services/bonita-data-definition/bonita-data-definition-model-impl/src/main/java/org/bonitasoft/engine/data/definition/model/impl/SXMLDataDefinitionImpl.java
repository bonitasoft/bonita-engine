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
package org.bonitasoft.engine.data.definition.model.impl;

import org.bonitasoft.engine.data.definition.model.SXMLDataDefinition;

/**
 * @author Elias Ricken de Medeiros
 */
public class SXMLDataDefinitionImpl extends SDataDefinitionImpl implements SXMLDataDefinition {

    private static final long serialVersionUID = 1L;

    private String namespace;

    private String element;

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
