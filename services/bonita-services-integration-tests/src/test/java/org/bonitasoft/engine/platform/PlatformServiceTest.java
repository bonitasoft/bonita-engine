package org.bonitasoft.engine.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.builder.SPlatformBuilder;
import org.bonitasoft.engine.platform.model.builder.SPlatformBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

public class PlatformServiceTest extends CommonServiceTest {

    @Override
    @After
    public void tearDown() throws Exception {
        TestUtil.closeTransactionIfOpen(getTransactionService());
    }

    @Test
    public void testPlatformBuilder() {
        final String version = "myVersion";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();
        final String initialVersion = "initialVersion";
        final String previousVersion = "previousVersion";

        final SPlatformBuilder sPlatformBuilder = BuilderFactory.get(SPlatformBuilderFactory.class).createNewInstance(version, previousVersion, initialVersion,
                createdBy, created);

        final SPlatform platform = sPlatformBuilder.done();

        assertEquals(version, platform.getVersion());
        assertEquals(createdBy, platform.getCreatedBy());
        assertEquals(created, platform.getCreated());
        assertEquals(initialVersion, platform.getInitialVersion());
        assertEquals(previousVersion, platform.getPreviousVersion());
    }

    @Test
    public void testCreatePlatform() throws Exception {
        final String version = "myVersion";
        final String previousVersion = "previousVersion";
        final String initialVersion = "initialVersion";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        getTransactionService().begin();

        final SPlatform platform = BuilderFactory.get(SPlatformBuilderFactory.class)
                .createNewInstance(version, previousVersion, initialVersion, createdBy, created).done();
        getPlatformService().createPlatformTables();

        getTransactionService().complete();
        getTransactionService().begin();
        getPlatformService().createPlatform(platform);
        final SPlatform readPlatform = getPlatformService().getPlatform();
        assertEquals(platform.getVersion(), readPlatform.getVersion());
        assertEquals(platform.getCreatedBy(), readPlatform.getCreatedBy());
        assertEquals(platform.getCreated(), readPlatform.getCreated());

        try {
            getPlatformService().createPlatformTables();
            fail("Platform alreadyExist...");
        } catch (final SPlatformAlreadyExistException e) {
            // OK
        }
        getPlatformService().deletePlatform();
        getTransactionService().complete();
        getTransactionService().begin();
        getPlatformService().deletePlatformTables();
        getTransactionService().complete();

        getTransactionService().begin();
        // check the platform was well deleted and try to recreate it
        getPlatformService().createPlatformTables();
        getPlatformService().deletePlatformTables();
        getTransactionService().complete();
    }

    @Test
    @Ignore("rewrite this test: what do we want it to do?")
    public void testGetPlatform() throws Exception {
        final String version = "myVersion";
        final String previousVersion = "previousVersion";
        final String initialVersion = "initialVersion";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        getTransactionService().begin();
        try {
            getPlatformService().getPlatform();
            fail("getPlatform() should not succeed");
        } catch (final SPlatformNotFoundException e) {
            // OK
        } finally {
            getTransactionService().complete();
        }

        final SPlatform platform = BuilderFactory.get(SPlatformBuilderFactory.class)
                .createNewInstance(version, previousVersion, initialVersion, createdBy, created).done();
        getTransactionService().begin();
        getPlatformService().createPlatformTables();
        getTransactionService().complete();
        getTransactionService().begin();
        getPlatformService().createPlatform(platform);
        SPlatform readPlatform = getPlatformService().getPlatform();
        assertNotNull(readPlatform);
        assertEquals(version, readPlatform.getVersion());
        assertEquals(createdBy, readPlatform.getCreatedBy());
        assertEquals(created, readPlatform.getCreated());

        getPlatformService().deletePlatform();

        getTransactionService().complete();
        getTransactionService().begin();
        getPlatformService().deletePlatformTables();
        getTransactionService().complete();
    }

    @Test(expected = SPlatformUpdateException.class)
    public void testUpdateInexistantPlatform() throws Exception {
        getTransactionService().begin();
        try {
            SPlatform platform = BuilderFactory.get(SPlatformBuilderFactory.class)
                    .createNewInstance("myVersion", "previousVersion", "initialVersion", "mycreatedBy", System.currentTimeMillis()).done();
            getPlatformService().updatePlatform(platform, new EntityUpdateDescriptor());
        } finally {
            getTransactionService().complete();
        }
    }

    @Test
    public void testUpdatePlatform() throws Exception {
        final String version = "myVersion";
        final String previousVersion = "previousVersion";
        final String initialVersion = "initialVersion";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        getTransactionService().begin();

        SPlatform platform = BuilderFactory.get(SPlatformBuilderFactory.class).createNewInstance(version, previousVersion, initialVersion, createdBy, created)
                .done();
        final String newCreatedBy = "newCreatedBy";
        final String newInitialVersion = "initialVersion";
        final String newPreviousVersion = "previousVersion";
        final String newVersion = "newVersion";
        final long newCreated = System.currentTimeMillis();

        final SPlatformBuilderFactory fact = BuilderFactory.get(SPlatformBuilderFactory.class);

        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        updateDescriptor.addField(fact.getCreatedByKey(), newCreatedBy);
        updateDescriptor.addField(fact.getInitialVersionKey(), newInitialVersion);
        updateDescriptor.addField(fact.getPreviousVersionKey(), newPreviousVersion);
        updateDescriptor.addField(fact.getVersionKey(), newVersion);
        updateDescriptor.addField(fact.getCreatedKey(), newCreated);

        getPlatformService().updatePlatform(platform, updateDescriptor);

        assertEquals(newCreatedBy, platform.getCreatedBy());
        assertEquals(newInitialVersion, platform.getInitialVersion());
        assertEquals(newPreviousVersion, platform.getPreviousVersion());
        assertEquals(newVersion, platform.getVersion());
        assertEquals(newCreated, platform.getCreated());

        platform = getPlatformService().getPlatform();

        assertEquals(newCreatedBy, platform.getCreatedBy());
        assertEquals(newInitialVersion, platform.getInitialVersion());
        assertEquals(newPreviousVersion, platform.getPreviousVersion());
        assertEquals(newVersion, platform.getVersion());
        assertEquals(newCreated, platform.getCreated());

        getPlatformService().deletePlatform();
        getTransactionService().complete();
        getTransactionService().begin();
        getPlatformService().deletePlatformTables();
        getTransactionService().complete();
    }

}
