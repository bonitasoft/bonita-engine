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
package org.bonitasoft.engine.continuation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.work.SWorkRegisterException;
import org.bonitasoft.engine.work.WorkService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class WorkServiceTest extends CommonBPMServicesTest {

    @Before
    public void before() throws SBonitaException {
        if (getWorkService().isStopped()) {
            getWorkService().start();
        }
    }

    private WorkService getWorkService() {
        return getTenantAccessor().getWorkService();
    }

    @After
    public void after() throws SBonitaException {
        getWorkService().stop();
        getWorkService().start();
    }

    @Test(expected = SWorkRegisterException.class)
    public void testWorkOnNotActiveTransaction() throws Exception {
        final List<String> works = new ArrayList<String>();
        getWorkService().registerWork(new ListAdder(works, "1"));
    }

    @Test
    public void testWorkInMultipleTransactions() throws Exception {
        getTransactionService().begin();
        final List<String> works = new ArrayList<String>();
        final WorkService workService = getWorkService();
        workService.registerWork(new ListAdder(works, "1"));
        getTransactionService().complete();

        waitFor(1, works);
        assertThat(works).contains("1");

        getTransactionService().begin();
        workService.registerWork(new ListAdder(works, "2"));
        getTransactionService().complete();

        waitFor(2, works);
        assertThat(works).contains("2");
    }

    @Test
    public void testMultipleContinuation() throws Exception {
        getTransactionService().begin();
        final List<String> works = new ArrayList<String>();
        getWorkService().registerWork(new ListAdder(works, "1"));
        getWorkService().registerWork(new ListAdder(works, "2"));
        getWorkService().registerWork(new ListAdder(works, "3"));
        getWorkService().registerWork(new ListAdder(works, "4"));
        getWorkService().registerWork(new ListAdder(works, "5"));

        getTransactionService().complete();
        waitFor(5, works);
        assertThat(works).contains("1", "2", "3", "4", "5");
    }

    public void waitFor(final int number, final List<String> works) throws InterruptedException {
        final long timeout = System.currentTimeMillis() + 2000;
        boolean reached = false;
        do {
            if (works.size() == number) {
                reached = true;
            } else {
                Thread.sleep(50);
            }
        } while (!reached && timeout > System.currentTimeMillis());
        assertThat(reached).isTrue();
    }

}
