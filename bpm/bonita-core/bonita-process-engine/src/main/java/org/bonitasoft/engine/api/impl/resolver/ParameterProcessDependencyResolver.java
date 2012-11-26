/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.resolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.api.impl.ProcessAPIImpl;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.core.process.definition.model.SParameterDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.exception.ParameterProcessNotFoundException;
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.parameter.SParameterProcessNotFoundException;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Baptiste Mesta
 */
public class ParameterProcessDependencyResolver extends ProcessDependencyResolver {

    @Override
    public boolean resolve(final ProcessAPIImpl processApi, final TenantServiceAccessor tenantAccessor, final BusinessArchive businessArchive,
            final SProcessDefinition sDefinition) throws ParameterProcessNotFoundException {
        final ParameterService parameterService = tenantAccessor.getParameterService();
        final Set<SParameterDefinition> parameters = sDefinition.getParameters();
        final Map<String, String> defaultParamterValues = businessArchive.getParameters();

        boolean resolved = true;

        final Map<String, String> storedParameters = new HashMap<String, String>();
        for (final SParameterDefinition sParameterDefinition : parameters) {
            final String name = sParameterDefinition.getName();
            final String value = defaultParamterValues.get(sParameterDefinition.getName());
            if (value == null) {
                resolved = false;
            }
            storedParameters.put(name, value);
        }
        if (!resolved && parameters.size() != defaultParamterValues.size()) {
            resolved = false;
        }
        try {
            parameterService.addAll(sDefinition.getId(), storedParameters);
        } catch (final SParameterProcessNotFoundException e) {
            throw new ParameterProcessNotFoundException(e);
        }
        return resolved;
    }

}
