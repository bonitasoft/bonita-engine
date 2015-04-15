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
package org.bonitasoft.engine.api.impl.resolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.Problem.Level;
import org.bonitasoft.engine.bpm.process.impl.internal.ProblemImpl;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SParameterDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.parameter.OrderBy;
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.parameter.SParameter;
import org.bonitasoft.engine.parameter.SParameterProcessNotFoundException;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class ParameterProcessDependencyDeployer implements ProcessDependencyDeployer {

    @Override
    public boolean deploy(final TenantServiceAccessor tenantAccessor, final BusinessArchive businessArchive,
                          final SProcessDefinition processDefinition) throws NotFoundException {
        final Set<SParameterDefinition> parameters = processDefinition.getParameters();
        boolean resolved = true;
        if (parameters.isEmpty()) {
            return resolved;
        }
        final ParameterService parameterService = tenantAccessor.getParameterService();
        final Map<String, String> defaultParamterValues = businessArchive.getParameters();
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
            parameterService.addAll(processDefinition.getId(), storedParameters);
        } catch (final SParameterProcessNotFoundException e) {
            throw new NotFoundException(e);
        }
        return resolved;
    }

    @Override
    public List<Problem> checkResolution(final TenantServiceAccessor tenantAccessor, final SProcessDefinition processDefinition) {
        if (processDefinition.getParameters().isEmpty()) {
            return Collections.emptyList();
        }
        final ParameterService parameterService = tenantAccessor.getParameterService();

        List<SParameter> parameters;
        final ArrayList<Problem> problems = new ArrayList<Problem>();
        int i = 0;
        do {
            try {
                parameters = parameterService.getNullValues(processDefinition.getId(), i, 100, OrderBy.NAME_ASC);
            } catch (final SBonitaException e) {
                return Collections.singletonList((Problem) new ProblemImpl(Level.ERROR, null, "parameter", "Unable to get parameter !!"));
            }
            i += 100;
            for (final SParameter parameter : parameters) {
                if (parameter.getValue() == null) {
                    final Problem problem = new ProblemImpl(Level.ERROR, null, "parameter", "Parameter '" + parameter.getName() + "' is not set.");
                    problems.add(problem);
                }
            }
        } while (parameters.size() == 100);
        return problems;
    }

}
