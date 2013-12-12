package org.bonitasoft.engine.persistence;

import javax.transaction.TransactionManager;

import org.hibernate.service.jta.platform.internal.BitronixJtaPlatform;

public class JNDIBitronixJtaPlatform extends BitronixJtaPlatform {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected TransactionManager locateTransactionManager() {
        // Force the lookup to JNDI to find the TransactionManager : since we share it between
        // Hibernate and Quartz, I prefer to force the JNDI lookup in order to be sure that
        // they are using the same instance.
        return (TransactionManager) jndiService().locate( "java:comp/UserTransaction" );
    }
}
