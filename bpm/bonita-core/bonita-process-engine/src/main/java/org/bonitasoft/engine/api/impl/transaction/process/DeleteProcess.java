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
package org.bonitasoft.engine.api.impl.transaction.process;

import java.util.List;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class DeleteProcess extends DeleteArchivedProcessInstances {

    private final ProcessDefinitionService processDefinitionService;
    private final ActorMappingService actorMappingService;
    private final ClassLoaderService classLoaderService;
    private final FormMappingService formMappingService;
    private final PageService pageService;
    private final ParameterService parameterService;

    public DeleteProcess(final TenantServiceAccessor tenantAccessor, final long processDefinitionId) {
        super(tenantAccessor, processDefinitionId);
        processDefinitionService = tenantAccessor.getProcessDefinitionService();
        actorMappingService = tenantAccessor.getActorMappingService();
        classLoaderService = tenantAccessor.getClassLoaderService();
        formMappingService = tenantAccessor.getFormMappingService();
        pageService = tenantAccessor.getPageService();
        parameterService = tenantAccessor.getParameterService();
    }

    @Override
    public void execute() throws SBonitaException {
        super.execute();
        actorMappingService.deleteActors(getProcessDefinitionId());
        parameterService.deleteAll(getProcessDefinitionId());
        deleteFormMapping();
        deleteProcessPages();
        try {
            processDefinitionService.delete(getProcessDefinitionId());
        } catch (final SProcessDefinitionNotFoundException spdnfe) {
            // ignore
        }
        classLoaderService.removeLocalClassLoader(ScopeType.PROCESS.name(), getProcessDefinitionId());
    }

    private void deleteProcessPages() throws SBonitaReadException, SObjectModificationException, SObjectNotFoundException {
        List<SPage> sPages;
        do {
            sPages = pageService.getPageByProcessDefinitionId(getProcessDefinitionId(), 0, 100);
            for (SPage sPage : sPages) {
                pageService.deletePage(sPage.getId());
            }
        } while (sPages.size() == 100);
    }

    protected void deleteFormMapping() throws SBonitaReadException, SObjectModificationException {
        List<SFormMapping> formMappings;
        do {
            formMappings = formMappingService.list(getProcessDefinitionId(), 0, 100);
            for (SFormMapping formMapping : formMappings) {
                formMappingService.delete(formMapping);
            }
        } while (formMappings.size() == 100);
    }

}
