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
 **/
package org.bonitasoft.engine.transaction;


public abstract class TransactionResourceTest {

    protected abstract TransactionService getTxService();

    // private List<SimpleTransactionResource> enlistResources(final BusinessTransaction transaction, final SimpleTransactionResource... resources)
    // throws STransactionResourceException {
    // final List<SimpleTransactionResource> result = new ArrayList<SimpleTransactionResource>();
    // for (final SimpleTransactionResource resource : resources) {
    // result.add(resource);
    // transaction.enlistTechnicalTransaction(resource);
    // }
    // return result;
    // }

    // @Test
    // public void testSimpleResourcesCommit() throws Exception {
    // final TransactionService txService = getTxService();
    //
    // txService.begin();
    //
    // final List<SimpleTransactionResource> resources = enlistResources(tx, new SimpleTransactionResource(false, false, false),
    // new SimpleTransactionResource(false, false, false));
    //
    // txService.complete();
    //
    // // check everything has been executed
    // final SimpleTransactionResource resource1 = resources.get(0);
    // assertTrue(resource1.isPrepare());
    // assertTrue(resource1.isCommit());
    // assertFalse(resource1.isRollback());
    //
    // final SimpleTransactionResource resource2 = resources.get(0);
    // assertTrue(resource2.isPrepare());
    // assertTrue(resource2.isCommit());
    // assertFalse(resource2.isRollback());
    // }
    //
    // @Test
    // public void testEnlistResourceOnCommittedTransaction() throws Exception {
    // final TransactionService txService = getTxService();
    //
    // txService.begin();
    // txService.complete();
    //
    // try {
    // txService.enlistTechnicalTransaction(new SimpleTransactionResource(false, false, false));
    // fail("this should be impossible to enlist a resource on a committed transaction");
    // } catch (final STransactionResourceException e) {
    // // OK
    // }
    // }
    //
    // @Test
    // public void testEnlistResourceOnRolledbackTransaction() throws Exception {
    // final TransactionService txService = getTxService();
    //
    // txService.begin();
    // txService.setRollbackOnly();
    // txService.complete();
    //
    // try {
    // txService.enlistTechnicalTransaction(new SimpleTransactionResource(false, false, false));
    // fail("this should be impossible to enlist a resource on a rolledback transaction");
    // } catch (final STransactionResourceException e) {
    // // OK
    // }
    // }
    //
    // @Test
    // public void testEnlistResourceOnCreatedTransaction() throws Exception {
    // final TransactionService txService = getTxService();
    //
    // try {
    // txService.enlistTechnicalTransaction(new SimpleTransactionResource(false, false, false));
    // fail("this should be impossible to enlist a resource on a created transaction");
    // } catch (final STransactionResourceException e) {
    // // OK
    // }
    // }
    //
    // @Test
    // public void testPrepareExceptionOnFirstResource() throws Exception {
    // final TransactionService txService = getTxService();
    //
    // txService.begin();
    // final List<SimpleTransactionResource> resources = enlistResources(tx, new SimpleTransactionResource(true, false, false), new SimpleTransactionResource(
    // false, false, false));
    //
    // try {
    // txService.complete();
    // fail("A prepare has failed. We cannot complete.");
    // } catch (final STransactionPrepareException e) {
    // assertEquals(TransactionState.ROLLEDBACK, txService.getState());
    // }
    //
    // // check everything has been executed
    // final SimpleTransactionResource resource1 = resources.get(0);
    // assertTrue(resource1.isPrepare());
    // assertFalse(resource1.isCommit());
    // assertTrue(resource1.isRollback());
    //
    // final SimpleTransactionResource resource2 = resources.get(1);
    // assertFalse(resource2.isPrepare());
    // assertFalse(resource2.isCommit());
    // assertTrue(resource2.isRollback());
    // }
    //
    // @Test
    // public void testPrepareExceptionOnSecondResource() throws Exception {
    // final TransactionService txService = getTxService();
    //
    // txService.begin();
    //
    // final List<SimpleTransactionResource> resources = enlistResources(tx, new SimpleTransactionResource(false, false, false),
    // new SimpleTransactionResource(true, false, false));
    //
    // try {
    // txService.complete();
    // fail("A prepare has failed. We cannot complete.");
    // } catch (final STransactionPrepareException e) {
    // assertEquals(TransactionState.ROLLEDBACK, txService.getState());
    // }
    //
    // // check everything has been executed
    //
    // final SimpleTransactionResource resource1 = resources.get(0);
    // assertTrue(resource1.isPrepare());
    // assertFalse(resource1.isCommit());
    // assertTrue(resource1.isRollback());
    //
    // final SimpleTransactionResource resource2 = resources.get(1);
    // assertTrue(resource2.isPrepare());
    // assertFalse(resource2.isCommit());
    // assertTrue(resource2.isRollback());
    // }
    //
    // @Test
    // public void testCommitExceptionOnFirstResource() throws Exception {
    // final TransactionService txService = getTxService();
    //
    // txService.begin();
    //
    // final List<SimpleTransactionResource> resources = enlistResources(tx, new SimpleTransactionResource(false, true, false), new SimpleTransactionResource(
    // false, false, false));
    //
    // try {
    // txService.complete();
    // fail("A commit has failed. We cannot complete.");
    // } catch (final STransactionCommitException e) {
    // assertEquals(TransactionState.COMMITTED, txService.getState());
    // }
    //
    // // check everything has been executed
    //
    // final SimpleTransactionResource resource1 = resources.get(0);
    // assertTrue(resource1.isPrepare());
    // assertTrue(resource1.isCommit());
    // assertFalse(resource1.isRollback());
    //
    // final SimpleTransactionResource resource2 = resources.get(1);
    // assertTrue(resource2.isPrepare());
    // assertTrue(resource2.isCommit());
    // assertFalse(resource2.isRollback());
    // }
    //
    // @Test
    // public void testCommitExceptionOnSecondResource() throws Exception {
    // final TransactionService txService = getTxService();
    //
    // txService.begin();
    //
    // final List<SimpleTransactionResource> resources = enlistResources(tx, new SimpleTransactionResource(false, false, false),
    // new SimpleTransactionResource(false, true, false));
    //
    // try {
    // txService.complete();
    // fail("A commit has failed. We cannot complete.");
    // } catch (final STransactionCommitException e) {
    // assertEquals(TransactionState.COMMITTED, txService.getState());
    // }
    //
    // // check everything has been executed
    //
    // final SimpleTransactionResource resource1 = resources.get(0);
    // assertTrue(resource1.isPrepare());
    // assertTrue(resource1.isCommit());
    // assertFalse(resource1.isRollback());
    //
    // final SimpleTransactionResource resource2 = resources.get(1);
    // assertTrue(resource2.isPrepare());
    // assertTrue(resource2.isCommit());
    // assertFalse(resource2.isRollback());
    // }
    //
    // @Test
    // public void testRollbackExceptionOnFirstResource() throws Exception {
    // final TransactionService txService = getTxService();
    //
    // txService.begin();
    //
    // final List<SimpleTransactionResource> resources = enlistResources(tx, new SimpleTransactionResource(true, false, true), new SimpleTransactionResource(
    // false, false, false));
    //
    // try {
    // txService.complete();
    // fail("A prepare has failed. We cannot complete.");
    // } catch (final STransactionPrepareException e) {
    // assertEquals(TransactionState.ROLLEDBACK, txService.getState());
    // }
    //
    // // check everything has been executed
    //
    // final SimpleTransactionResource resource1 = resources.get(0);
    // assertTrue(resource1.isPrepare());
    // assertFalse(resource1.isCommit());
    // assertTrue(resource1.isRollback());
    //
    // final SimpleTransactionResource resource2 = resources.get(1);
    // assertFalse(resource2.isPrepare());
    // assertFalse(resource2.isCommit());
    // assertTrue(resource2.isRollback());
    // }
    //
    // @Test
    // public void testRollbackExceptionOnSecondResource() throws Exception {
    // final TransactionService txService = getTxService();
    //
    // txService.begin();
    //
    // final List<SimpleTransactionResource> resources = enlistResources(tx, new SimpleTransactionResource(true, false, false), new SimpleTransactionResource(
    // false, false, true));
    //
    // try {
    // txService.complete();
    // fail("A prepare has failed. We cannot complete.");
    // } catch (final STransactionPrepareException e) {
    // assertEquals(TransactionState.ROLLEDBACK, txService.getState());
    // }
    //
    // // check everything has been executed
    //
    // final SimpleTransactionResource resource1 = resources.get(0);
    // assertTrue(resource1.isPrepare());
    // assertFalse(resource1.isCommit());
    // assertTrue(resource1.isRollback());
    //
    // final SimpleTransactionResource resource2 = resources.get(1);
    // assertFalse(resource2.isPrepare());
    // assertFalse(resource2.isCommit());
    // assertTrue(resource2.isRollback());
    // }
    //
    // @Test
    // public void testSynchronizationExecOnResourcePrepareException() throws Exception {
    // final TransactionService txService = getTxService();
    //
    // txService.begin();
    //
    // txService.enlistTechnicalTransaction(new SimpleTransactionResource(true, false, false));
    // txService.enlistTechnicalTransaction(new SimpleTransactionResource(false, false, false));
    //
    // final SimpleSynchronization synch = new SimpleSynchronization();
    // txService.registerSynchronization(synch);
    //
    // try {
    // txService.complete();
    // fail("A prepare has failed. We cannot complete.");
    // } catch (final STransactionPrepareException e) {
    // assertEquals(TransactionState.ROLLEDBACK, txService.getState());
    // }
    //
    // // check everything has been executed
    // assertTrue(synch.isBeforeCompletion());
    // assertTrue(synch.isAfterCompletion());
    // }
    //
    // @Test
    // public void testSynchronizationExecOnResourceCommitException() throws Exception {
    // final TransactionService txService = getTxService();
    //
    // txService.begin();
    //
    // txService.enlistTechnicalTransaction(new SimpleTransactionResource(false, true, false));
    // txService.enlistTechnicalTransaction(new SimpleTransactionResource(false, true, false));
    //
    // final SimpleSynchronization synch = new SimpleSynchronization();
    // txService.registerSynchronization(synch);
    //
    // try {
    // txService.complete();
    // fail("A commit has failed. We cannot complete.");
    // } catch (final STransactionCommitException e) {
    // assertEquals(TransactionState.COMMITTED, txService.getState());
    // }
    //
    // // check everything has been executed
    // assertTrue(synch.isBeforeCompletion());
    // assertTrue(synch.isAfterCompletion());
    // }
    //
    // @Test(expected = SBeforeCompletionException.class)
    // public void testSynchronizationFailsOnBefore() throws Exception {
    // final TransactionService txService = getTxService();
    //
    // txService.begin();
    //
    // txService.enlistTechnicalTransaction(new SimpleTransactionResource(false, false, false));
    // txService.enlistTechnicalTransaction(new SimpleTransactionResource(false, false, false));
    //
    // final SimpleSynchronization synch = new SimpleSynchronization(true, false);
    // txService.registerSynchronization(synch);
    // try {
    // txService.complete();
    // } finally {
    // // check everything has been executed
    // assertFalse(synch.isBeforeCompletion());
    // assertEquals(TransactionState.ACTIVE, txService.getState());
    // assertFalse(synch.isAfterCompletion());
    // }
    // }
    //
    // @Test(expected = SAfterCompletionException.class)
    // public void testSynchronizationFailsOnAfter() throws Exception {
    // final TransactionService txService = getTxService();
    //
    // txService.begin();
    //
    // txService.enlistTechnicalTransaction(new SimpleTransactionResource(false, false, false));
    // txService.enlistTechnicalTransaction(new SimpleTransactionResource(false, false, false));
    //
    // final SimpleSynchronization synch = new SimpleSynchronization(false, true);
    // txService.registerSynchronization(synch);
    // try {
    // txService.complete();
    // } finally {
    // // check everything has been executed
    // assertTrue(synch.isBeforeCompletion());
    // assertEquals(TransactionState.COMMITTED, txService.getState());
    // assertFalse(synch.isAfterCompletion());
    // }
    // }
    //
    // @Test(expected = SBeforeCompletionException.class)
    // public void testSynchronizationFailsOnBeforeAndAfter() throws Exception {
    // final TransactionService txService = getTxService();
    //
    // txService.begin();
    //
    // txService.enlistTechnicalTransaction(new SimpleTransactionResource(false, false, false));
    // txService.enlistTechnicalTransaction(new SimpleTransactionResource(false, false, false));
    //
    // final SimpleSynchronization synch = new SimpleSynchronization(true, true);
    // txService.registerSynchronization(synch);
    // try {
    // txService.complete();
    // } finally {
    // // check everything has been executed
    // assertFalse(synch.isBeforeCompletion());
    // assertEquals(TransactionState.ACTIVE, txService.getState());
    // assertFalse(synch.isAfterCompletion());
    // }
    // }
    //
    // @Test(expected = SBeforeCompletionException.class)
    // public void testMultipleSynchronizationFails() throws Exception {
    // final TransactionService txService = getTxService();
    //
    // txService.begin();
    //
    // txService.enlistTechnicalTransaction(new SimpleTransactionResource(false, false, false));
    // txService.enlistTechnicalTransaction(new SimpleTransactionResource(false, false, false));
    //
    // final SimpleSynchronization synch1 = new SimpleSynchronization(false, true);
    // final SimpleSynchronization synch2 = new SimpleSynchronization(true, false);
    // final SimpleSynchronization synch3 = new SimpleSynchronization(true, true);
    // final SimpleSynchronization synch4 = new SimpleSynchronization(false, false);
    // txService.registerSynchronization(synch1);
    // txService.registerSynchronization(synch2);
    // txService.registerSynchronization(synch3);
    // txService.registerSynchronization(synch4);
    // try {
    // txService.complete();
    // } finally {
    // assertEquals(TransactionState.ACTIVE, txService.getState());
    //
    // // check everything has been executed
    // assertTrue(synch1.isBeforeCompletion());
    // assertFalse(synch1.isAfterCompletion());
    //
    // assertFalse(synch2.isBeforeCompletion());
    // assertFalse(synch2.isAfterCompletion());
    //
    // assertFalse(synch3.isBeforeCompletion());
    // assertFalse(synch3.isAfterCompletion());
    //
    // assertTrue(synch4.isBeforeCompletion());
    // assertFalse(synch4.isAfterCompletion());
    // }
    // }

}
