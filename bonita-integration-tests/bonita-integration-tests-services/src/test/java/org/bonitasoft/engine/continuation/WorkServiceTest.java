package org.bonitasoft.engine.continuation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    @Test
    public void testContinuation() throws Exception {
        final Logger logger = Logger.getLogger(this.getClass().getName());
        logger.setLevel(Level.FINEST);
        logger.addHandler(new ConsoleHandler());
        getTransactionService().begin();
        final List<String> works = new ArrayList<String>();
        getWorkService().registerWork(new ListAdder(works, "1"));
        getTransactionService().complete();
        Thread.sleep(1000);
        assertEquals(1, works.size());
        assertTrue(works.contains("1"));
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
        Thread.sleep(100);
        assertEquals(1, works.size());
        assertTrue(works.contains("1"));

        getTransactionService().begin();
        workService.registerWork(new ListAdder(works, "2"));
        getTransactionService().complete();
        Thread.sleep(100);
        assertEquals(2, works.size());
        assertTrue(works.contains("2"));
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
        getWorkService().registerWork(new ListAdder(works, "6"));

        getTransactionService().complete();
        Thread.sleep(100);
        assertEquals(6, works.size());
        assertTrue(works.contains("1"));
        assertTrue(works.contains("2"));
        assertTrue(works.contains("3"));
        assertTrue(works.contains("4"));
        assertTrue(works.contains("5"));
        assertTrue(works.contains("6"));
    }

}
