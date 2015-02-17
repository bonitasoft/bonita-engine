/*
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.bonitasoft.engine.core.form.impl;

import org.bonitasoft.engine.bpm.BPMServicesBuilder;
import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.bonitasoft.engine.transaction.STransactionCreationException;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.transaction.STransactionRollbackException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class FormMappingServiceIT extends CommonBPMServicesTest {

    public FormMappingService formMappingService;


    private final BPMServicesBuilder servicesBuilder;

    private final TransactionService transactionService;

    @Before
    public void setup() throws AlreadyExistsException, CreationException {
    }

    @After
    public void tearDown() throws DeletionException {
        try {
            transactionService.begin();
            transactionService.complete();
        } catch (STransactionException e) {
            throw new DeletionException(e);
        }
    }

    public FormMappingServiceIT() {
        super();
        servicesBuilder = getServicesBuilder();
        transactionService = servicesBuilder.getTransactionService();
        formMappingService = servicesBuilder.getFormMappingService();
    }

    @Ignore("NYI")
    @Test
    public void createAndDeleteFormMapping() throws Exception {
        transactionService.begin();
        SFormMapping taskForm = formMappingService.create(15l, "step1", "form_page1", false, "TASK");
        SFormMapping processStartForm = formMappingService.create(15l, null, "form_page2", false, "PROCESS_START");
        SFormMapping processOverviewForm = formMappingService.create(15l, null, "form_page_url", true, "PROCESS_OVERVIEW");
        SFormMapping otherProcess = formMappingService.create(16l, null, "form_page_url", true, "PROCESS_OVERVIEW");
        transactionService.complete();

        transactionService.begin();
        //FIXME implement me
        formMappingService.list(15l, 0, 10);
        transactionService.complete();
    }
}
