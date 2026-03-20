/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;

import javax.persistence.EntityManager;
import javax.transaction.Status;
import javax.transaction.TransactionManager;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.bonitasoft.engine.business.data.impl.JPABusinessDataRepositoryImpl;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.service.ServiceAccessorSingleton;
import org.bonitasoft.engine.transaction.JTATransactionServiceImpl;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.AopTestUtils;

/**
 * Integration tests for EntityManager lifecycle under various JTA transaction outcomes.
 * Uses the full Bonita engine with real Narayana JTA transactions to reproduce actual behavior,
 * particularly the transaction timeout scenario (BPA-321).
 */
@Slf4j
public class TransactionTimeoutEntityManagerIT extends CommonAPIIT {

    private JPABusinessDataRepositoryImpl bdmRepository;

    private TransactionService transactionService;

    @Before
    public void setUp() throws Exception {
        loginWithTechnicalUser();

        // Deploy a minimal BDM so the EntityManagerFactory is created
        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        final byte[] zip = converter.zip(buildMinimalBOM());
        getTenantAdministrationAPI().pause();
        getTenantAdministrationAPI().cleanAndUninstallBusinessDataModel();
        getTenantAdministrationAPI().updateBusinessDataModel(zip);
        getTenantAdministrationAPI().resume();

        // Get the real JPABusinessDataRepositoryImpl from the engine
        // Unwrap Spring AOP proxy (created by BusinessDataRepositoryEventAspect) to access internal fields via reflection
        ServiceAccessor serviceAccessor = ServiceAccessorSingleton.getInstance();
        bdmRepository = AopTestUtils.getTargetObject(serviceAccessor.getBusinessDataRepository());
        transactionService = serviceAccessor.getTransactionService();
    }

    @After
    public void tearDown() throws Exception {
        // Clean up ThreadLocal to avoid leaking to the next test
        clearManagersThreadLocal();

        if (!getTenantAdministrationAPI().isPaused()) {
            getTenantAdministrationAPI().pause();
            getTenantAdministrationAPI().cleanAndUninstallBusinessDataModel();
            getTenantAdministrationAPI().resume();
        }
        logout();
    }

    /**
     * Scenario (a): Nominal — transaction commits successfully.
     * The EntityManager is created, used, and properly cleaned up by afterCompletion(STATUS_COMMITTED).
     */
    @Test
    public void afterCompletion_should_cleanup_entityManager_on_successful_commit() throws Exception {
        //given
        transactionService.begin();

        //when — trigger getEntityManager() which registers the RemoveEntityManagerSynchronization
        bdmRepository.getEntityClassNames();
        EntityManager emDuringTx = getManagersThreadLocalValue();
        assertThat(emDuringTx).as("EM should exist during transaction").isNotNull();
        assertThat(emDuringTx.isOpen()).isTrue();

        transactionService.complete();

        //then — afterCompletion(STATUS_COMMITTED) should have closed and removed the EM
        EntityManager emAfterCommit = getManagersThreadLocalValue();
        assertThat(emAfterCommit).as("EM should be removed from ThreadLocal after commit").isNull();
    }

    /**
     * Scenario (b): Application rollback — transaction is rolled back explicitly.
     * The EntityManager should be cleaned up by afterCompletion(STATUS_ROLLEDBACK).
     */
    @Test
    public void afterCompletion_should_cleanup_entityManager_on_explicit_rollback() throws Exception {
        //given
        transactionService.begin();

        bdmRepository.getEntityClassNames();
        EntityManager emDuringTx = getManagersThreadLocalValue();
        assertThat(emDuringTx).as("EM should exist during transaction").isNotNull();

        //when
        rollbackTransaction();

        //then — afterCompletion(STATUS_ROLLEDBACK) should have closed and removed the EM
        EntityManager emAfterRollback = getManagersThreadLocalValue();
        assertThat(emAfterRollback).as("EM should be removed from ThreadLocal after rollback").isNull();
    }

    /**
     * Scenario (c): Transaction marked rollback-only (setRollbackOnly), then commit attempt.
     * commit() triggers a rollback. The EntityManager should still be cleaned up.
     */
    @Test
    public void afterCompletion_should_cleanup_entityManager_on_rollback_only_commit_attempt() throws Exception {
        //given
        transactionService.begin();

        bdmRepository.getEntityClassNames();
        EntityManager emDuringTx = getManagersThreadLocalValue();
        assertThat(emDuringTx).isNotNull();

        //when
        rollbackTransaction();

        //then — afterCompletion(STATUS_ROLLEDBACK) should have closed and removed the EM
        EntityManager emAfter = getManagersThreadLocalValue();
        assertThat(emAfter).as("EM should be removed from ThreadLocal after rollback-only commit").isNull();
    }

    /**
     * Scenario (d): Transaction timeout — Narayana's TransactionReaper aborts the transaction.
     * This is the BPA-321 scenario. The reaper calls afterCompletion on its own thread,
     * so ThreadLocal.get() returns null and the EM is NOT cleaned up.
     * <p>
     * After the timeout, the defensive getEntityManager() should detect the stale EM
     * (not joined to any transaction) and discard it, creating a fresh one for the next tx.
     */
    @Test
    public void getEntityManager_should_detect_stale_entityManager_after_transaction_timeout_and_create_fresh_one()
            throws Exception {
        //given — start a transaction with a 1-second timeout
        TransactionManager txManager = getTransactionManager();
        txManager.setTransactionTimeout(1);
        transactionService.begin();

        // Trigger EntityManager creation within the transaction
        bdmRepository.getEntityClassNames();
        EntityManager emDuringTx = getManagersThreadLocalValue();
        assertThat(emDuringTx).as("EM should exist during transaction").isNotNull();

        //when — wait for the timeout to fire
        log.info("Waiting for transaction timeout (1s)...");
        Thread.sleep(2000);

        // The transaction should have been aborted by the reaper
        int status = txManager.getStatus();
        log.info("Transaction status after timeout: {}", status);
        assertThat(status).as("Transaction should be rolled back or no longer active")
                .isIn(Status.STATUS_ROLLEDBACK, Status.STATUS_NO_TRANSACTION, Status.STATUS_MARKED_ROLLBACK);

        // The stale EM may still be in the ThreadLocal (the reaper thread couldn't clean it)
        EntityManager staleEM = getManagersThreadLocalValue();
        log.info("Stale EM still in ThreadLocal after timeout: {}", staleEM != null);

        // Clean up the timed-out transaction if needed
        if (txManager.getStatus() != Status.STATUS_NO_TRANSACTION) {
            try {
                rollbackTransaction();
            } catch (Exception e) {
                log.info("Rollback of timed-out tx: {}", e.getMessage());
            }
        }

        //then — start a new transaction and verify the defensive getEntityManager() works
        txManager.setTransactionTimeout(0); // restore default timeout
        transactionService.begin();

        // The defensive getEntityManager() should detect the stale EM and create a fresh one
        bdmRepository.getEntityClassNames();
        EntityManager freshEM = getManagersThreadLocalValue();
        assertThat(freshEM).as("A fresh EntityManager should be provided for the new transaction").isNotNull();
        assertThat(freshEM.isOpen()).isTrue();
        assertThat(freshEM.isJoinedToTransaction()).isTrue();

        // If the stale EM was still around, the fresh EM should be a different instance
        if (staleEM != null) {
            assertThat(freshEM).as("Fresh EM should be a different instance than the stale one")
                    .isNotSameAs(staleEM);
        }

        rollbackTransaction();
    }

    // --- Helpers ---

    private BusinessObjectModel buildMinimalBOM() {
        SimpleField nameField = new SimpleField();
        nameField.setName("name");
        nameField.setType(FieldType.STRING);

        BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("com.company.model.pojo.SimpleEntity");
        bo.addField(nameField);

        BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(bo);
        return bom;
    }

    @SuppressWarnings("unchecked")
    private ThreadLocal<EntityManager> getManagersThreadLocal() {
        try {
            Field managersField = JPABusinessDataRepositoryImpl.class.getDeclaredField("managers");
            managersField.setAccessible(true);
            return (ThreadLocal<EntityManager>) managersField.get(bdmRepository);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access managers ThreadLocal", e);
        }
    }

    private EntityManager getManagersThreadLocalValue() {
        return getManagersThreadLocal().get();
    }

    private void clearManagersThreadLocal() {
        getManagersThreadLocal().remove();
    }

    private TransactionManager getTransactionManager() {
        try {
            Field txMgrField = JTATransactionServiceImpl.class.getDeclaredField("txManager");
            txMgrField.setAccessible(true);
            return (TransactionManager) txMgrField.get(transactionService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access txManager field", e);
        }
    }

    private void rollbackTransaction() throws STransactionException {
        transactionService.setRollbackOnly();
        transactionService.complete(); // will trigger rollback since it's marked as rollback-only
    }
}
