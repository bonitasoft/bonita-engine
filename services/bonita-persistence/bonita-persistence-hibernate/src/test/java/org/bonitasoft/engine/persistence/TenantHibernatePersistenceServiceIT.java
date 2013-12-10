package org.bonitasoft.engine.persistence;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.model.Human;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.transaction.JTATransactionServiceImpl;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;

public class TenantHibernatePersistenceServiceIT {

    static TransactionService transactionService;

    static PersistenceService persistenceService;

    private static PoolingDataSource ds1;

    @BeforeClass
    public static void initServices() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.bonitasoft.engine.persistence.SimpleMemoryContextFactory");

        ds1 = new PoolingDataSource();
        ds1.setUniqueName("java:comp/env/bonitaDS");
        ds1.setClassName("org.h2.jdbcx.JdbcDataSource");
        ds1.setMaxPoolSize(3);
        ds1.setAllowLocalTransactions(true);
        ds1.getDriverProperties().put("URL", "jdbc:h2:mem:journal;LOCK_MODE=0;MVCC=TRUE;DB_CLOSE_ON_EXIT=TRUE;IGNORECASE=TRUE;");
        ds1.getDriverProperties().put("user", "sa");
        ds1.getDriverProperties().put("password", "");
        ds1.init();

        final DataSource dataSource = initDataSource();

        final Map<String, Object> mappings = new HashMap<String, Object>();
        mappings.put("java:comp/env/bonitaDS", ds1);
        mappings.put("java:comp/UserTransaction", TransactionManagerServices.getTransactionManager());

        final InitialContext ctx = new InitialContext();
        final MemoryJNDISetup jndiSetup = new MemoryJNDISetup(ctx, mappings);
        jndiSetup.init();

        final ReadSessionAccessor sessionAccessor = mock(ReadSessionAccessor.class);
        when(sessionAccessor.getTenantId()).thenReturn(1l);

        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final SequenceManager sequenceManager = mock(SequenceManager.class);

        final Properties properties = new Properties();
        properties.put("hibernate.connection.datasource", "java:comp/env/bonitaDS");
        properties.put("hibernate.current_session_context_class", "jta");
        properties.put("hibernate.transaction.factory_class", "org.hibernate.engine.transaction.internal.jta.JtaTransactionFactory");
        properties.put("hibernate.transaction.manager_lookup_class", "org.bonitasoft.engine.persistence.BTMJNDITransactionManagerLookup");
        properties.put("hibernate.dialect", "");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.use_sql_comments", "false");
        properties.put("hibernate.generate_statistics", "false");
        properties.put("hibernate.connection.shutdown", "true");
        properties.put("hibernate.validator.autoregister_listeners", "false");
        properties.put("hibernate.validator.apply_to_ddl", "false");
        properties.put("javax.persistence.validation.mode", "NONE");

        final List<HibernateResourcesProvider> resources = new ArrayList<HibernateResourcesProvider>();
        final HibernateResourcesProvider resourcesProvider = new HibernateResourcesProvider();
        resourcesProvider.setResources(new HashSet<String>(Arrays.asList("persistence/test/parent-child.hbm.xml",
                "persistence/test/parent-child.queries.hbm.xml", "persistence/test/car.hbm.xml", "persistence/test/car.queries.hbm.xml")));
        final Map<String, String> classAliasMappings = new HashMap<String, String>();
        classAliasMappings.put("org.bonitasoft.engine.persistence.model.Human", "human");
        classAliasMappings.put("org.bonitasoft.engine.persistence.model.Child", "child");
        classAliasMappings.put("org.bonitasoft.engine.persistence.model.Parent", "parent");
        classAliasMappings.put("org.bonitasoft.engine.persistence.model.Car", "car");
        resourcesProvider.setClassAliasMappings(classAliasMappings);
        resources.add(resourcesProvider);
        final HibernateResourcesConfigurationProviderImpl confProvider = new HibernateResourcesConfigurationProviderImpl();
        confProvider.setHbmResources(resources);

        final HibernateConfigurationProviderImpl provider = new HibernateConfigurationProviderImpl(properties, confProvider,
                Collections.<String, String> emptyMap(), Collections.<String> emptyList());

        final List<DBConfiguration> configurations = new ArrayList<DBConfiguration>();

        configurations.add(new DBConfiguration("/org/bonitasoft/engine/persistence/db/h2-createTables.sql",
                "/org/bonitasoft/engine/persistence/db/h2-dropTables.sql", null, "/org/bonitasoft/engine/persistence/db/h2-cleanTables.sql", "journal",
                "/org/bonitasoft/engine/persistence/db/h2-deleteTenantObjects.sql", 0));

        configurations.add(new DBConfiguration("/persistence/test/h2-createTables.sql", "/persistence/test/h2-dropTables.sql",
                "/persistence/test/h2-initTables.sql", null, "journal", "/persistence/test/h2-deleteTenantObjects.sql", 0));

        configurations.add(new DBConfiguration("/org/bonitasoft/engine/persistence/test/human/h2-postCreateStructure.sql", null, "journal"));

        final DBConfigurationsProvider dbConfigurationsProvider = new DBConfigurationsProvider();
        dbConfigurationsProvider.setTenantConfigurations(configurations);

        persistenceService = new TenantHibernatePersistenceService("journal", sessionAccessor, provider, dbConfigurationsProvider, ";", "#", logger,
                sequenceManager, dataSource);

        transactionService = new JTATransactionServiceImpl(logger, TransactionManagerServices.getTransactionManager());

        persistenceService.createStructure();
        transactionService.begin();
        final Map<String, String> replacements = Collections.singletonMap("tenantid", String.valueOf(1));
        persistenceService.initializeStructure(replacements);
        transactionService.complete();

    }

    private static DataSource initDataSource() throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver"); // also you need the MySQL driver
        final BoneCPConfig config = new BoneCPConfig();
        config.setJdbcUrl("jdbc:h2:mem:journal;LOCK_MODE=0;MVCC=TRUE;DB_CLOSE_ON_EXIT=TRUE;IGNORECASE=TRUE;");
        config.setUsername("sa");
        config.setPassword("");
        config.setMinConnectionsPerPartition(5); // if you say 5 here, there will be 10 connection available config.setMaxConnectionsPerPartition(10);
        config.setPartitionCount(2); // 2*5 = 10 connection will be available
        return new BoneCPDataSource(config);
    }

    @AfterClass
    public static void clear() {
        ds1.close();
    }

    @After
    public void closeTransactionIfNeeded() throws Exception {
        if (transactionService.isTransactionActive()) {
            transactionService.setRollbackOnly();
            transactionService.complete();
        }
    }

    @Test
    public void insertAndDeleteInASameTransaction() throws Exception {
        transactionService.begin();

        final Human human1 = buildHuman("Homer", "Simpson", 42);

        persistenceService.insert(human1);

        final List<OrderByOption> orderByOptions = new ArrayList<OrderByOption>();
        orderByOptions.add(new OrderByOption(Human.class, "firstName", OrderByType.ASC));
        orderByOptions.add(new OrderByOption(Human.class, "lastName", OrderByType.DESC));

        final SelectListDescriptor<Human> selectDescriptor = new SelectListDescriptor<Human>("getAllHumans", null, Human.class,
                new QueryOptions(orderByOptions));
        List<Human> allHumans;

        allHumans = persistenceService.selectList(selectDescriptor);
        assertEquals(1, allHumans.size());
        assertEquals(human1, allHumans.get(0));

        persistenceService.delete(human1);

        transactionService.complete();

        transactionService.begin();
        allHumans = persistenceService.selectList(selectDescriptor);
        assertEquals(0, allHumans.size());
        transactionService.complete();
    }

    @Test
    public void insertAndDeleteInSeperateTransactions() throws Exception {
        transactionService.begin();

        final Human human1 = buildHuman("Homer", "Simpson", 42);

        persistenceService.insert(human1);

        final List<OrderByOption> orderByOptions = new ArrayList<OrderByOption>();
        orderByOptions.add(new OrderByOption(Human.class, "firstName", OrderByType.ASC));
        orderByOptions.add(new OrderByOption(Human.class, "lastName", OrderByType.DESC));

        final SelectListDescriptor<Human> selectDescriptor = new SelectListDescriptor<Human>("getAllHumans", null, Human.class,
                new QueryOptions(orderByOptions));
        List<Human> allHumans;
        transactionService.complete();

        transactionService.begin();
        allHumans = persistenceService.selectList(selectDescriptor);
        assertEquals(1, allHumans.size());
        assertEquals(human1, allHumans.get(0));

        persistenceService.delete(human1);

        transactionService.complete();

        transactionService.begin();
        allHumans = persistenceService.selectList(selectDescriptor);
        assertEquals(0, allHumans.size());
        transactionService.complete();
    }

    protected static Human buildHuman(final String firstName, final String lastName, final int age) {
        final Human human = new Human();
        human.setFirstName(firstName);
        human.setLastName(lastName);
        human.setAge(age);
        human.setDeleted(false);
        return human;
    }

}
