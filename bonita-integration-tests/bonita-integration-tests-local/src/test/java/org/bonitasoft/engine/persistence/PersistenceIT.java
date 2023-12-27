/**
 * Copyright (C) 2016 Bonitasoft S.A.
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
package org.bonitasoft.engine.persistence;

import static org.junit.Assert.fail;

import org.assertj.core.api.Assertions;
import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.bonitasoft.engine.transaction.STransactionCreationException;
import org.bonitasoft.engine.transaction.STransactionRollbackException;
import org.junit.Before;
import org.junit.Test;

public class PersistenceIT extends CommonBPMServicesTest {

    private ReadPersistenceService persistenceService;
    private Recorder recorder;

    @Before
    public void before() {
        persistenceService = getServiceAccessor().getReadPersistenceService();
        recorder = getServiceAccessor().getRecorder();
    }

    @Test
    public void testIfOneFailAllFail() throws Exception {
        Long numberOfUsersBefore = getNumberOfUsers();
        getTransactionService().begin();

        final SUser user = new SUser();
        user.setUserName("SUserImpl1FN");
        user.setPassword("SUserImpl1LN");
        recorder.recordInsert(new InsertRecord(user), "USER");
        try {
            persistenceService.selectOne(new SelectOneDescriptor<SUser>("wrong query", null, SUser.class));
            fail("Exception expected");
        } catch (final Exception e) {
            getTransactionService().setRollbackOnly();
        } finally {
            getTransactionService().complete();
        }

        Assertions.assertThat(getNumberOfUsers()).isEqualTo(numberOfUsersBefore);
    }

    private Long getNumberOfUsers() throws STransactionCreationException, SBonitaReadException,
            STransactionCommitException, STransactionRollbackException {
        getTransactionService().begin();
        final Long nbOfSUserImpl = persistenceService
                .selectOne(new SelectOneDescriptor<>("getNumberOfSUser", null, SUser.class, Long.class));
        getTransactionService().complete();
        return nbOfSUserImpl;
    }

}
