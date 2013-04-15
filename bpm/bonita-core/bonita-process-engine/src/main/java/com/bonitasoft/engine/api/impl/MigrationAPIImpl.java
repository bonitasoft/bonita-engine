/*******************************************************************************
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.util.List;

import org.bonitasoft.engine.api.impl.PageIndexCheckingUtil;
import org.bonitasoft.engine.api.impl.transaction.DeleteMigrationPlan;
import org.bonitasoft.engine.api.impl.transaction.GetMigrationPlan;
import org.bonitasoft.engine.api.impl.transaction.GetMigrationPlanDescriptor;
import org.bonitasoft.engine.api.impl.transaction.GetMigrationPlanDescriptors;
import org.bonitasoft.engine.api.impl.transaction.GetNumberOfMigrationPlan;
import org.bonitasoft.engine.api.impl.transaction.ImportMigrationPlan;
import org.bonitasoft.engine.api.impl.transaction.PrepareProcessForMigration;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.migration.MigrationPlanService;
import org.bonitasoft.engine.core.migration.exceptions.SInvalidMigrationPlanException;
import org.bonitasoft.engine.core.migration.exceptions.SMigrationPlanCreationException;
import org.bonitasoft.engine.core.migration.exceptions.SMigrationPlanDeletionException;
import org.bonitasoft.engine.core.migration.exceptions.SMigrationPlanNotFoundException;
import org.bonitasoft.engine.core.migration.model.SMigrationPlanDescriptor;
import org.bonitasoft.engine.exception.BonitaReadException;
import org.bonitasoft.engine.exception.InvalidMigrationPlanException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.MigrationPlanCreationException;
import org.bonitasoft.engine.exception.MigrationPlanDeletionException;
import org.bonitasoft.engine.exception.MigrationPlanNotFoundException;
import org.bonitasoft.engine.exception.PageOutOfRangeException;
import org.bonitasoft.engine.exception.PrepareForMigrationFailedException;
import org.bonitasoft.engine.migration.MigrationPlan;
import org.bonitasoft.engine.migration.MigrationPlanCriterion;
import org.bonitasoft.engine.migration.MigrationPlanDescriptor;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.api.MigrationAPI;

/**
 * @author Baptiste Mesta
 */
public class MigrationAPIImpl implements MigrationAPI {

    private static TenantServiceAccessor getTenantAccessor() throws InvalidSessionException {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new InvalidSessionException(e);
        }
    }

    @Override
    public MigrationPlanDescriptor importMigrationPlan(final byte[] content) throws InvalidSessionException, InvalidMigrationPlanException,
            MigrationPlanCreationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final MigrationPlanService migrationPlanService = tenantAccessor.getMigrationPlanService();

        final ImportMigrationPlan importMigrationPlan = new ImportMigrationPlan(migrationPlanService, content);
        try {
            transactionExecutor.execute(importMigrationPlan);
            final SMigrationPlanDescriptor result = importMigrationPlan.getResult();
            return ModelConvertor.toMigrationPlanDescriptor(result);
        } catch (final SInvalidMigrationPlanException e) {
            throw new InvalidMigrationPlanException(e.getCause());
        } catch (final SMigrationPlanCreationException e) {
            throw new MigrationPlanCreationException(e.getCause());
        } catch (final SBonitaException e) {
            throw new MigrationPlanCreationException(e);
        }
    }

    @Override
    public byte[] exportMigrationPlan(final long id) throws MigrationPlanNotFoundException, InvalidSessionException, BonitaReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final MigrationPlanService migrationPlanService = tenantAccessor.getMigrationPlanService();
        final GetMigrationPlanDescriptor getMigrationPlan = new GetMigrationPlanDescriptor(migrationPlanService, id);
        try {
            transactionExecutor.execute(getMigrationPlan);
        } catch (final SMigrationPlanNotFoundException e) {
            throw new MigrationPlanNotFoundException(id, e);
        } catch (final SBonitaException e) {
            throw new BonitaReadException("unable to read migration plan with id = " + id, e);
        }
        return getMigrationPlan.getResult().getMigrationPlanContent();
    }

    @Override
    public List<MigrationPlanDescriptor> getMigrationPlanDescriptors(final int pageIndex, final int numberPerPage, final MigrationPlanCriterion pagingCriterion)
            throws InvalidSessionException, BonitaReadException, PageOutOfRangeException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final MigrationPlanService migrationPlanService = tenantAccessor.getMigrationPlanService();
        final long totalNumber = getNumberOfMigrationPlan(transactionExecutor, migrationPlanService);
        PageIndexCheckingUtil.checkIfPageIsOutOfRange(totalNumber, pageIndex, numberPerPage);
        final GetMigrationPlanDescriptors getMigrationPlan = new GetMigrationPlanDescriptors(migrationPlanService, pageIndex, numberPerPage, pagingCriterion);
        try {
            transactionExecutor.execute(getMigrationPlan);
        } catch (final SBonitaException e) {
            throw new BonitaReadException("unable to read migration plans", e);
        }
        return ModelConvertor.toMigrationPlanDescriptors(getMigrationPlan.getResult());
    }

    @Override
    public MigrationPlan getMigrationPlan(final long id) throws InvalidSessionException, BonitaReadException, MigrationPlanNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final MigrationPlanService migrationPlanService = tenantAccessor.getMigrationPlanService();
        final GetMigrationPlan getMigrationPlan = new GetMigrationPlan(migrationPlanService, id);
        try {
            transactionExecutor.execute(getMigrationPlan);
        } catch (final SMigrationPlanNotFoundException e) {
            throw new MigrationPlanNotFoundException(id, e);
        } catch (final SBonitaException e) {
            throw new BonitaReadException("unable to read migration plan with id = " + id, e);
        }
        return ModelConvertor.toMigrationPlan(getMigrationPlan.getResult());
    }

    @Override
    public void deleteMigrationPlan(final long id) throws InvalidSessionException, MigrationPlanNotFoundException, MigrationPlanDeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final MigrationPlanService migrationPlanService = tenantAccessor.getMigrationPlanService();
        final DeleteMigrationPlan deleteMigrationPlan = new DeleteMigrationPlan(migrationPlanService, id);
        try {
            transactionExecutor.execute(deleteMigrationPlan);
        } catch (final SMigrationPlanNotFoundException e) {
            throw new MigrationPlanNotFoundException(id, e);
        } catch (final SMigrationPlanDeletionException e) {
            throw new MigrationPlanDeletionException(id, e);
        } catch (final SBonitaException e) {
            throw new MigrationPlanDeletionException(id, e);
        }
    }

    @Override
    public long getNumberOfMigrationPlan() throws InvalidSessionException, BonitaReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final MigrationPlanService migrationPlanService = tenantAccessor.getMigrationPlanService();
        return getNumberOfMigrationPlan(transactionExecutor, migrationPlanService);
    }

    private long getNumberOfMigrationPlan(final TransactionExecutor transactionExecutor, final MigrationPlanService migrationPlanService)
            throws BonitaReadException {
        final GetNumberOfMigrationPlan getNumberOfMigrationPlan = new GetNumberOfMigrationPlan(migrationPlanService);
        try {
            transactionExecutor.execute(getNumberOfMigrationPlan);
        } catch (final SBonitaException e) {
            throw new BonitaReadException("unable to get the number of migration plan", e);
        }
        return getNumberOfMigrationPlan.getResult();
    }

    @Override
    public void prepareProcessesForMigration(final List<Long> processInstanceIds, final long migrationPlanId) throws InvalidSessionException,
            PrepareForMigrationFailedException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final MigrationPlanService migrationPlanService = tenantAccessor.getMigrationPlanService();

        final PrepareProcessForMigration transactionContent = new PrepareProcessForMigration(migrationPlanService, processInstanceIds, migrationPlanId);
        try {
            transactionExecutor.execute(transactionContent);
        } catch (final SBonitaException e) {
            throw new PrepareForMigrationFailedException(e);
        }
    }

}
