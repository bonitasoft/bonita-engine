/**
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
 **/

package org.bonitasoft.platform.setup.command;

import static org.bonitasoft.platform.setup.command.CommandTestUtils.buildCommandLine;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.apache.commons.cli.Options;
import org.bonitasoft.platform.setup.PlatformSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class PushCommandTest {

    @Spy
    private PushCommand pushCommand;
    @Mock
    private PlatformSetup platformSetup;

    @Before
    public void before() throws Exception {
        doReturn(platformSetup).when(pushCommand).getPlatformSetup(any(String[].class));
    }

    @Test
    public void should_execute_push() throws Exception {
        pushCommand.execute(new Options(), buildCommandLine());

        verify(platformSetup).push(false);
    }

}
