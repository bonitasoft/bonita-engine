/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.document.api.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.core.document.exception.SDocumentContentNotFoundException;
import org.bonitasoft.engine.core.document.exception.SDocumentException;
import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Vincent Elcrin
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceImplTest {

    @Mock
    private Recorder recorder;
    @Mock
    private ReadPersistenceService persistenceService;
    @Mock
    private SDocumentDownloadURLProvider urlProvider;
    @Mock
    private EventService eventService;
    @Mock
    private TechnicalLoggerService technicalLogger;
    @Mock
    private ArchiveService archiveService;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void generateDocumentURL_should_call_urlProvider() {
        doReturn("generated").when(urlProvider).generateURL("name", "docId");
        assertEquals("generated", documentService.generateDocumentURL("name", "docId"));
    }

}
