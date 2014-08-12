/**
 * Copyright (C) 2012-2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.document.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.document.DocumentService;
import org.bonitasoft.engine.document.SDocumentContentNotFoundException;
import org.bonitasoft.engine.document.SDocumentDeletionException;
import org.bonitasoft.engine.document.SDocumentStorageException;
import org.bonitasoft.engine.document.model.SDocument;
import org.bonitasoft.engine.document.model.SDocumentContent;
import org.bonitasoft.engine.document.model.SDocumentContentBuilder;
import org.bonitasoft.engine.document.model.SDocumentContentBuilderFactory;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
public class DocumentServiceImpl implements DocumentService {

    private final Recorder recorder;

    private final ReadPersistenceService persistenceService;

    public DocumentServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceService) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
    }

    @Override
    public byte[] getContent(final String documentId) throws SDocumentContentNotFoundException {
        return getDocumentContent(documentId).getContent();
    }

    @Override
    public SDocument storeDocumentContent(final SDocument sDocument, final byte[] documentContent) throws SDocumentStorageException {
        final String documentId = String.valueOf(UUID.randomUUID().getLeastSignificantBits());
        final SDocumentContent sdocumentContent = createDocumentContent(documentId, documentContent);
        final InsertRecord insertRecord = new InsertRecord(sdocumentContent);
        final SInsertEvent insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent("SDocumentContent")
                .setObject(sdocumentContent)
                .done();
        try {
            recorder.recordInsert(insertRecord, insertEvent);
        } catch (final SRecorderException re) {
            throw new SDocumentStorageException(re);
        }
        try {
            ClassReflector.invokeSetter(sDocument, "setId", String.class, documentId);
        } catch (final Exception e) {
            throw new SDocumentStorageException(e);
        }
        return sDocument;
    }

    @Override
    public void deleteDocumentContent(final String documentId) throws SDocumentDeletionException, SDocumentContentNotFoundException {
        SDocumentContent sdocumentContent = null;
        try {
            sdocumentContent = getDocumentContent(documentId);
            final DeleteRecord deleteRecord = new DeleteRecord(sdocumentContent);
            final SDeleteEvent deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent("SDocumentContent")
                    .setObject(sdocumentContent)
                    .done();
            recorder.recordDelete(deleteRecord, deleteEvent);
        } catch (final SRecorderException e) {
            throw new SDocumentDeletionException("can't delete Document content " + sdocumentContent, e);
        }
    }

    private SDocumentContent createDocumentContent(final String storageId, final byte[] content) {
        final SDocumentContentBuilder dcontentBuilder = BuilderFactory.get(SDocumentContentBuilderFactory.class).createNewInstance();
        dcontentBuilder.setStorageId(storageId).setContent(content);
        return dcontentBuilder.done();
    }

    private SDocumentContent getDocumentContent(final String documentId) throws SDocumentContentNotFoundException {
        NullCheckingUtil.checkArgsNotNull(documentId);
        final Map<String, Object> inputParameters = new HashMap<String, Object>();
        inputParameters.put("documentId", documentId);
        try {
            final SelectOneDescriptor<SDocumentContent> selectDescriptor = new SelectOneDescriptor<SDocumentContent>("getDocContentByDocumentId",
                    inputParameters, SDocumentContent.class);
            final SDocumentContent docContent = persistenceService.selectOne(selectDescriptor);
            if (docContent == null) {
                throw new SDocumentContentNotFoundException(documentId);
            }
            return docContent;
        } catch (final SBonitaReadException e) {
            throw new SDocumentContentNotFoundException(documentId, e);
        }
    }

}
