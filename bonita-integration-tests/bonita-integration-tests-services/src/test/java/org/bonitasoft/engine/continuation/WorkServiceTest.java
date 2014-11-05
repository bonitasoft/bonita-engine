package org.bonitasoft.engine.continuation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.work.SWorkRegisterException;
import org.bonitasoft.engine.work.WorkService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WorkServiceTest extends CommonServiceTest {

    @Before
    public void before() throws SBonitaException {
        getWorkService().start();
    }

    @After
    public void after() throws SBonitaException {
        getWorkService().stop();
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
        } while (!reached || timeout < System.currentTimeMillis());
        assertThat(reached).isTrue();
    }

}
