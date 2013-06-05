/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.document.impl;

import org.bonitasoft.engine.document.SDocumentContentNotFoundException;
import org.bonitasoft.engine.document.SDocumentException;
import org.bonitasoft.engine.document.model.SDocumentBuilders;
import org.bonitasoft.engine.document.model.SDocumentContent;
import org.bonitasoft.engine.events.model.builders.SEventBuilders;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;

/**
 * @author Celine Souchet
 * 
 */
public class DocumentServiceImplTest {

    private Recorder recorder;

    private ReadPersistenceService persistence;

    private QueriableLoggerService queriableLoggerService;

    private SEventBuilders eventBuilders;

    private SDocumentBuilders documentBuilders;

    private TechnicalLoggerService logger;

    private DocumentServiceImpl documentServiceImpl;

    @Before
    public void setUp() throws Exception {
        recorder = mock(Recorder.class);
        persistence = mock(ReadPersistenceService.class);
        queriableLoggerService = mock(QueriableLoggerService.class);
        eventBuilders = mock(SEventBuilders.class);
        documentBuilders = mock(SDocumentBuilders.class);
        logger = mock(TechnicalLoggerService.class);
        documentServiceImpl = new DocumentServiceImpl(recorder, eventBuilders, persistence, documentBuilders, logger, queriableLoggerService);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.document.impl.DocumentServiceImpl#getContent(java.lang.String)}.
     */
    @Test
    public final void getContent() throws SBonitaReadException, SDocumentContentNotFoundException, SDocumentException {
        final SDocumentContent sDocumentContent = mock(SDocumentContent.class);
        final byte[] content = { 2 };
        when(sDocumentContent.getContent()).thenReturn(content);
        when(persistence.selectOne(any(SelectOneDescriptor.class))).thenReturn(sDocumentContent);

        Assert.assertEquals(content, documentServiceImpl.getContent("documentId"));
    }

    @Test(expected = SDocumentContentNotFoundException.class)
    public final void getContentNotExists() throws SBonitaReadException, SDocumentContentNotFoundException, SDocumentException {
        when(persistence.selectOne(any(SelectOneDescriptor.class))).thenReturn(null);

        documentServiceImpl.getContent("documentId");
    }

    @Test(expected = SDocumentException.class)
    public final void getContentThrowException() throws SBonitaReadException, SDocumentContentNotFoundException, SDocumentException {
        when(persistence.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException(""));

        documentServiceImpl.getContent("documentId");
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.document.impl.DocumentServiceImpl#storeDocumentContent(org.bonitasoft.engine.document.model.SDocument, byte[])}.
     */
    @Test
    public final void storeDocumentContent() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.document.impl.DocumentServiceImpl#deleteDocumentContent(java.lang.String)}.
     */
    @Test
    public final void deleteDocumentContent() {
        // TODO : Not yet implemented
    }

}
