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
package org.bonitasoft.engine.core.form.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.bonitasoft.engine.bpm.BPMServicesBuilder;
import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.bonitasoft.engine.transaction.STransactionCreationException;
import org.bonitasoft.engine.transaction.STransactionRollbackException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class FormMappingServiceIT extends CommonBPMServicesTest {

    private final SessionService sessionService;
    private final SessionAccessor sessionAccessor;
    public FormMappingService formMappingService;

    private final BPMServicesBuilder servicesBuilder;

    private final TransactionService transactionService;

    @After
    public void tearDown() throws SBonitaReadException, SObjectModificationException, STransactionCreationException,
            STransactionCommitException, STransactionRollbackException {
        transactionService.begin();
        for (SFormMapping sFormMapping : formMappingService.list(0, 1000)) {
            formMappingService.delete(sFormMapping);
        }
        transactionService.complete();
    }

    public FormMappingServiceIT() {
        super();
        servicesBuilder = getServicesBuilder();
        transactionService = servicesBuilder.getTransactionService();
        formMappingService = servicesBuilder.getFormMappingService();
        sessionService = servicesBuilder.getSessionService();
        sessionAccessor = servicesBuilder.getSessionAccessor();
    }

    @Test
    public void createAndListFormMapping() throws Exception {
        transactionService.begin();
        SFormMapping taskForm = formMappingService.create(15l, "step1", "form_page1", FormMappingTarget.INTERNAL.name(), "TASK");
        SFormMapping processStartForm = formMappingService.create(15l, null, "form_page2", FormMappingTarget.INTERNAL.name(), "PROCESS_START");
        SFormMapping processOverviewForm = formMappingService.create(15l, null, "form_page_url", FormMappingTarget.URL.name(), "PROCESS_OVERVIEW");
        SFormMapping otherProcess = formMappingService.create(16l, null, "form_page_other", FormMappingTarget.LEGACY.name(), "PROCESS_OVERVIEW");
        transactionService.complete();

        transactionService.begin();
        List<SFormMapping> list = formMappingService.list(15l, 0, 10);
        List<SFormMapping> listAll = formMappingService.list(0, 10);

        transactionService.complete();
        assertThat(list).extracting("type").containsExactly("TASK", "PROCESS_START", "PROCESS_OVERVIEW");
        assertThat(list).extracting("form").containsExactly("form_page1", "form_page2", "form_page_url");
        assertThat(listAll).extracting("type").containsExactly("TASK", "PROCESS_START", "PROCESS_OVERVIEW", "PROCESS_OVERVIEW");
        assertThat(listAll).extracting("form").containsExactly("form_page1", "form_page2", "form_page_url","form_page_other");
        assertThat(listAll).extracting("processDefinitionId").containsExactly(15l, 15l, 15l,16l);
    }

    @Test
    public void create_and_get_FormMapping() throws Exception {
        transactionService.begin();
        SFormMapping taskForm = formMappingService.create(15l, "step1", "form_page1", FormMappingTarget.INTERNAL.name(), "TASK");
        transactionService.complete();

        transactionService.begin();
        SFormMapping sFormMapping = formMappingService.get(taskForm.getId());
        SFormMapping sFormMappingByProperties = formMappingService.get(15l,"TASK","step1");
        transactionService.complete();
        assertThat(sFormMapping).isEqualTo(taskForm).isEqualTo(sFormMappingByProperties);
    }
    @Test
    public void create_and_get_FormMapping_with_no_task() throws Exception {
        transactionService.begin();
        SFormMapping taskForm = formMappingService.create(15l, null, "form_page1", FormMappingTarget.INTERNAL.name(), "TASK");
        transactionService.complete();

        transactionService.begin();
        SFormMapping sFormMapping = formMappingService.get(taskForm.getId());
        SFormMapping sFormMappingByProperties = formMappingService.get(15l,"TASK");
        List<SFormMapping> list = formMappingService.list(0, 100);
        transactionService.complete();
        assertThat(sFormMapping).isEqualTo(taskForm);
        assertThat(sFormMappingByProperties).isEqualTo(sFormMapping);
    }

    @Test
    public void delete_FormMapping() throws Exception {
        transactionService.begin();
        SFormMapping taskForm = formMappingService.create(15l, "step1", "form_page1", FormMappingTarget.INTERNAL.name(), "TASK");
        transactionService.complete();

        transactionService.begin();
        formMappingService.delete(formMappingService.get(taskForm.getId()));
        transactionService.complete();

        transactionService.begin();
        try{
            formMappingService.get(taskForm.getId());
            fail("should have thrown a not found");
        }catch(SObjectNotFoundException e){
            //ok
        }
        transactionService.complete();

    }

    @Test
    public void update_FormMapping() throws Exception {
        transactionService.begin();
        SFormMapping taskForm = formMappingService.create(15l, "step1", "form_page1", FormMappingTarget.INTERNAL.name(), "TASK");
        transactionService.complete();

        transactionService.begin();
        SFormMapping sFormMapping = formMappingService.get(taskForm.getId());
        formMappingService.update(sFormMapping, "newFormName", FormMappingTarget.URL.name());
        transactionService.complete();

        transactionService.begin();
        SFormMapping updatedInDatabase = formMappingService.get(taskForm.getId());
        transactionService.complete();

        assertThat(sFormMapping).isEqualTo(updatedInDatabase);
        assertThat(updatedInDatabase.getForm()).isEqualTo("newFormName");
        assertThat(updatedInDatabase.getTarget()).isEqualTo(FormMappingTarget.URL.name());
        assertThat(updatedInDatabase.getLastUpdateDate()).isGreaterThan(taskForm.getLastUpdateDate());

        SSession john = sessionService.createSession(1, 12, "john", false);
        sessionAccessor.setSessionInfo(john.getId(), 1);

        transactionService.begin();
        SFormMapping reupdated = formMappingService.get(taskForm.getId());
        formMappingService.update(reupdated, "newFormName2", FormMappingTarget.INTERNAL.name());
        transactionService.complete();


        assertThat(reupdated.getForm()).isEqualTo("newFormName2");
        assertThat(reupdated.getTarget()).isEqualTo(FormMappingTarget.INTERNAL.name());
        assertThat(reupdated.getLastUpdateDate()).isGreaterThan(updatedInDatabase.getLastUpdateDate());
        assertThat(reupdated.getLastUpdatedBy()).isEqualTo(12);

    }
}
