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
        getWorkService().registerWork(new ListAdder(works, "1", 0));
        getTransactionService().complete();
        Thread.sleep(1000);
        assertEquals(1, works.size());
        assertTrue(works.contains("1"));
    }

    @Test(expected = SWorkRegisterException.class)
    public void testWorkOnNotActiveTransaction() throws Exception {
        final List<String> works = new ArrayList<String>();
        getWorkService().registerWork(new ListAdder(works, "1", 0));
    }

    @Test
    public void testWorkInMultipleTransactions() throws Exception {
        getTransactionService().begin();
        final List<String> works = new ArrayList<String>();
        final WorkService workService = getWorkService();
        workService.registerWork(new ListAdder(works, "1", 0));
        getTransactionService().complete();
        Thread.sleep(100);
        assertEquals(1, works.size());
        assertTrue(works.contains("1"));

        getTransactionService().begin();
        workService.registerWork(new ListAdder(works, "2", 0));
        getTransactionService().complete();
        Thread.sleep(100);
        assertEquals(2, works.size());
        assertTrue(works.contains("2"));
    }

    @Test
    public void testMultipleContinuation() throws Exception {
        getTransactionService().begin();
        final List<String> works = new ArrayList<String>();
        getWorkService().registerWork(new ListAdder(works, "1", 0));
        getWorkService().registerWork(new ListAdder(works, "2", 0));
        getWorkService().registerWork(new ListAdder(works, "3", 0));
        getWorkService().registerWork(new ListAdder(works, "4", 0));
        getWorkService().registerWork(new ListAdder(works, "5", 0));
        getWorkService().registerWork(new ListAdder(works, "6", 0));

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

    @Test
    public void testContinuationWithDelay() throws Exception {
        getTransactionService().begin();

        final List<String> works = new ArrayList<String>();
        getWorkService().registerWork(new ListAdder(works, "1", 500));

        getTransactionService().complete();
        Thread.sleep(250);
        assertEquals(0, works.size());
        Thread.sleep(500);
        assertEquals(1, works.size());
        assertTrue(works.contains("1"));
    }

    @Test
    public void testMultipleContinuationWithDelay() throws Exception {
        getTransactionService().begin();

        final List<String> works = new ArrayList<String>();
        getWorkService().registerWork(new ListAdder(works, "1", 150));
        getWorkService().registerWork(new ListAdder(works, "2", 150));

        getTransactionService().complete();
        Thread.sleep(100);
        assertEquals(0, works.size());
        Thread.sleep(250);
        assertEquals(2, works.size());
        assertTrue(works.contains("1"));
        assertTrue(works.contains("2"));
    }

}
