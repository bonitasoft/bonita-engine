/*
 * Copyright (C) 2012 BonitaSoft S.A.
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
 */
public class ParameterProcessDependencyResolver extends ProcessDependencyResolver {

    @Override
    public boolean resolve(final ProcessAPI processApi, final TenantServiceAccessor tenantAccessor, final BusinessArchive businessArchive,
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
