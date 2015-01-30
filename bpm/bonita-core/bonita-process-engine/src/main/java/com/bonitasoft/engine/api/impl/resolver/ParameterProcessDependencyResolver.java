/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.resolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.api.impl.resolver.ProcessDependencyResolver;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.Problem.Level;
import org.bonitasoft.engine.bpm.process.impl.internal.ProblemImpl;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SParameterDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.service.TenantServiceAccessor;

import com.bonitasoft.engine.bpm.parameter.ParameterProcessNotFoundException;
import com.bonitasoft.engine.parameter.OrderBy;
import com.bonitasoft.engine.parameter.ParameterService;
import com.bonitasoft.engine.parameter.SParameter;
import com.bonitasoft.engine.parameter.SParameterProcessNotFoundException;
import com.bonitasoft.engine.service.impl.LicenseChecker;
import com.bonitasoft.manager.Features;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class ParameterProcessDependencyResolver implements ProcessDependencyResolver {

    @Override
    public boolean resolve(final TenantServiceAccessor tenantAccessor, final BusinessArchive businessArchive,
            final SProcessDefinition sDefinition) throws ParameterProcessNotFoundException {
        final Set<SParameterDefinition> parameters = sDefinition.getParameters();
        boolean resolved = true;
        if (parameters.isEmpty()) {
            return resolved;
        }
        LicenseChecker.getInstance().checkLicenseAndFeature(Features.CREATE_PARAMETER);
        final ParameterService parameterService = ((com.bonitasoft.engine.service.TenantServiceAccessor) tenantAccessor).getParameterService();
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
            parameterService.addAll(sDefinition.getId(), storedParameters);
        } catch (final SParameterProcessNotFoundException e) {
            throw new ParameterProcessNotFoundException(e);
        }
        return resolved;
    }

    @Override
    public List<Problem> checkResolution(final TenantServiceAccessor tenantAccessor, final SProcessDefinition processDefinition) {
        if (processDefinition.getParameters().isEmpty()) {
            return Collections.emptyList();
        }
        final ParameterService parameterService = ((com.bonitasoft.engine.service.TenantServiceAccessor) tenantAccessor).getParameterService();

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
