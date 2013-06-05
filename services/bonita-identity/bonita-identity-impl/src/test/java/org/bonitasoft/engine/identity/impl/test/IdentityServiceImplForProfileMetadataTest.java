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
package org.bonitasoft.engine.identity.impl.test;

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
public class IdentityServiceImplForProfileMetadataTest {

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
     * Test method for
     * {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#createProfileMetadataDefinition(org.bonitasoft.engine.identity.model.SProfileMetadataDefinition)}
     * .
     */
    @Test
    public final void createProfileMetadataDefinition() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#createProfileMetadataValue(org.bonitasoft.engine.identity.model.SProfileMetadataValue)}.
     */
    @Test
    public final void createProfileMetadataValue() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#deleteProfileMetadataDefinition(long)}.
     */
    @Test
    public final void deleteProfileMetadataDefinitionById() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#deleteProfileMetadataDefinition(org.bonitasoft.engine.identity.model.SProfileMetadataDefinition)}
     * .
     */
    @Test
    public final void deleteProfileMetadataDefinitionSByObject() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#deleteProfileMetadataValue(long)}.
     */
    @Test
    public final void deleteProfileMetadataValueById() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#deleteProfileMetadataValue(org.bonitasoft.engine.identity.model.SProfileMetadataValue)}.
     */
    @Test
    public final void deleteProfileMetadataValueByObject() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getNumberOfProfileMetadataDefinition()}.
     */
    @Test
    public final void getNumberOfProfileMetadataDefinition() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getProfileMetadataByName(java.lang.String)}.
     */
    @Test
    public final void getProfileMetadataByName() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getProfileMetadataDefinition(int, int)}.
     */
    @Test
    public final void getPaginatedProfileMetadataDefinition() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getProfileMetadataDefinition(long)}.
     */
    @Test
    public final void getProfileMetadataDefinitionById() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getProfileMetadataDefinitions(java.util.List)}.
     */
    @Test
    public final void getProfileMetadataDefinitions() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getProfileMetadataValue(long)}.
     */
    @Test
    public final void getProfileMetadataValue() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#getProfileMetadataValues(java.util.List)}.
     */
    @Test
    public final void getProfileMetadataValues() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#updateProfileMetadataDefinition(org.bonitasoft.engine.identity.model.SProfileMetadataDefinition, org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor)}
     * .
     */
    @Test
    public final void updateProfileMetadataDefinition() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.identity.impl.IdentityServiceImpl#updateProfileMetadataValue(org.bonitasoft.engine.identity.model.SProfileMetadataValue, org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor)}
     * .
     */
    @Test
    public final void updateProfileMetadataValue() {
        // TODO : Not yet implemented
    }

}
