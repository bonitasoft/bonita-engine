/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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

import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.document.DocumentService;
import org.bonitasoft.engine.document.SDocumentContentNotFoundException;
import org.bonitasoft.engine.document.SDocumentDeletionException;
import org.bonitasoft.engine.document.SDocumentStorageException;
import org.bonitasoft.engine.document.model.SDocument;
import org.bonitasoft.engine.document.model.SDocumentBuilders;
import org.bonitasoft.engine.document.model.SDocumentContent;
import org.bonitasoft.engine.document.model.SDocumentContentBuilder;
import org.bonitasoft.engine.document.model.SDocumentLogBuilder;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilders;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
public class DocumentServiceImpl implements DocumentService {

    private final Recorder recorder;

    private final SEventBuilders eventBuilders;

    private final ReadPersistenceService persistenceService;

    private final SDocumentBuilders documentBuilders;

    private final QueriableLoggerService queriableLoggerService;

    public DocumentServiceImpl(final Recorder recorder, final SEventBuilders eventBuilders, final ReadPersistenceService persistenceService,
            final SDocumentBuilders documentBuilders, final QueriableLoggerService queriableLoggerService) {
        this.recorder = recorder;
        this.eventBuilders = eventBuilders;
        this.persistenceService = persistenceService;
        this.documentBuilders = documentBuilders;
        this.queriableLoggerService = queriableLoggerService;
    }

    @Override
    public byte[] getContent(final String documentId) throws SDocumentContentNotFoundException {
        return getDocumentContent(documentId).getContent();
    }

    @Override
    public SDocument storeDocumentContent(final SDocument sDocument, final byte[] documentContent) throws SDocumentStorageException {
        final String documentId = String.valueOf(UUID.randomUUID().getLeastSignificantBits());
        final SDocumentLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "store a document content");
        final SDocumentContent sdocumentContent = createDocumentContent(documentId, documentContent);
        final InsertRecord insertRecord = new InsertRecord(sdocumentContent);
        final SInsertEvent insertEvent = (SInsertEvent) eventBuilders.getEventBuilder().createInsertEvent("SDocumentContent").setObject(sdocumentContent)
                .done();
        try {
            recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(sdocumentContent.getId(), SQueriableLog.STATUS_OK, logBuilder, "storeDocumentContent");

        } catch (final SRecorderException re) {
            initiateLogBuilder(sdocumentContent.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "storeDocumentContent");
            throw new SDocumentStorageException(re);
        }
        try {
            ClassReflector.invokeSetter(sDocument, "setId", String.class, documentId);
        } catch (final Throwable e) {
            throw new SDocumentStorageException(e);
        }
        return sDocument;
    }

    @Override
    public void deleteDocumentContent(final String documentId) throws SDocumentDeletionException, SDocumentContentNotFoundException {
        final SDocumentLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "Deleting a document content");
        SDocumentContent sdocumentContent = null;
        try {
            // sdocumentContent = getDocumentContent(sDocument.getStorageId());
            sdocumentContent = getDocumentContent(documentId);
            final DeleteRecord deleteRecord = new DeleteRecord(sdocumentContent);
            final SDeleteEvent deleteEvent = (SDeleteEvent) eventBuilders.getEventBuilder().createDeleteEvent("SDocumentContent").setObject(sdocumentContent)
                    .done();
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(sdocumentContent.getId(), SQueriableLog.STATUS_OK, logBuilder, "delete");
        } catch (final SRecorderException e) {
            initiateLogBuilder(sdocumentContent.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "delete");
            throw new SDocumentDeletionException("can't delete Document content " + sdocumentContent, e);
        }
    }

    private SDocumentContent createDocumentContent(final String storageId, final byte[] content) {
        final SDocumentContentBuilder dcontentBuilder = documentBuilders.getSDocumentContentBuilder();
        dcontentBuilder.createNewInstance();
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
                throw new SDocumentContentNotFoundException("Cannot get the DocumentContent with documentID:" + documentId);
            }
            return docContent;
        } catch (final SBonitaReadException e) {
            throw new SDocumentContentNotFoundException("Cannot get the DocumentContent with documentID:" + documentId, e);
        }
    }

    private SDocumentLogBuilder getQueriableLog(final ActionType actionType, final String message) {
        final SDocumentLogBuilder logBuilder = documentBuilders.getSDocumentLogBuilder();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.createNewInstance().actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    private void initiateLogBuilder(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String callerMethodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), callerMethodName, log);
        }
    }
}
