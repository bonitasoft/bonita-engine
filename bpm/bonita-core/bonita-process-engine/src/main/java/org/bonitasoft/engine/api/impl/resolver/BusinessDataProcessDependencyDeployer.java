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
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.Problem.Level;
import org.bonitasoft.engine.bpm.process.impl.internal.ProblemImpl;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.core.process.definition.model.SBusinessDataDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 */
public class BusinessDataProcessDependencyDeployer implements ProcessDependencyDeployer {

    @Override
    public boolean deploy(final TenantServiceAccessor tenantAccessor, final BusinessArchive businessArchive, final SProcessDefinition processDefinition) {
        return checkResolution(tenantAccessor, processDefinition).isEmpty();
    }

    @Override
    public List<Problem> checkResolution(final TenantServiceAccessor tenantAccessor, final SProcessDefinition processDefinition) {
        final List<SBusinessDataDefinition> businessDataDefinitions = processDefinition.getProcessContainer().getBusinessDataDefinitions();
        if (businessDataDefinitions.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Problem> problems = new ArrayList<Problem>();
        final BusinessDataRepository businessDataRepository =  tenantAccessor.getBusinessDataRepository();
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
