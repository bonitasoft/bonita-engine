/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.console.common.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportError.Type;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.common.model.ImportStatusMessages;
import org.junit.*;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ImportStatusMessagesTest {

    @BeforeClass
    public static void initEnvironment() {
        HashMap<String, String> availableLocales;
        availableLocales = new HashMap<>();
        availableLocales.put("en", "English");
        availableLocales.put("fr", "Français");
        availableLocales.put("es", "Español");
        availableLocales.put("pt_BR", "Português (Brasil)");
        availableLocales.put("ja", "日本語");

        I18n i18n = mock(I18n.class);
        I18n.setInstance(i18n);
        Mockito.when(i18n.getAvailableLocalesFor(anyString())).thenReturn(availableLocales);
    }

    @AfterClass
    public static void cleanUp() throws Exception {
        I18n.setInstance(null);
    }

    @Before
    public void init()
            throws InvalidSessionException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void should_put_elements_Added_or_replaced_in_added_map() throws Exception {
        //given
        final ImportStatus statusAdded1 = new ImportStatus("statusAdded1");
        statusAdded1.setStatus(ImportStatus.Status.ADDED);
        final ImportStatus statusAdded2 = new ImportStatus("statusAdded2");
        statusAdded2.setStatus(ImportStatus.Status.REPLACED);

        final List<ImportStatus> importStatus = new ArrayList<>(Arrays.asList(statusAdded1, statusAdded2));

        //when
        final ImportStatusMessages importStatusMessages = new ImportStatusMessages(importStatus);

        //then
        assertThat(importStatusMessages.getImported().size()).isEqualTo(2);
        assertThat(importStatusMessages.getImported().get(0).getName()).isEqualTo("statusAdded1");
        assertThat(importStatusMessages.getImported().get(0).getStatusType())
                .isEqualTo(ImportStatus.Status.ADDED.name());
        assertThat(importStatusMessages.getImported().get(1).getName()).isEqualTo("statusAdded2");
        assertThat(importStatusMessages.getImported().get(1).getStatusType())
                .isEqualTo(ImportStatus.Status.REPLACED.name());
        assertThat(importStatusMessages.getSkipped().size()).isEqualTo(0);
        assertThat(importStatusMessages.getErrors().size()).isEqualTo(0);

    }

    @Test
    public void should_put_elements_skipped_in_skipped_map() throws Exception {
        //given
        final ImportStatus status1 = new ImportStatus("statusSkipped1");
        status1.setStatus(ImportStatus.Status.SKIPPED);
        final ImportStatus status2 = new ImportStatus("statusSkipped2");
        status2.setStatus(ImportStatus.Status.SKIPPED);

        final List<ImportStatus> importStatus = new ArrayList<>(Arrays.asList(status1, status2));

        //when
        final ImportStatusMessages importStatusMessages = new ImportStatusMessages(importStatus);

        //then
        assertThat(importStatusMessages.getSkipped().size()).isEqualTo(2);
        assertThat(importStatusMessages.getSkipped().get(0).getName()).isEqualTo("statusSkipped1");
        assertThat(importStatusMessages.getSkipped().get(0).getStatusType())
                .isEqualTo(ImportStatus.Status.SKIPPED.name());
        assertThat(importStatusMessages.getSkipped().get(1).getName()).isEqualTo("statusSkipped2");
        assertThat(importStatusMessages.getSkipped().get(1).getStatusType())
                .isEqualTo(ImportStatus.Status.SKIPPED.name());
        assertThat(importStatusMessages.getImported().size()).isEqualTo(0);
        assertThat(importStatusMessages.getErrors().size()).isEqualTo(0);

    }

    @Test
    public void should_put_elements_With_error_in_error_map() throws Exception {
        //given
        final ImportStatus status1 = new ImportStatus("statusError1");
        status1.addError(new ImportError("Error1", Type.GROUP));
        status1.addError(new ImportError("Error2", Type.GROUP));
        status1.addError(new ImportError("Error3", Type.ROLE));
        status1.addError(new ImportError("Error4", Type.USER));
        final ImportStatus status2 = new ImportStatus("statusError2");
        status2.addError(new ImportError("Error1", Type.PAGE));

        final List<ImportStatus> importStatus = new ArrayList<>(Arrays.asList(status1, status2));

        //when
        final ImportStatusMessages importStatusMessages = new ImportStatusMessages(importStatus);

        //then
        assertThat(importStatusMessages.getErrors().size()).isEqualTo(2);
        assertThat(importStatusMessages.getErrors().get(0).getName()).isEqualTo("statusError1");
        assertThat(importStatusMessages.getErrors().get(0).getStatusType()).isEqualTo(ImportStatus.Status.ADDED.name());
        assertThat(importStatusMessages.getErrors().get(0).getErrors().size()).isEqualTo(3);
        assertThat(importStatusMessages.getErrors().get(0).getErrors().get(Type.GROUP.name()).get(0))
                .isEqualTo("Error1");
        assertThat(importStatusMessages.getErrors().get(0).getErrors().get(Type.GROUP.name()).get(1))
                .isEqualTo("Error2");
        assertThat(importStatusMessages.getErrors().get(0).getErrors().get(Type.ROLE.name()).get(0))
                .isEqualTo("Error3");
        assertThat(importStatusMessages.getErrors().get(0).getErrors().get(Type.USER.name()).get(0))
                .isEqualTo("Error4");
        assertThat(importStatusMessages.getErrors().get(1).getName()).isEqualTo("statusError2");
        assertThat(importStatusMessages.getErrors().get(1).getStatusType()).isEqualTo(ImportStatus.Status.ADDED.name());
        assertThat(importStatusMessages.getErrors().get(1).getErrors().size()).isEqualTo(1);
        assertThat(importStatusMessages.getErrors().get(1).getErrors().get(Type.PAGE.name()).get(0))
                .isEqualTo("Error1");
        assertThat(importStatusMessages.getImported().size()).isEqualTo(0);
        assertThat(importStatusMessages.getSkipped().size()).isEqualTo(0);

    }

}
