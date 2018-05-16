/*
 * Copyright (C) 2016 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 */
package org.bonitasoft.platform.setup;

import static org.mockito.Mockito.*;

import org.bonitasoft.platform.setup.jndi.MemoryJNDISetup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * author Emmanuel Duchastenier
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        PlatformSetupApplication.class
})
public class ScriptExecutorTest {

    @Autowired
    MemoryJNDISetup memoryJNDISetup;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private ScriptExecutor scriptExecutor;

    @Test
    public void createAndInitializePlatformIfNecessary_should_not_create_platform_if_already_existing() throws Exception {
        //given
        ScriptExecutor spy = spy(scriptExecutor);
        doReturn(true).when(spy).isPlatformAlreadyCreated();

        //when
        spy.createAndInitializePlatformIfNecessary();

        //then
        verify(spy, times(0)).createTables();
        verify(spy, times(0)).initializePlatformStructure();
        verify(spy, times(0)).insertPlatform();
    }

    @Test
    public void createAndInitializePlatformIfNecessary_should_create_platform_if_not_already_existing() throws Exception {
        //given
        ScriptExecutor spy = spy(scriptExecutor);

        //when
        spy.createAndInitializePlatformIfNecessary();

        //then
        verify(spy).createTables();
        verify(spy).initializePlatformStructure();
        verify(spy).insertPlatform();

        //cleanup
        scriptExecutor.deleteTables();
    }

}
