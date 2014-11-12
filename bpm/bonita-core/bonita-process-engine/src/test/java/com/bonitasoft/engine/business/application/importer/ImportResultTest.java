/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.api.ImportStatus;
import org.junit.Test;

import com.bonitasoft.engine.business.application.model.SApplication;

public class ImportResultTest {

    @Test
    public void getImportedObject_should_return_object_passed_by_constructor() throws Exception {
        //given
        final SApplication application = mock(SApplication.class);
        final ImportResult importResult = new ImportResult(application, mock(ImportStatus.class));

        // when
        final SApplication retrievedApp = importResult.getApplication();

        //then
        assertThat(retrievedApp).isEqualTo(application);
    }

    @Test
    public void getImportStatus() throws Exception {
        //given
        final ImportStatus status = mock(ImportStatus.class);
        final ImportResult importResult = new ImportResult(mock(SApplication.class), status);

        //when
        final ImportStatus retrievedStatus = importResult.getImportStatus();

        //then
        assertThat(retrievedStatus).isEqualTo(status);
    }
}
