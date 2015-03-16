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
package org.bonitasoft.engine.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.platform.exception.SPlatformAlreadyExistException;
import org.bonitasoft.engine.platform.exception.SPlatformNotFoundException;
import org.bonitasoft.engine.platform.exception.SPlatformUpdateException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.builder.SPlatformBuilder;
import org.bonitasoft.engine.platform.model.builder.SPlatformBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

public class PlatformServiceTest extends CommonBPMServicesTest {

    private final PlatformService platformService;

    public PlatformServiceTest() {
        platformService = getPlatformAccessor().getPlatformService();
    }

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
        platformService.createTables();

        getTransactionService().complete();
        getTransactionService().begin();
        platformService.createPlatform(platform);
        final SPlatform readPlatform = platformService.getPlatform();
        assertEquals(platform.getVersion(), readPlatform.getVersion());
        assertEquals(platform.getCreatedBy(), readPlatform.getCreatedBy());
        assertEquals(platform.getCreated(), readPlatform.getCreated());

        try {
            platformService.createTables();
            fail("Platform already exists...");
        } catch (final SPlatformAlreadyExistException e) {
            // OK
        }
        platformService.deletePlatform();
        getTransactionService().complete();
        platformService.deleteTables();

        // check the platform was well deleted and try to recreate it
        platformService.createTables();
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
            platformService.getPlatform();
            fail("getPlatform() should not succeed");
        } catch (final SPlatformNotFoundException e) {
            // OK
        } finally {
            getTransactionService().complete();
        }

        final SPlatform platform = BuilderFactory.get(SPlatformBuilderFactory.class)
                .createNewInstance(version, previousVersion, initialVersion, createdBy, created).done();
        platformService.createTables();
        getTransactionService().complete();
        platformService.createPlatform(platform);
        SPlatform readPlatform = platformService.getPlatform();
        assertNotNull(readPlatform);
        assertEquals(version, readPlatform.getVersion());
        assertEquals(createdBy, readPlatform.getCreatedBy());
        assertEquals(created, readPlatform.getCreated());

        platformService.deletePlatform();

        getTransactionService().complete();

        platformService.deleteTables();
    }

    @Test(expected = SPlatformUpdateException.class)
    public void testUpdateInexistantPlatform() throws Exception {
        getTransactionService().begin();
        try {
            SPlatform platform = BuilderFactory.get(SPlatformBuilderFactory.class)
                    .createNewInstance("myVersion", "previousVersion", "initialVersion", "mycreatedBy", System.currentTimeMillis()).done();
            platformService.updatePlatform(platform, new EntityUpdateDescriptor());
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

        platformService.updatePlatform(platform, updateDescriptor);

        assertEquals(newCreatedBy, platform.getCreatedBy());
        assertEquals(newInitialVersion, platform.getInitialVersion());
        assertEquals(newPreviousVersion, platform.getPreviousVersion());
        assertEquals(newVersion, platform.getVersion());
        assertEquals(newCreated, platform.getCreated());

        platform = platformService.getPlatform();

        assertEquals(newCreatedBy, platform.getCreatedBy());
        assertEquals(newInitialVersion, platform.getInitialVersion());
        assertEquals(newPreviousVersion, platform.getPreviousVersion());
        assertEquals(newVersion, platform.getVersion());
        assertEquals(newCreated, platform.getCreated());

        platformService.deletePlatform();
        getTransactionService().complete();

        platformService.deleteTables();
    }

}
