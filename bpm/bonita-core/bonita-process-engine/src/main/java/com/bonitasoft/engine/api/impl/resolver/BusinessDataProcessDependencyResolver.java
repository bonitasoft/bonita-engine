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
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.api.impl.resolver.ProcessDependencyResolver;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.Problem.Level;
import org.bonitasoft.engine.bpm.process.impl.internal.ProblemImpl;
import org.bonitasoft.engine.core.process.definition.model.SBusinessDataDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.service.TenantServiceAccessor;

import com.bonitasoft.engine.business.data.BusinessDataRepository;

/**
 * @author Matthieu Chaffotte
 */
public class BusinessDataProcessDependencyResolver implements ProcessDependencyResolver {

    @Override
    public boolean resolve(final TenantServiceAccessor tenantAccessor, final BusinessArchive businessArchive, final SProcessDefinition sDefinition) {
        return checkResolution(tenantAccessor, sDefinition).isEmpty();
    }

    @Override
    public List<Problem> checkResolution(final TenantServiceAccessor tenantAccessor, final SProcessDefinition processDefinition) {
        final List<SBusinessDataDefinition> businessDataDefinitions = processDefinition.getProcessContainer().getBusinessDataDefinitions();
        if (businessDataDefinitions.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Problem> problems = new ArrayList<Problem>();
        final BusinessDataRepository businessDataRepository = ((com.bonitasoft.engine.service.TenantServiceAccessor) tenantAccessor)
                .getBusinessDataRepository();
        final Set<String> entityClassNames = businessDataRepository.getEntityClassNames();
        for (final SBusinessDataDefinition sBusinessDataDefinition : businessDataDefinitions) {
            final String className = sBusinessDataDefinition.getClassName();
            if (!entityClassNames.contains(className)) {
                final Problem problem = new ProblemImpl(Level.ERROR, sBusinessDataDefinition.getName(), "business data", "The business data '"
                        + sBusinessDataDefinition.getName() + "' with the class name '" + className + "', is not managed by the current version of the BDM");
                problems.add(problem);
            }
        }
        return problems;
    }

}
