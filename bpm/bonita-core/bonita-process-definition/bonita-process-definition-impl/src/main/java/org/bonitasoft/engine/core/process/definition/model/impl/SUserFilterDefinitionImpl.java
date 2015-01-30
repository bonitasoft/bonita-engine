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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.bpm.userfilter.UserFilterDefinition;
import org.bonitasoft.engine.core.process.definition.model.SUserFilterDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.ServerModelConvertor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Baptiste Mesta
 */
public class SUserFilterDefinitionImpl extends SNamedElementImpl implements SUserFilterDefinition {

    private static final long serialVersionUID = -6045216424839658552L;

    private final String filterId;

    private final String version;

    private final Map<String, SExpression> inputs;

    /**
     * @param userFilter
     */
    public SUserFilterDefinitionImpl(final UserFilterDefinition userFilter) {
        super(userFilter.getName());
        filterId = userFilter.getUserFilterId();
        version = userFilter.getVersion();
        inputs = new HashMap<String, SExpression>(userFilter.getInputs().size());
        for (final Entry<String, Expression> input : userFilter.getInputs().entrySet()) {
            final Expression value = input.getValue();
            final SExpression sExpression = ServerModelConvertor.convertExpression(value);
            inputs.put(input.getKey(), sExpression);
        }
    }

    /**
     * @param name
     * @param userFilterId
     */
    public SUserFilterDefinitionImpl(final String name, final String userFilterId, final String version) {
        super(name);
        filterId = userFilterId;
        this.version = version;
        inputs = new HashMap<String, SExpression>();
    }

    @Override
    public String getUserFilterId() {
        return filterId;
    }

    @Override
    public Map<String, SExpression> getInputs() {
        return inputs;
    }

    public void addInput(final String name, final SExpression value) {
        inputs.put(name, value);
    }

    @Override
    public String getVersion() {
        return version;
    }

}
