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
package org.bonitasoft.engine.log.technical;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.LoggerFactory;

/**
 * @author Baptiste Mesta
 */
public class TechnicalLoggerServiceTest {

    /**
     * @author Baptiste Mesta
     */
    private final class RunnableImplementation implements Runnable {

        /**
     * 
     */
        private final TechnicalLoggerSLF4JImpl techLogger;

        /**
         * @param techLogger
         */
        private RunnableImplementation(final TechnicalLoggerSLF4JImpl techLogger) {
            this.techLogger = techLogger;
        }

        @Override
        public void run() {
            techLogger.log(this.getClass(), TechnicalLogSeverity.INFO, "just logged");
        }
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void setUp() {
        LoggerFactory.getLogger(TechnicalLoggerServiceTest.class).info("Testing : {}", name.getMethodName());

    }

    @After
    public void tearDown() {
        LoggerFactory.getLogger(TechnicalLoggerServiceTest.class).info("Tested: {}", name.getMethodName());
    }

    // @Test
    public void testLogDebug() throws IOException {
        TechnicalLoggerSLF4JImpl techLogger = new TechnicalLoggerSLF4JImpl();
        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        PrintStream out = System.out;
        System.setOut(new PrintStream(myOut));

        techLogger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "just logged");
        final String standardOutput = myOut.toString();
        myOut.close();
        System.setOut(out);
        System.out.println("log was:" + standardOutput);
        assertTrue(standardOutput.contains("just logged"));
        assertTrue(standardOutput.toLowerCase().contains("debug"));
        assertTrue(standardOutput.contains("TechnicalLoggerServiceTest"));
    }

    @Test
    public void testLogInfo() throws IOException {
        TechnicalLoggerSLF4JImpl techLogger = new TechnicalLoggerSLF4JImpl();
        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        PrintStream out = System.out;
        System.setOut(new PrintStream(myOut));

        techLogger.log(this.getClass(), TechnicalLogSeverity.INFO, "just logged an info");
        final String standardOutput = myOut.toString();
        myOut.close();
        System.setOut(out);
        System.out.println("log was:" + standardOutput);
        assertTrue(standardOutput.contains("just logged an info"));
        assertTrue(standardOutput.toLowerCase().contains("info"));
        assertTrue(standardOutput.contains("TechnicalLoggerServiceTest"));
    }

    @Test
    public void testLogWarning() throws IOException {
        TechnicalLoggerSLF4JImpl techLogger = new TechnicalLoggerSLF4JImpl();
        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        PrintStream out = System.out;
        System.setOut(new PrintStream(myOut));

        techLogger.log(this.getClass(), TechnicalLogSeverity.WARNING, "logged a warning");
        final String standardOutput = myOut.toString();
        myOut.close();
        System.setOut(out);
        System.out.println("log was:" + standardOutput);
        assertTrue(standardOutput.contains("logged a warning"));
        assertTrue(standardOutput.toLowerCase().contains("warn"));
        assertTrue(standardOutput.contains("TechnicalLoggerServiceTest"));
    }

    @Test
    public void testLogError() throws IOException {
        TechnicalLoggerSLF4JImpl techLogger = new TechnicalLoggerSLF4JImpl();
        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        PrintStream out = System.out;
        System.setOut(new PrintStream(myOut));

        techLogger.log(this.getClass(), TechnicalLogSeverity.ERROR, "just logged an error");
        final String standardOutput = myOut.toString();
        myOut.close();
        System.setOut(out);
        System.out.println("log was:" + standardOutput);
        assertTrue(standardOutput.contains("just logged an error"));
        assertTrue(standardOutput.toLowerCase().contains("error"));
        assertTrue(standardOutput.contains("TechnicalLoggerServiceTest"));
    }

    @Test
    public void testLogInfoFromNestedClass() throws IOException {

        TechnicalLoggerSLF4JImpl techLogger = new TechnicalLoggerSLF4JImpl();
        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        PrintStream out = System.out;
        System.setOut(new PrintStream(myOut));

        Runnable runnable = new RunnableImplementation(techLogger);
        runnable.run();
        final String standardOutput = myOut.toString();
        myOut.close();
        System.setOut(out);
        System.out.println("log was:" + standardOutput);
        assertTrue(standardOutput.contains("just logged"));
        assertTrue(standardOutput.toLowerCase().contains("info"));
        assertTrue(standardOutput.contains("RunnableImplementation"));
    }

}
