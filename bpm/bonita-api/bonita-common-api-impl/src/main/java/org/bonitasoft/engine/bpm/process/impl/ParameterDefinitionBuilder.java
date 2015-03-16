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
package org.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.parameter.impl.ParameterDefinitionImpl;
import org.bonitasoft.engine.bpm.process.impl.internal.DesignProcessDefinitionImpl;

/**
 * Definition builder for Parameter (specific Bonita BPM Subscription Edition feature).
 *
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @see org.bonitasoft.engine.bpm.parameter.ParameterDefinition
 * @version 6.4.1
 * @since 6.0.0
 */
public class ParameterDefinitionBuilder extends ProcessBuilder implements DescriptionBuilder {

    private final ParameterDefinitionImpl parameter;

    /**
     * Default Constructor.
     * To build a new {@link org.bonitasoft.engine.bpm.parameter.ParameterDefinition}
     *
     * @param processDefinitionBuilder
     *        The {@link ProcessDefinitionBuilder} to build the
     *        {@link org.bonitasoft.engine.bpm.process.DesignProcessDefinition}
     * @param designProcessDefinitionImpl
     *        The {@link org.bonitasoft.engine.bpm.process.DesignProcessDefinition} where add the new
     *        {@link org.bonitasoft.engine.bpm.parameter.ParameterDefinition}
     * @param parameterName
     *        The name of the new {@link org.bonitasoft.engine.bpm.parameter.ParameterDefinition}
     * @param type
     *        The type of the new {@link org.bonitasoft.engine.bpm.parameter.ParameterDefinition}
     */
    protected ParameterDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final DesignProcessDefinitionImpl designProcessDefinitionImpl,
                                         final String parameterName, final String type) {
        super(designProcessDefinitionImpl, processDefinitionBuilder);
        parameter = new ParameterDefinitionImpl(parameterName, type);
        designProcessDefinitionImpl.addParameter(parameter);
    }
    protected ParameterDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final DesignProcessDefinitionImpl designProcessDefinitionImpl){
        super(designProcessDefinitionImpl, processDefinitionBuilder);
        parameter = null;
    }

    @Override
    public ParameterDefinitionBuilder addDescription(final String description) {
        parameter.setDescription(description);
        return this;
    }

}
