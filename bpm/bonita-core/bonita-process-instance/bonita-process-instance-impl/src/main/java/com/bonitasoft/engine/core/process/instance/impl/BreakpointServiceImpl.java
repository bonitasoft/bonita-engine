/*******************************************************************************
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.process.instance.impl;

import java.io.Serializable;
import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.transaction.TransactionState;

import com.bonitasoft.engine.core.process.instance.api.BreakpointService;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SBreakpointCreationException;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SBreakpointDeletionException;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SBreakpointNotFoundException;
import com.bonitasoft.engine.core.process.instance.model.breakpoint.SBreakpoint;
import com.bonitasoft.engine.core.process.instance.model.builder.SBreakpointBuilderFactory;
import com.bonitasoft.engine.core.process.instance.model.builder.SBreakpointLogBuilder;
import com.bonitasoft.engine.core.process.instance.model.builder.SBreakpointLogBuilderFactory;
import com.bonitasoft.engine.core.process.instance.recorder.SelectDescriptorBuilderExt;

/**
 * @author Baptiste Mesta
 */
public class BreakpointServiceImpl implements BreakpointService {

    /**
     * @author Baptiste Mesta
     */
    private static final class ResetBreakpointFlag implements BonitaTransactionSynchronization {

        private final BreakpointServiceImpl breakpointServiceImpl;

        public ResetBreakpointFlag(final BreakpointServiceImpl breakpointServiceImpl) {
            this.breakpointServiceImpl = breakpointServiceImpl;
        }

        @Override
        public void beforeCommit() {
        }

        @Override
        public void afterCompletion(final TransactionState transactionState) {
            if (TransactionState.COMMITTED.equals(transactionState)) {
                this.breakpointServiceImpl.isBreakpointsSynched = false;
            }
        }
    }

    private static final String INSTANCE_KEY = "INST_";

    private static final String DEFINITION_KEY = "DEF_";

    private static final int BATCH_SIZE = 100;

    private static final String BREAKPOINTS = "BREAKPOINTS_BY_INSTANCE";

    private static final Serializable BREAKPOINTS_ACTIVE = "BREAKPOINTS_ACTIVE";

    private final EventService eventService;

    private final Recorder recorder;

    private final ReadPersistenceService persistenceRead;

    private boolean isBreakpointsSynched = false;

    private final CacheService cacheService;

    private final String defaultSortingField;

    private final OrderByType defaulOrder;

    private final QueriableLoggerService queriableLoggerService;

    private final TransactionService transactionService;

    public BreakpointServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceRead, final EventService eventService,
            final CacheService cacheService, final QueriableLoggerService queriableLoggerService,
            final TransactionService transactionService) {
        this.recorder = recorder;
        this.persistenceRead = persistenceRead;
        this.eventService = eventService;
        this.cacheService = cacheService;
        this.queriableLoggerService = queriableLoggerService;
        this.transactionService = transactionService;
        this.defaultSortingField = BuilderFactory.get(SBreakpointBuilderFactory.class).getDefinitionIdKey();
        this.defaulOrder = OrderByType.ASC;
    }

    @Override
    public SBreakpoint addBreakpoint(final SBreakpoint breakpoint) throws SBreakpointCreationException {
        final SBreakpointLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, NEW_BREAKPOINT_ADDED, breakpoint);
        final InsertRecord insertRecord = new InsertRecord(breakpoint);
        SInsertEvent insertEvent = null;
        if (this.eventService.hasHandlers(BREAKPOINT, EventActionType.CREATED)) {
            insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(BREAKPOINT).setObject(breakpoint).done();
        }
        try {
            this.recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(breakpoint.getId(), SQueriableLog.STATUS_OK, logBuilder, "addBreakpoint");
            this.transactionService.registerBonitaSynchronization(new ResetBreakpointFlag(this));
            this.isBreakpointsSynched = false;// FIXME register that in a synchronization to refresh breakpoints after commit
        } catch (final SBonitaException e) {
            initiateLogBuilder(breakpoint.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "addBreakpoint");
            throw new SBreakpointCreationException(e);
        }
        return breakpoint;
    }

    @Override
    public void removeBreakpoint(final long id) throws SBreakpointDeletionException, SBreakpointNotFoundException, SBonitaReadException {
        final SBreakpoint sBreakpoint = getBreakpoint(id);
        SDeleteEvent deleteEvent = null;
        if (this.eventService.hasHandlers(BREAKPOINT, EventActionType.DELETED)) {
            deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(BREAKPOINT).setObject(sBreakpoint).done();
        }
        final DeleteRecord deleteRecord = new DeleteRecord(sBreakpoint);
        final SBreakpointLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, REMOVING_BREAKPOINT, sBreakpoint);
        try {
            this.recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(id, SQueriableLog.STATUS_OK, logBuilder, "removeBreakpoint");
            this.isBreakpointsSynched = false;
        } catch (final SRecorderException e) {
            initiateLogBuilder(id, SQueriableLog.STATUS_FAIL, logBuilder, "removeBreakpoint");
            throw new SBreakpointDeletionException(e);
        }
    }

    @Override
    public SBreakpoint getBreakpoint(final long id) throws SBreakpointNotFoundException, SBonitaReadException {
        final SBreakpoint breakpoint = this.persistenceRead.selectById(SelectDescriptorBuilderExt.getElementById(SBreakpoint.class, "Breakpoint", id));
        if (breakpoint == null) {
            throw new SBreakpointNotFoundException(id);
        }
        return breakpoint;
    }

    protected <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    protected <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    protected SBreakpointLogBuilder getQueriableLog(final ActionType actionType, final String message, final SBreakpoint breakpoint) {
        final SBreakpointLogBuilder logBuilder = BuilderFactory.get(SBreakpointLogBuilderFactory.class).createNewInstance();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        logBuilder.definitionId(breakpoint.getDefinitionId());
        return logBuilder;
    }

    @Override
    public boolean isBreakpointActive() throws SBonitaReadException {
        try {
            Boolean active = (Boolean) this.cacheService.get(BREAKPOINTS, BREAKPOINTS_ACTIVE);
            if (!this.isBreakpointsSynched || active == null) {// active might be out of cache
                synchronizeBreakpoints();
                active = (Boolean) this.cacheService.get(BREAKPOINTS, BREAKPOINTS_ACTIVE);
            }
            return active == null ? false : active;
        } catch (final SCacheException e) {
            throw new SBonitaReadException("unable to read from cache", e);
        }
    }

    /**
     * @throws SCacheException
     * @throws SBonitaReadException
     */
    private synchronized void synchronizeBreakpoints() throws SCacheException, SBonitaReadException {
        if (!this.isBreakpointsSynched || this.cacheService.get(BREAKPOINTS, BREAKPOINTS_ACTIVE) == null) {// double synchronization check
            this.cacheService.clear(BREAKPOINTS);
            long nbOfBreakpoints = getNumberOfBreakpoints();
            int index = 0;
            final boolean active = nbOfBreakpoints > 0;
            while (nbOfBreakpoints > 0) {
                final List<SBreakpoint> breakpoints = getBreakpoints(index, index + BATCH_SIZE, this.defaultSortingField, this.defaulOrder);
                nbOfBreakpoints -= BATCH_SIZE;
                index += BATCH_SIZE;
                for (final SBreakpoint breakpoint : breakpoints) {
                    addBreakpointInCache(breakpoint);
                }
            }
            this.cacheService.store(BREAKPOINTS, BREAKPOINTS_ACTIVE, active);
            this.isBreakpointsSynched = true;
        }
    }

    @Override
    public long getNumberOfBreakpoints() throws SBonitaReadException {
        return this.persistenceRead.selectOne(SelectDescriptorBuilderExt.getNumberOfBreakpoints());
    }

    @Override
    public List<SBreakpoint> getBreakpoints(final int fromIndex, final int maxResults, final String sortingField, final OrderByType sortingOrder)
            throws SBonitaReadException {
        final QueryOptions queryOptions;
        if (sortingField == null || sortingOrder == null) {
            queryOptions = new QueryOptions(fromIndex, maxResults, SBreakpoint.class, this.defaultSortingField, this.defaulOrder);
        } else {
            queryOptions = new QueryOptions(fromIndex, maxResults, SBreakpoint.class, sortingField, sortingOrder);
        }
        final SelectListDescriptor<SBreakpoint> elements = SelectDescriptorBuilderExt.getElements(SBreakpoint.class, "Breakpoint", queryOptions);
        return this.persistenceRead.selectList(elements);
    }

    private void addBreakpointInCache(final SBreakpoint breakpoint) throws SCacheException {
        final String breakpointKey;
        if (breakpoint.isInstanceScope()) {
            breakpointKey = buildBreakpointKey(INSTANCE_KEY, breakpoint.getInstanceId(), breakpoint.getElementName(), breakpoint.getStateId());
        } else {
            breakpointKey = buildBreakpointKey(DEFINITION_KEY, breakpoint.getDefinitionId(), breakpoint.getElementName(), breakpoint.getStateId());
        }

        this.cacheService.store(BREAKPOINTS, breakpointKey, breakpoint);
    }

    private static String buildBreakpointKey(final String key, final long id, final String elementName, final int stateId) {
        return key + id + elementName + '%' + stateId;
    }

    @Override
    public SBreakpoint getBreakPointFor(final long definitionId, final long instanceId, final String elementName, final int stateId)
            throws SBonitaReadException {
        try {
            if (!this.isBreakpointsSynched) {
                synchronizeBreakpoints();
            }
            // FIXME do not use cache? breakpoints might go out of cache...
            SBreakpoint breakpoint = (SBreakpoint) this.cacheService.get(BREAKPOINTS, buildBreakpointKey(INSTANCE_KEY, instanceId, elementName, stateId));
            if (breakpoint == null) {
                breakpoint = (SBreakpoint) this.cacheService.get(BREAKPOINTS, buildBreakpointKey(DEFINITION_KEY, definitionId, elementName, stateId));
            }
            return breakpoint;
        } catch (final SCacheException e) {
            throw new SBonitaReadException("unable to read ", e);
        }
    }

    private void initiateLogBuilder(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String callerMethodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (this.queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            this.queriableLoggerService.log(this.getClass().getName(), callerMethodName, log);
        }
    }

}
