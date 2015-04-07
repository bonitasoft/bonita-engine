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
 */
package org.bonitasoft.engine.api.impl.resolver;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.impl.internal.ProblemImpl;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Laurent Leseigneur
 */
public class PageProcessDependencyResolver implements ProcessDependencyResolver {

    public static final String ERROR_MESSAGE = "error while resolving form mapping %s";

    @Override
    public boolean resolve(final TenantServiceAccessor tenantAccessor, final BusinessArchive businessArchive, final SProcessDefinition sDefinition) {
        return true;
    }

    @Override
    public List<Problem> checkResolution(final TenantServiceAccessor tenantAccessor, final SProcessDefinition processDefinition) {
        List<Problem> problems = new ArrayList<>();
        try {
            problems = checkPageProcesResolution(tenantAccessor, processDefinition);
        } catch (SBonitaReadException | SObjectNotFoundException e) {
            problems.add(new ProblemImpl(Problem.Level.ERROR, null, null, "unable to resolve form mapping dependencies"));
        }
        return problems;
    }

    protected List<Problem> checkPageProcesResolution(TenantServiceAccessor tenantAccessor, SProcessDefinition sProcessDefinition) throws SBonitaReadException,
            SObjectNotFoundException {
        final List<Problem> problems = new ArrayList<>();
        final FormMappingService formMappingService = tenantAccessor.getFormMappingService();
        List<SFormMapping> formMappings;
        do {
            formMappings = formMappingService.list(sProcessDefinition.getId(), 0, 100);
            for (SFormMapping formMapping : formMappings) {
                checkFormMappingResolution(tenantAccessor, formMapping, sProcessDefinition.getId(), problems);
            }
        } while (formMappings.size() == 100);
        return problems;
    }

    private void checkFormMappingResolution(TenantServiceAccessor tenantAccessor, SFormMapping formMapping, long processDefinitionId, List<Problem> problems)
            throws SBonitaReadException, SObjectNotFoundException {
        if (isMappingRelatedToCustomPage(formMapping)) {
            final Long pageId = formMapping.getPageMapping().getPageId();
            addProblemIfPageIsNotFound(tenantAccessor, formMapping, processDefinitionId, problems, pageId);
        }
    }

    private void addProblemIfPageIsNotFound(TenantServiceAccessor tenantAccessor, SFormMapping formMapping, long processDefinitionId, List<Problem> problems,
            Long pageId) throws SBonitaReadException, SObjectNotFoundException {
        if (pageId == null || tenantAccessor.getPageService().getPage(pageId) == null) {
            addProblem(formMapping, processDefinitionId, problems);
        }
    }

    private void addProblem(SFormMapping formMapping, long processDefinitionId, List<Problem> problems) {
        problems.add(new ProblemImpl(Problem.Level.ERROR, processDefinitionId, "form mapping", String.format(ERROR_MESSAGE, formMapping.toString())));
    }

    private boolean isMappingRelatedToCustomPage(SFormMapping formMapping) {
        final SPageMapping pageMapping = formMapping.getPageMapping();
        return (pageMapping !=null && pageMapping.getPageId()!=null);
    }
}
