/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.identity.impl;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.identity.impl.IdentityServiceImpl;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.recorder.Recorder;
import org.junit.Before;
import org.junit.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Celine Souchet
 * 
 */
public class IdentityServiceImplForRoleTest {

    @Mock
    private Recorder recorder;

    @Mock
    private ReadPersistenceService persistenceService;

    @Mock
    private EventService eventService;

    @Mock
    private TechnicalLoggerService logger;

    @InjectMocks
    private IdentityServiceImpl identityServiceImpl;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#createRole(org.bonitasoft.engine.identity.model.SRole)}.
     */
    @Test
    public final void createRole() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#deleteRole(long)}.
     */
    @Test
    public final void deleteRoleById() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#deleteRole(org.bonitasoft.engine.identity.model.SRole)}.
     */
    @Test
    public final void deleteRoleByObject() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getNumberOfRoles()}.
     */
    @Test
    public final void getNumberOfRoles() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getRole(long)}.
     */
    @Test
    public final void getRole() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getRoleByName(java.lang.String)}.
     */
    @Test
    public final void getRoleByName() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getRoles(int, int)}.
     */
    @Test
    public final void getPaginatedRoles() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getRoles(int, int, java.lang.String, org.bonitasoft.engine.persistence.OrderByType)}.
     */
    @Test
    public final void getPaginatedRolesWithOrder() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getRoles(java.util.List)}.
     */
    @Test
    public final void getRolesByIds() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#updateRole(org.bonitasoft.engine.identity.model.SRole, org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor)}
     * .
     */
    @Test
    public final void updateRole() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getNumberOfRoles(org.bonitasoft.engine.persistence.QueryOptions)}.
     */
    @Test
    public final void getNumberOfRolesWithOptions() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#searchRoles(org.bonitasoft.engine.persistence.QueryOptions)}.
     */
    @Test
    public final void searchRoles() {
        // TODO : Not yet implemented
    }

}
