package org.bonitasoft.engine.transaction;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;

import org.junit.Ignore;
import org.junit.Test;

import bitronix.tm.Configuration;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

public class TestBitronix {

    @Test
    @Ignore
    public void testSetupTransactionManager() throws Exception {
        PoolingDataSource myDataSource = new PoolingDataSource();
        myDataSource.setClassName("oracle.jdbc.xa.client.OracleXADataSource");
        myDataSource.setUniqueName("oracle");
        myDataSource.getDriverProperties().setProperty("user", "users1");
        myDataSource.getDriverProperties().setProperty("password", "users1");
        myDataSource.getDriverProperties().setProperty("URL", "jdbc:oracle:thin:@localhost:1521:XE");

        Configuration conf = TransactionManagerServices.getConfiguration();
        conf.setServerId("jvm-1");
        conf.setLogPart1Filename("/tmp/tx-logs/part1.btm");
        conf.setLogPart2Filename("/tmp/tx-logs/part2.btm");

        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "bitronix.tm.jndi.BitronixInitialContextFactory");
        Context ctx = new InitialContext(env);

        TransactionManager tm = (TransactionManager) ctx.lookup("java:comp/UserTransaction");

        tm.begin();
        System.out.println(tm.getTransaction());
        System.out.println(tm.getStatus());
        tm.commit();
    }

}
