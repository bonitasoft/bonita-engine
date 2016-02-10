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

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import bitronix.tm.Configuration;
import bitronix.tm.TransactionManagerServices;

public class TransactionResourceImplTest extends TransactionResourceTest {

    @BeforeClass
    public static void setUpJNDIBitronixTransactionManager() {
        final Configuration conf = TransactionManagerServices.getConfiguration();
        conf.setServerId("jvm-1");
        conf.setJournal(null); // Disable the journal for the tests.
    }

    @AfterClass
    public static void stopJNDIBitronixTransactionManager() {
        TransactionManagerServices.getTransactionManager().shutdown();
    }

    @Override
    protected TransactionService getTxService() {
        return new JTATransactionServiceImpl(getLoggerService(), getTransactionManager());
    }

    private TransactionManager getTransactionManager() {
        try {
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "bitronix.tm.jndi.BitronixInitialContextFactory");

            final Context ctx = new InitialContext(env);
            return (TransactionManager) ctx.lookup("java:comp/UserTransaction");
        } catch (final NamingException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO See to use some mocks instead of Anonymous classes.
    protected TechnicalLoggerService getLoggerService() {

        return new TechnicalLoggerService() {

            @Override
            public void log(final Class<?> callerClass, final TechnicalLogSeverity severity, final String message, final Throwable t) {
            }

            @Override
            public void log(final Class<?> callerClass, final TechnicalLogSeverity severity, final String message) {
            }

            @Override
            public void log(final Class<?> callerClass, final TechnicalLogSeverity severity, final Throwable t) {
            }

            @Override
            public boolean isLoggable(final Class<?> callerClass, final TechnicalLogSeverity severity) {
                return false;
            }
        };

    }

}
