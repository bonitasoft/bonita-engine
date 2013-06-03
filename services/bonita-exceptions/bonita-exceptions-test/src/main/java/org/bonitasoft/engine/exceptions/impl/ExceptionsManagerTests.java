package org.bonitasoft.engine.exceptions.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exceptions.ExceptionsManager;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public abstract class ExceptionsManagerTests {

    public abstract ExceptionsManager getExceptionManager();

    @Test(expected = IllegalArgumentException.class)
    public void testGetWithNullException() {
        getExceptionManager().getPossibleCauses((SBonitaException) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWithNullParameters() {
        getExceptionManager().getPossibleCauses(MyTestException1.class.getName(), (Object[]) null);
    }

    @Test
    public void testGetUnknownExceptionId() {
        final List<String> causes = getExceptionManager().getPossibleCauses("plop");
        assertEquals(0, causes.size());
    }

    @Test
    public void testGetCausesFromExceptionId() {
        final List<String> causes = getExceptionManager().getPossibleCauses(MyTestException1.class.getName());
        assertEquals(5, causes.size());
        assertEquals("First Exception", causes.get(0));
        assertEquals("My ? Exception with ? parameters", causes.get(1));
        assertEquals("a ? lot ? of ? ? ? ?", causes.get(2));
        assertEquals("Plop plop", causes.get(3));
        assertEquals("kikoo", causes.get(4));
    }

    @Test
    public void testGetCausesFromExceptionIdAndParameters() {
        final List<String> causes = getExceptionManager().getPossibleCauses(MyTestException1.class.getName(),
                new Object[] { "great", 2, "plop", "kikoo", "lol" });
        assertEquals(5, causes.size());
        assertEquals("First Exception", causes.get(0));
        assertEquals("My great Exception with 2 parameters", causes.get(1));
        assertEquals("a 2 lot kikoo of plop great {5} lol", causes.get(2));
        assertEquals("Plop plop", causes.get(3));
        assertEquals("kikoo", causes.get(4));
    }

    @Test
    public void testGetCausesFromBonitaException() {
        final SBonitaException bonitaException = new MyTestException1(new Object[] { "great", 2, "plop", "kikoo", "lol" });
        final List<String> causes = getExceptionManager().getPossibleCauses(bonitaException);
        assertEquals(5, causes.size());
        assertEquals("First Exception", causes.get(0));
        assertEquals("My great Exception with 2 parameters", causes.get(1));
        assertEquals("a 2 lot kikoo of plop great {5} lol", causes.get(2));
        assertEquals("Plop plop", causes.get(3));
        assertEquals("kikoo", causes.get(4));
    }

    @Test
    public void testGetCausesFromExceptionIdAndEmptyParameters() {
        final List<String> causes = getExceptionManager().getPossibleCauses(MyTestException1.class.getName(), new Object[] {});
        assertEquals(5, causes.size());
        assertEquals("First Exception", causes.get(0));
        assertEquals("My {0} Exception with {1} parameters", causes.get(1));
        assertEquals("a {1} lot {3} of {2} {0} {5} {4}", causes.get(2));
        assertEquals("Plop plop", causes.get(3));
        assertEquals("kikoo", causes.get(4));
    }

    @Test
    public void testGetCausesFrom2Exceptions() {
        List<String> causes = getExceptionManager().getPossibleCauses(new MyTestException1());
        assertEquals(5, causes.size());
        causes = getExceptionManager().getPossibleCauses(new MyTestException2());
        assertEquals(2, causes.size());
        assertEquals("To Bonita or not to Bonita, that is the question", causes.get(0));
        assertEquals("My 6@|_|54 \\/\\/I7H \\/\\/3ird Chars &é\"'(-è_çà)=", causes.get(1));
    }
}
