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
package org.bonitasoft.engine.platform.command.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.platform.command.SPlatformCommandGettingException;
import org.bonitasoft.engine.platform.command.SPlatformCommandNotFoundException;
import org.bonitasoft.engine.platform.command.model.SPlatformCommand;
import org.bonitasoft.engine.services.PersistenceService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Celine Souchet
 */
public class PlatformCommandServiceImplTest {

    @Mock
    private PersistenceService persistenceService;

    @Mock
    private TechnicalLoggerService logger;

    @InjectMocks
    private PlatformCommandServiceImpl platformCommandServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.command.impl.PlatformCommandServiceImpl#getPlatformCommand(java.lang.String)}.
     */
    @Test
    public final void getPlatformCommandByName() throws SBonitaReadException, SPlatformCommandNotFoundException, SPlatformCommandGettingException {
        final SPlatformCommand sPlatformCommand = mock(SPlatformCommand.class);
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<SPlatformCommand>> any())).thenReturn(sPlatformCommand);

        Assert.assertEquals(sPlatformCommand, platformCommandServiceImpl.getPlatformCommand("name"));
    }

    @Test(expected = SPlatformCommandNotFoundException.class)
    public final void getPlatformCommandByNameNotExists() throws SBonitaReadException, SPlatformCommandNotFoundException, SPlatformCommandGettingException {
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<SPlatformCommand>> any())).thenReturn(null);

        platformCommandServiceImpl.getPlatformCommand("name");
    }

    @Test(expected = SPlatformCommandGettingException.class)
    public final void getPlatformCommandByNameThrowException() throws SBonitaReadException, SPlatformCommandNotFoundException, SPlatformCommandGettingException {
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<SPlatformCommand>> any())).thenThrow(new SBonitaReadException(""));

        platformCommandServiceImpl.getPlatformCommand("name");
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.platform.command.impl.PlatformCommandServiceImpl#getPlatformCommands(org.bonitasoft.engine.persistence.QueryOptions)}.
     */
    @Test
    public final void getPlatformCommandsWithOptions() throws SBonitaReadException, SPlatformCommandGettingException {
        final List<SPlatformCommand> sPlatformCommands = new ArrayList<SPlatformCommand>();
        sPlatformCommands.add(mock(SPlatformCommand.class));
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<SPlatformCommand>> any())).thenReturn(sPlatformCommands);

        Assert.assertEquals(sPlatformCommands, platformCommandServiceImpl.getPlatformCommands(options));
    }

    @Test(expected = SPlatformCommandGettingException.class)
    public final void getPlatformCommandsWithOptionsThrowException() throws SBonitaReadException, SPlatformCommandGettingException {
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<SPlatformCommand>> any())).thenThrow(new SBonitaReadException(""));

        platformCommandServiceImpl.getPlatformCommands(options);
    }

}
