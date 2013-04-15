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

import javax.transaction.Synchronization;

import org.bonitasoft.engine.cache.CacheException;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilder;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
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
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;
import org.bonitasoft.engine.transaction.BusinessTransaction;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.transaction.TransactionState;

import com.bonitasoft.engine.core.process.instance.api.BreakpointService;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SBreakpointCreationException;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SBreakpointDeletionException;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SBreakpointNotFoundException;
import com.bonitasoft.engine.core.process.instance.model.breakpoint.SBreakpoint;
import com.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import com.bonitasoft.engine.core.process.instance.model.builder.SBreakpointLogBuilder;
import com.bonitasoft.engine.core.process.instance.recorder.SelectDescriptorBuilder;

/**
 * @author Baptiste Mesta
 */
public class BreakpointServiceImpl implements BreakpointService {

    /**
     * @author Baptiste Mesta
     */
    private final class ResetBreakpointFlag implements Synchronization {

        private final BreakpointServiceImpl breakpointServiceImpl;

        private final BusinessTransaction transaction;

        public ResetBreakpointFlag(final BreakpointServiceImpl breakpointServiceImpl, final BusinessTransaction transaction) {
            this.breakpointServiceImpl = breakpointServiceImpl;
            this.transaction = transaction;
        }

        @Override
        public void beforeCompletion() {
        }

        @Override
        public void afterCompletion(final int status) {
            if (TransactionState.COMMITTED.equals(transaction.getState())) {
                breakpointServiceImpl.isBreakpointsSynched = false;
            }
        }
    }

    private static final String INSTANCE_KEY = "INST_";

    private static final String DEFINITION_KEY = "DEF_";

    private static final int BATCH_SIZE = 100;

    private static final String BREAKPOINTS = "BREAKPOINTS_BY_INSTANCE";

    private static final Serializable BREAKPOINTS_ACTIVE = "BREAKPOINTS_ACTIVE";

    protected final BPMInstanceBuilders instanceBuilders;

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
            final BPMInstanceBuilders instanceBuilders, final CacheService cacheService, final ReadSessionAccessor sessionAccessor,
            final QueriableLoggerService queriableLoggerService, final TransactionService transactionService) {
        this.recorder = recorder;
        this.persistenceRead = persistenceRead;
        this.instanceBuilders = instanceBuilders;
        this.eventService = eventService;
        this.cacheService = cacheService;
        this.queriableLoggerService = queriableLoggerService;
        this.transactionService = transactionService;
        defaultSortingField = instanceBuilders.getSBreakpointBuilder().getDefinitionIdKey();
        defaulOrder = OrderByType.ASC;
    }

    @Override
    public SBreakpoint addBreakpoint(final SBreakpoint breakpoint) throws SBreakpointCreationException {
        final SBreakpointLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, NEW_BREAKPOINT_ADDED, breakpoint);
        final InsertRecord insertRecord = new InsertRecord(breakpoint);
        SInsertEvent insertEvent = null;
        if (eventService.hasHandlers(BREAKPOINT, EventActionType.CREATED)) {
            final SEventBuilder eventBuilder = eventService.getEventBuilder();
            insertEvent = (SInsertEvent) eventBuilder.createInsertEvent(BREAKPOINT).setObject(breakpoint).done();
        }
        try {
            recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(breakpoint.getId(), SQueriableLog.STATUS_OK, logBuilder, "addBreakpoint");
            final BusinessTransaction transaction = transactionService.getTransaction();
            transaction.registerSynchronization(new ResetBreakpointFlag(this, transaction));
            isBreakpointsSynched = false;// FIXME register that in a synchronization to refresh breakpoints after commit
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
        if (eventService.hasHandlers(BREAKPOINT, EventActionType.DELETED)) {
            deleteEvent = (SDeleteEvent) eventService.getEventBuilder().createDeleteEvent(BREAKPOINT).setObject(sBreakpoint).done();
        }
        final DeleteRecord deleteRecord = new DeleteRecord(sBreakpoint);
        final SBreakpointLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, REMOVING_BREAKPOINT, sBreakpoint);
        try {
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(id, SQueriableLog.STATUS_OK, logBuilder, "removeBreakpoint");
            isBreakpointsSynched = false;
        } catch (final SRecorderException e) {
            initiateLogBuilder(id, SQueriableLog.STATUS_FAIL, logBuilder, "removeBreakpoint");
            throw new SBreakpointDeletionException(e);
        }
    }

    @Override
    public SBreakpoint getBreakpoint(final long id) throws SBreakpointNotFoundException, SBonitaReadException {
        final SBreakpoint breakpoint = persistenceRead.selectById(SelectDescriptorBuilder.getElementById(SBreakpoint.class, "Breakpoint", id));
        if (breakpoint == null) {
            throw new SBreakpointNotFoundException(id);
        }
        return breakpoint;
    }

    protected <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.createNewInstance().actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    protected <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    protected SBreakpointLogBuilder getQueriableLog(final ActionType actionType, final String message, final SBreakpoint breakpoint) {
        final SBreakpointLogBuilder logBuilder = instanceBuilders.getSBreakpointLogBuilder();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        logBuilder.definitionId(breakpoint.getDefinitionId());
        return logBuilder;
    }

    @Override
    public boolean isBreakpointActive() throws SBonitaReadException {
        try {
            Boolean active = (Boolean) cacheService.get(BREAKPOINTS, BREAKPOINTS_ACTIVE);
            if (!isBreakpointsSynched || active == null) {// active might be out of cache
                synchronizeBreakpoints();
                active = (Boolean) cacheService.get(BREAKPOINTS, BREAKPOINTS_ACTIVE);
            }
            return active == null ? false : active;
        } catch (final TenantIdNotSetException e) {
            throw new SBonitaReadException("unable to get tenantId", e);
        } catch (final CacheException e) {
            throw new SBonitaReadException("unable to read from cache");
        }
    }

    /**
     * @throws CacheException
     * @throws TenantIdNotSetException
     * @throws SBonitaReadException
     */
    private synchronized void synchronizeBreakpoints() throws CacheException, TenantIdNotSetException, SBonitaReadException {
        if (!isBreakpointsSynched || cacheService.get(BREAKPOINTS, BREAKPOINTS_ACTIVE) == null) {// double synchronization check
            cacheService.clear(BREAKPOINTS);
            long nbOfBreakpoints = getNumberOfBreakpoints();
            int index = 0;
            final boolean active = nbOfBreakpoints > 0;
            while (nbOfBreakpoints > 0) {
                final List<SBreakpoint> breakpoints = getBreakpoints(index, index + BATCH_SIZE, defaultSortingField, defaulOrder);
                nbOfBreakpoints -= BATCH_SIZE;
                index += BATCH_SIZE;
                for (final SBreakpoint breakpoint : breakpoints) {
                    addBreakpointInCache(BREAKPOINTS, breakpoint);
                }
            }
            cacheService.store(BREAKPOINTS, BREAKPOINTS_ACTIVE, active);
            isBreakpointsSynched = true;
        }
    }

    @Override
    public long getNumberOfBreakpoints() throws SBonitaReadException {
        return persistenceRead.selectOne(SelectDescriptorBuilder.getNumberOfBreakpoints());
    }

    @Override
    public List<SBreakpoint> getBreakpoints(final int fromIndex, final int maxResults, final String sortingField, final OrderByType sortingOrder)
            throws SBonitaReadException {
        final QueryOptions queryOptions;
        if (sortingField == null || sortingOrder == null) {
            queryOptions = new QueryOptions(fromIndex, maxResults, SBreakpoint.class, defaultSortingField, defaulOrder);
        } else {
            queryOptions = new QueryOptions(fromIndex, maxResults, SBreakpoint.class, sortingField, sortingOrder);
        }
        final SelectListDescriptor<SBreakpoint> elements = SelectDescriptorBuilder.getElements(SBreakpoint.class, "Breakpoint", queryOptions);
        return persistenceRead.selectList(elements);
    }

    private void addBreakpointInCache(final String cacheName, final SBreakpoint breakpoint) throws CacheException {
        if (breakpoint.isInstanceScope()) {
            cacheService.store(cacheName, getBreakpointKey(INSTANCE_KEY, breakpoint.getInstanceId(), breakpoint.getElementName(), breakpoint.getStateId()),
                    breakpoint);
        } else {
            cacheService.store(cacheName, getBreakpointKey(DEFINITION_KEY, breakpoint.getDefinitionId(), breakpoint.getElementName(), breakpoint.getStateId()),
                    breakpoint);
        }
    }

    private String getBreakpointKey(final String key, final long id, final String elementName, final int stateId) {
        return key + id + elementName + '%' + stateId;
    }

    @Override
    public SBreakpoint getBreakPointFor(final long definitionId, final long instanceId, final String elementName, final int stateId)
            throws SBonitaReadException {
        try {
            if (!isBreakpointsSynched) {
                synchronizeBreakpoints();
            }
            // FIXME do not use cache? breakpoints might go out of cache...
            SBreakpoint breakpoint = (SBreakpoint) cacheService.get(BREAKPOINTS, getBreakpointKey(INSTANCE_KEY, instanceId, elementName, stateId));
            if (breakpoint == null) {
                breakpoint = (SBreakpoint) cacheService.get(BREAKPOINTS, getBreakpointKey(DEFINITION_KEY, definitionId, elementName, stateId));
            }
            return breakpoint;
        } catch (final TenantIdNotSetException e) {
            throw new SBonitaReadException("unable to get tenantId", e);
        } catch (final CacheException e) {
            throw new SBonitaReadException("unable to read ");
        }
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
