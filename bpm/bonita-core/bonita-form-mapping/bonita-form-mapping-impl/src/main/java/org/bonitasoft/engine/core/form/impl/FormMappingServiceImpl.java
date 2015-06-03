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
 */
package org.bonitasoft.engine.core.form.impl;

import static org.bonitasoft.engine.page.AuthorizationRuleConstants.IS_ACTOR_INITIATOR;
import static org.bonitasoft.engine.page.AuthorizationRuleConstants.IS_ADMIN;
import static org.bonitasoft.engine.page.AuthorizationRuleConstants.IS_INVOLVED_IN_PROCESS_INSTANCE;
import static org.bonitasoft.engine.page.AuthorizationRuleConstants.IS_PROCESS_INITIATOR;
import static org.bonitasoft.engine.page.AuthorizationRuleConstants.IS_PROCESS_OWNER;
import static org.bonitasoft.engine.page.AuthorizationRuleConstants.IS_TASK_AVAILABLE_FOR_USER;
import static org.bonitasoft.engine.page.AuthorizationRuleConstants.IS_TASK_PERFORMER;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SDeletionException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.form.FormMappingKeyGenerator;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.page.PageMappingService;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;

/**
 * @author Baptiste Mesta
 */
public class FormMappingServiceImpl implements FormMappingService {

    public static final String FORM_MAPPING = "FORM_MAPPING";

    private final Recorder recorder;
    private final ReadPersistenceService persistenceService;
    private final SessionService sessionService;
    private final ReadSessionAccessor sessionAccessor;
    private final PageMappingService pageMappingService;
    private final PageService pageService;
    private final FormMappingKeyGenerator formMappingKeyGenerator;
    private final String externalUrlAdapter;
    private final String legacyUrlAdapter;
    private final Map<FormMappingType, List<String>> authorizationRulesMap;
    private QueriableLoggerService queriableLoggerService;

    public FormMappingServiceImpl(Recorder recorder, ReadPersistenceService persistenceService, SessionService sessionService,
            ReadSessionAccessor sessionAccessor, PageMappingService pageMappingService, PageService pageService,
            FormMappingKeyGenerator formMappingKeyGenerator, String externalUrlAdapter, String legacyUrlAdapter, QueriableLoggerService queriableLoggerService) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.sessionService = sessionService;
        this.sessionAccessor = sessionAccessor;
        this.pageMappingService = pageMappingService;
        this.pageService = pageService;
        this.formMappingKeyGenerator = formMappingKeyGenerator;
        this.externalUrlAdapter = externalUrlAdapter;
        this.legacyUrlAdapter = legacyUrlAdapter;
        this.queriableLoggerService = queriableLoggerService;

        authorizationRulesMap = new HashMap<>(3);
        authorizationRulesMap.put(FormMappingType.PROCESS_START, Arrays.asList(IS_ADMIN, IS_PROCESS_OWNER, IS_ACTOR_INITIATOR));
        authorizationRulesMap.put(FormMappingType.PROCESS_OVERVIEW,
                Arrays.asList(IS_ADMIN, IS_PROCESS_OWNER, IS_PROCESS_INITIATOR, IS_TASK_PERFORMER, IS_INVOLVED_IN_PROCESS_INSTANCE));
        authorizationRulesMap.put(FormMappingType.TASK, Arrays.asList(IS_ADMIN, IS_PROCESS_OWNER, IS_TASK_AVAILABLE_FOR_USER));
    }

    @Override
    public SFormMapping create(long processDefinitionId, String task, Integer type, String target, String form)
            throws SBonitaReadException, SObjectCreationException {
        if (target == null) {
            throw new IllegalArgumentException("Illegal form target " + target);
        }
        SPageMapping sPageMapping;
        String key = formMappingKeyGenerator.generateKey(processDefinitionId, task, type);
        List<String> authorizationRules = buildAuthorizationRules(type);
        switch (target) {
            case SFormMapping.TARGET_INTERNAL:
                sPageMapping = pageMappingService.create(key, getPageIdOrNull(form, processDefinitionId), authorizationRules);
                break;
            case SFormMapping.TARGET_URL:
                sPageMapping = pageMappingService.create(key, form, externalUrlAdapter, authorizationRules);
                break;
            case SFormMapping.TARGET_LEGACY:
                sPageMapping = pageMappingService.create(key, null, legacyUrlAdapter, null);
                break;
            case SFormMapping.TARGET_UNDEFINED:
                sPageMapping = null;
                break;
            default:
                throw new IllegalArgumentException("Illegal form target " + target);

        }
        SFormMappingImpl sFormMapping = new SFormMappingImpl(processDefinitionId, type, task);
        insertFormMapping(sFormMapping, sPageMapping);
        return sFormMapping;
    }

    private List<String> buildAuthorizationRules(Integer type) {
        return authorizationRulesMap.get(FormMappingType.getTypeFromId(type));
    }

    Long getPageIdOrNull(String form, long processDefinitionId) throws SBonitaReadException {
        SPage pageByName = pageService.getPageByNameAndProcessDefinitionId(form, processDefinitionId);
        if (pageByName == null) {
            pageByName = pageService.getPageByName(form);
        }
        return pageByName == null ? null : pageByName.getId();
    }

    private void insertFormMapping(SFormMappingImpl sFormMapping, SPageMapping sPageMapping) throws SObjectCreationException {
        InsertRecord record = new InsertRecord(sFormMapping);
        sFormMapping.setPageMapping(sPageMapping);
        final SInsertEvent insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(FORM_MAPPING)
                .setObject(sFormMapping)
                .done();
        try {
            recorder.recordInsert(record, insertEvent);
        } catch (SRecorderException e) {
            throw new SObjectCreationException(e);
        }
    }

    @Override
    public void update(SFormMapping formMapping, String url, Long pageId) throws SObjectModificationException {
        final SUpdateEvent updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(FORM_MAPPING).setObject(formMapping)
                .done();
        checkExclusiveUrlOrPageId(url, pageId);
        String urlAdapter = checkAndGetUrlAdapter(url);
        checkThatInternalPageExists(pageId);
        FormMappingLogBuilder logBuilder = getLogBuilder(ActionType.UPDATED);
        EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();

        try {
            Long oldPageId = null;
            String oldUrlAdapter = null;
            String oldUrl = null;
            entityUpdateDescriptor.addField("lastUpdatedBy", getSessionUserId());
            entityUpdateDescriptor.addField("lastUpdateDate", System.currentTimeMillis());
            // case where page mapping did not exist already (TARGET == UNDEFINED):
            if (formMapping.getPageMapping() == null) {
                SPageMapping sPageMapping = createPageMappingForExistingFormMapping(formMapping, url, pageId);
                ((SFormMappingImpl) formMapping).setPageMapping(sPageMapping);
            } else {
                oldPageId = formMapping.getPageMapping().getPageId();
                oldUrlAdapter = formMapping.getPageMapping().getUrlAdapter();
                oldUrl = formMapping.getPageMapping().getUrl();
                // Update the existing page mapping:
                entityUpdateDescriptor.addField("pageMapping.url", url);
                entityUpdateDescriptor.addField("pageMapping.urlAdapter", urlAdapter);
                entityUpdateDescriptor.addField("pageMapping.pageId", pageId);
            }
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(formMapping, entityUpdateDescriptor);
            recorder.recordUpdate(updateRecord, updateEvent);
            final String rawMessage = "Previous: pageId=<" + oldPageId + "> urlAdapter=<" + oldUrlAdapter + "> url=<"
                    + oldUrl + ">";
            log(formMapping, SQueriableLog.STATUS_OK, logBuilder, "update", truncate(rawMessage));
        } catch (SBonitaException e) {
            log(formMapping, SQueriableLog.STATUS_FAIL, logBuilder, "update", "failed to update");
            throw new SObjectModificationException(e);
        }
    }

    String truncate(String rawMessage) {
        return rawMessage.substring(0,Math.min(255,rawMessage.length()));
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder) {
        logBuilder.actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    private FormMappingLogBuilder getLogBuilder(final ActionType actionType) {
        final FormMappingLogBuilder logBuilder = new FormMappingLogBuilder();
        this.initializeLogBuilder(logBuilder);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    private void log(final SFormMapping formMapping, final int sQueriableLogStatus, final FormMappingLogBuilder logBuilder, final String callerMethodName,
            String rawMessage) {
        logBuilder.actionScope(String.valueOf(formMapping.getProcessDefinitionId()));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(formMapping.getId());
        logBuilder.rawMessage(rawMessage);
        final SQueriableLog log = logBuilder.done();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), callerMethodName, log);
        }
    }

    protected SPageMapping createPageMappingForExistingFormMapping(SFormMapping formMapping, String url, Long pageId) throws SObjectCreationException {
        String key = formMappingKeyGenerator.generateKey(formMapping.getProcessDefinitionId(), formMapping.getTask(), formMapping.getType());
        if (url != null) {
            return pageMappingService.create(key, url, externalUrlAdapter, buildAuthorizationRules(formMapping.getType()));
        } else {
            return pageMappingService.create(key, pageId, buildAuthorizationRules(formMapping.getType()));
        }
    }

    protected void checkThatInternalPageExists(Long pageId) throws SObjectModificationException {
        if (pageId != null) {
            try {
                pageService.getPage(pageId);
            } catch (SBonitaReadException | SObjectNotFoundException e) {
                throw new SObjectModificationException("the page with id " + pageId + " does not exists");
            }
        }
    }

    protected String checkAndGetUrlAdapter(String url) throws SObjectModificationException {
        if (url == null) {
            return null;
        }
        checkUrlNotEmpty(url);
        return externalUrlAdapter;
    }

    protected void checkUrlNotEmpty(String url) throws SObjectModificationException {
        if (url.isEmpty()) {
            throw new SObjectModificationException("Can't have an empty url");
        }
    }

    protected void checkExclusiveUrlOrPageId(String url, Long pageId) throws SObjectModificationException {
        if (!((url != null) ^ (pageId != null))) {
            throw new SObjectModificationException("Can't update the form mapping with both url and pageId");
        }
    }

    private long getSessionUserId() throws SSessionNotFoundException, SessionIdNotSetException {
        return sessionService.getLoggedUserFromSession(sessionAccessor);
    }

    @Override
    public void delete(SFormMapping formMapping) throws SObjectModificationException {
        final SDeleteEvent deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(FORM_MAPPING).setObject(formMapping)
                .done();
        try {
            recorder.recordDelete(new DeleteRecord(formMapping), deleteEvent);
            if (formMapping.getPageMapping() != null) {
                pageMappingService.delete(formMapping.getPageMapping());
            }
        } catch (SRecorderException | SDeletionException e) {
            throw new SObjectModificationException(e);
        }
    }

    @Override
    public SFormMapping get(long formMappingId) throws SBonitaReadException, SObjectNotFoundException {
        SFormMapping getFormMappingById = persistenceService.selectById(new SelectByIdDescriptor<>("getFormMappingById", SFormMapping.class,
                formMappingId));
        if (getFormMappingById == null) {
            throw new SObjectNotFoundException(formMappingId);
        }
        return getFormMappingById;
    }

    @Override
    public SFormMapping get(String key) throws SBonitaReadException, SObjectNotFoundException {
        return persistenceService.selectOne(new SelectOneDescriptor<SFormMapping>("getFormMappingByKey", Collections.<String, Object> singletonMap("key", key),
                SFormMapping.class));
    }

    @Override
    public SFormMapping get(long processDefinitionId, Integer type, String task) throws SBonitaReadException {
        Map<String, Object> parameters = new HashMap<String, Object>(3);
        parameters.put("processDefinitionId", processDefinitionId);
        parameters.put("type", type);
        parameters.put("task", task);
        return persistenceService.selectOne(new SelectOneDescriptor<SFormMapping>("getFormMappingOfProcessDefinitionOnTask", parameters, SFormMapping.class));
    }

    @Override
    public SFormMapping get(long processDefinitionId, Integer type) throws SBonitaReadException {
        Map<String, Object> parameters = new HashMap<String, Object>(3);
        parameters.put("processDefinitionId", processDefinitionId);
        parameters.put("type", type);
        return persistenceService.selectOne(new SelectOneDescriptor<SFormMapping>("getFormMappingOfProcessDefinition", parameters, SFormMapping.class));
    }

    @Override
    public List<SFormMapping> list(long processDefinitionId, int fromIndex, int numberOfResults) throws SBonitaReadException {
        Map<String, Object> parameters = new HashMap<String, Object>(3);
        parameters.put("processDefinitionId", processDefinitionId);
        return persistenceService.selectList(new SelectListDescriptor<SFormMapping>("getFormMappingsOfProcessDefinition", parameters, SFormMapping.class,
                new QueryOptions(
                        fromIndex, numberOfResults)));
    }

    @Override
    public List<SFormMapping> list(int fromIndex, int numberOfResults) throws SBonitaReadException {
        Map<String, Object> parameters = new HashMap<String, Object>(3);
        return persistenceService.selectList(new SelectListDescriptor<SFormMapping>("getFormMappings", parameters, SFormMapping.class, new QueryOptions(
                fromIndex, numberOfResults)));
    }

    @Override
    public long getNumberOfFormMappings(QueryOptions queryOptions) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SFormMapping.class, queryOptions, Collections.<String, Object> emptyMap());
    }

    @Override
    public List<SFormMapping> searchFormMappings(QueryOptions queryOptions) throws SBonitaReadException {
        return persistenceService.searchEntity(SFormMapping.class, queryOptions, Collections.<String, Object> emptyMap());
    }

    private static class FormMappingLogBuilder extends CRUDELogBuilder implements SPersistenceLogBuilder {

        /*
         * resulting log example
         * 1 54 1433257801158 2015 6 153 23 william.jobs 1 7.0.0-SNAPSHOT INTERNAL FORM_MAPPING_UPDATED 1 1 update the formmapping
         * org.bonitasoft.engine.core.form.impl.FormMappingServiceImpl update -1 -1 1 -1 -1
         */
        @Override
        protected String getActionTypePrefix() {
            return FORM_MAPPING;
        }

        @Override
        protected void checkExtraRules(SQueriableLog log) {
        }

        @Override
        public SPersistenceLogBuilder objectId(long objectId) {
            queriableLogBuilder.numericIndex(2, objectId);
            return this;
        }
    }
}
