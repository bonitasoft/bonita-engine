/*
 * Copyright (C) 2012-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.api.impl.resolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.impl.resolver.ProcessDependencyResolver;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.core.process.definition.model.SParameterDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.parameter.SParameterProcessNotFoundException;
import org.bonitasoft.engine.service.TenantServiceAccessor;

import com.bonitasoft.engine.exception.ParameterProcessNotFoundException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class ParameterProcessDependencyResolver extends ProcessDependencyResolver {

    @Override
    public boolean resolve(final ProcessAPI processApi, final TenantServiceAccessor tenantAccessor, final BusinessArchive businessArchive,
            final SProcessDefinition sDefinition) throws ParameterProcessNotFoundException {
        final Set<SParameterDefinition> parameters = sDefinition.getParameters();
        boolean resolved = true;
        if (parameters.isEmpty()) {
            return resolved;
        }
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

}
