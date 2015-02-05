/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.migration.impl;

import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.core.migration.exceptions.SInvalidMigrationPlanException;
import org.bonitasoft.engine.core.migration.exceptions.SMigrationPlanNotFoundException;
import org.bonitasoft.engine.core.migration.exceptions.SPrepareForMigrationFailedException;
import org.bonitasoft.engine.core.migration.impl.MigrationPlanServiceImpl;
import org.bonitasoft.engine.core.migration.model.SMigrationMapping;
import org.bonitasoft.engine.core.migration.model.SMigrationPlan;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.xml.ParserFactory;

import com.bonitasoft.engine.core.process.instance.api.BreakpointService;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SBreakpointCreationException;
import com.bonitasoft.engine.core.process.instance.model.builder.SBreakpointBuilderFactory;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class MigrationPlanServiceExt extends MigrationPlanServiceImpl {

    private final BreakpointService breakpointService;

    private final int interruptingStateId;

    public MigrationPlanServiceExt(final Recorder recorder, final ReadPersistenceService persistenceService, final EventService eventService,
            final ParserFactory parserFactory, final CacheService cacheService, final ProcessInstanceService processInstanceService,
            final BreakpointService breakpointService, final int interruptingStateId, final QueriableLoggerService queriableLoggerService) {
        super(recorder, persistenceService, eventService, parserFactory, cacheService, processInstanceService, queriableLoggerService);
        this.breakpointService = breakpointService;
        this.interruptingStateId = interruptingStateId;
    }

    @Override
    public void prepareProcessesForMigration(final List<Long> processInstanceIds, final long migrationPlanId) throws SPrepareForMigrationFailedException {
        SMigrationPlan migrationPlan;
        try {
            migrationPlan = getMigrationPlan(migrationPlanId);
        } catch (final SInvalidMigrationPlanException e) {
            throw new SPrepareForMigrationFailedException(migrationPlanId, e);
        } catch (final SBonitaReadException e) {
            throw new SPrepareForMigrationFailedException(migrationPlanId, e);
        } catch (final SMigrationPlanNotFoundException e) {
            throw new SPrepareForMigrationFailedException(migrationPlanId, e);
        }
        for (final Long processInstanceId : processInstanceIds) {
            SProcessInstance processInstance;
            try {
                processInstance = processInstanceService.getProcessInstance(processInstanceId);
                processInstanceService.setMigrationPlanId(processInstance, migrationPlanId);
                for (final SMigrationMapping mapping : migrationPlan.getMappings()) {
                    breakpointService.addBreakpoint(BuilderFactory.get(SBreakpointBuilderFactory.class)
                            .createNewInstance(processInstance.getProcessDefinitionId(), processInstance.getId(), mapping.getSourceName(),
                                    mapping.getSourceState(), interruptingStateId).done());
                }
                // TODO check if the process is in ready for migration
            } catch (final SProcessInstanceNotFoundException e) {
                throw new SPrepareForMigrationFailedException(processInstanceId, migrationPlanId, e);
            } catch (final SProcessInstanceReadException e) {
                throw new SPrepareForMigrationFailedException(processInstanceId, migrationPlanId, e);
            } catch (final SProcessInstanceModificationException e) {
                throw new SPrepareForMigrationFailedException(processInstanceId, migrationPlanId, e);
            } catch (final SBreakpointCreationException e) {
                throw new SPrepareForMigrationFailedException(processInstanceId, migrationPlanId, e);
            }
        }
    }
}
