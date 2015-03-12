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

import org.bonitasoft.engine.bpm.parameter.ParameterDefinition;
import org.bonitasoft.engine.core.process.definition.model.SParameterDefinition;

/**
 * @author Matthieu Chaffotte
 */
public class SParameterDefinitionImpl extends SNamedElementImpl implements SParameterDefinition {

    private static final long serialVersionUID = -6048365663287821057L;

    private String description;

    private final String type;

    public SParameterDefinitionImpl(final ParameterDefinition parameterDefinition) {
        super(parameterDefinition.getName());
        description = parameterDefinition.getDescription();
        type = parameterDefinition.getType();
    }

    public SParameterDefinitionImpl(final String name, final String type) {
        super(name);
        this.type = type;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

}
