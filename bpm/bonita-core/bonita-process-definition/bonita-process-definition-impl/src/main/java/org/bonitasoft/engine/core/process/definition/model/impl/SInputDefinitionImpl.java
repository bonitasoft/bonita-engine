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
package org.bonitasoft.engine.core.process.definition.model.impl;

import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;

/**
 * @author Matthieu Chaffotte
 */
public class SInputDefinitionImpl extends SNamedElementImpl implements SInputDefinition {

    private static final long serialVersionUID = -5021740296501498639L;
    private final String description;
    private final boolean multiple;

    public SInputDefinitionImpl(final String name) {
        this(name, null, false);
    }

    public SInputDefinitionImpl(final String name, final String description) {
        this(name, description, false);
    }

    public SInputDefinitionImpl(final String name, final String description, final boolean multiple) {
        super(name);
        this.description = description;
        this.multiple = multiple;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isMultiple() {
        return multiple;
    }

}
