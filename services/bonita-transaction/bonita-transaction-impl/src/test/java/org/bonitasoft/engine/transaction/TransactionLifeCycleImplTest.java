package org.bonitasoft.engine.transaction;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.FireEventException;
import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.events.model.HandlerUnregistrationException;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.events.model.builders.SEventBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import bitronix.tm.Configuration;
import bitronix.tm.TransactionManagerServices;

public class TransactionLifeCycleImplTest extends TransactionLifeCycleTest {

    @BeforeClass
    public static void setUpJNDIBitronixTransactionManager() {
        final Configuration conf = TransactionManagerServices.getConfiguration();
        // TODO Make the following configurable
        conf.setServerId("jvm-1");
        conf.setLogPart1Filename("/tmp/tx-logs/part1.btm");
        conf.setLogPart2Filename("/tmp/tx-logs/part2.btm");
    }

    @AfterClass
    public static void stopJNDIBitronixTransactionManager() {
        TransactionManagerServices.getTransactionManager().shutdown();
    }

    @Override
    protected TransactionService getTxService() {
        return new JTATransactionServiceImpl(getLoggerService(), getTransactionManager(), getEventService());
    }

    private TransactionManager getTransactionManager() {
        try {
            final Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "bitronix.tm.jndi.BitronixInitialContextFactory");

            final Context ctx = new InitialContext(env);
            return (TransactionManager) ctx.lookup("java:comp/UserTransaction");
        } catch (final NamingException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO See to use some mocks instead of Anonymous classes.
    protected EventService getEventService() {
        return new EventService() {

            @Override
            public void removeHandler(final String eventType, final SHandler<SEvent> handler) throws HandlerUnregistrationException {
            }

            @Override
            public void removeAllHandlers(final SHandler<SEvent> handler) throws HandlerUnregistrationException {
            }

            @Override
            public Map<String, Set<SHandler<SEvent>>> getRegisteredHandlers() {
                return Collections.emptyMap();
            }

            @Override
            public Set<SHandler<SEvent>> getHandlers(final String eventType) {
                return null;
            }

            @Override
            public SEventBuilder getEventBuilder() {
                return getSEventBuilder();
            }

            private SEventBuilder getSEventBuilder() {
                return new SEventBuilder() {

                    private SEvent event;

                    @Override
                    public SEventBuilder createNewInstance(final String type) {
                        event = new SEvent() {

                            @Override
                            public String getType() {
                                return type;
                            }

                            @Override
                            public Object getObject() {
                                return null;
                            }

                            @Override
                            public void setObject(final Object ob) {
                            }
                        };
                        return this;
                    }

                    @Override
                    public SEvent done() {
                        return event;
                    }

                    @Override
                    public SEventBuilder setObject(final Object ob) {
                        event.setObject(ob);
                        return this;
                    }

                    @Override
                    public SEventBuilder createInsertEvent(final String type) {
                        return this;
                    }

                    @Override
                    public SEventBuilder createDeleteEvent(final String type) {
                        return this;
                    }

                    @Override
                    public SEventBuilder createUpdateEvent(final String type) {
                        return this;
                    }
                };

            }

            @Override
            public void fireEvent(final SEvent event) throws FireEventException {
            }

            @Override
            public void addHandler(final String eventType, final SHandler<SEvent> handler) throws HandlerRegistrationException {
            }

            @Override
            public boolean hasHandlers(final String eventType, final EventActionType actionType) {
                return false;
            }
        };
    }

    // TODO See to use some mocks instead of Anonymous classes.
    protected TechnicalLoggerService getLoggerService() {
        
        return new TechnicalLoggerService() {
            
            @Override
            public void log(Class<?> callerClass, TechnicalLogSeverity severity, String message, Throwable t) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void log(Class<?> callerClass, TechnicalLogSeverity severity, String message) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void log(Class<?> callerClass, TechnicalLogSeverity severity, Throwable t) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public boolean isLoggable(Class<?> callerClass, TechnicalLogSeverity severity) {
                // TODO Auto-generated method stub
                return false;
            }
        };
        

    }
    
}
