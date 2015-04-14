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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Aurelien Pupier
 */
@RunWith(MockitoJUnitRunner.class)
public class DeleteProcessTest {

    public static final long PROCESS_DEFINITION_ID = 1L;
    public static final long PAGE_ID = 2L;
    @Mock
    private TenantServiceAccessor tenantAccessor;
    @Mock
    private ClassLoaderService classLoaderService;
    @Mock
    private ProcessDefinitionService processDefinitionService;
    @Mock
    private ActorMappingService actorMappingService;
    @Mock
    private FormMappingService formMappingService;
    @Mock
    private ProcessInstanceService processInstanceService;
    @Mock
    private PageService pageService;
    @Mock
    private SPage sPage;
    @Mock
    private SFormMapping formMapping;

    @Before
    public void before() {
        when(tenantAccessor.getClassLoaderService()).thenReturn(classLoaderService);
        when(tenantAccessor.getProcessDefinitionService()).thenReturn(processDefinitionService);
        when(tenantAccessor.getActorMappingService()).thenReturn(actorMappingService);
        when(tenantAccessor.getFormMappingService()).thenReturn(formMappingService);
        when(tenantAccessor.getProcessInstanceService()).thenReturn(processInstanceService);
        when(tenantAccessor.getPageService()).thenReturn(pageService);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.transaction.process.DeleteProcess#execute()}.
     * Make sure we will always think to clean ClassloaderSrvice when deleting a process.
     * 
     * @throws SBonitaException
     */
    @Test
    public void testClassloaderClearedWhenExecuteCalled() throws SBonitaException {
        // given
        final long processDefinitionId = PROCESS_DEFINITION_ID;
        final DeleteProcess deleteProcess = new DeleteProcess(tenantAccessor, processDefinitionId);

        //when
        deleteProcess.execute();

        //then
        verify(classLoaderService).removeLocalClassLoader(ScopeType.PROCESS.name(), processDefinitionId);
    }

    @Test
    public void should_delete_process_delete_pages() throws SBonitaException {
        //given
        final long processDefinitionId = PROCESS_DEFINITION_ID;
        doReturn(PAGE_ID).when(sPage).getId();
        doReturn(Arrays.asList(sPage)).when(pageService).getPageByProcessDefinitionId(eq(PROCESS_DEFINITION_ID), anyInt(), anyInt());

        //when
        final DeleteProcess deleteProcess = new DeleteProcess(tenantAccessor, processDefinitionId);
        deleteProcess.execute();

        //then
        verify(pageService).deletePage(PAGE_ID);
    }

    @Test
    public void should_delete_process_form_mapping() throws SBonitaException {
        //given
        final long processDefinitionId = PROCESS_DEFINITION_ID;

        doReturn(Arrays.asList(formMapping)).when(formMappingService).list(eq(PROCESS_DEFINITION_ID), anyInt(), anyInt());

        //when
        final DeleteProcess deleteProcess = new DeleteProcess(tenantAccessor, processDefinitionId);
        deleteProcess.execute();

        //then
        verify(formMappingService).delete(formMapping);
    }

}
