/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.filter.impl;

import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.filter.UserFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class SConnectorUserFilterAdapterTest {

    @Mock
    UserFilter userFilter;

    SConnectorUserFilterAdapter sConnectorUserFilterAdapter;

    @Before
    public void setUp() {
        sConnectorUserFilterAdapter = new SConnectorUserFilterAdapter(userFilter, "anActor");
    }

    @Test
    public void validate_should_call_validateInputParameters() throws Exception {
        sConnectorUserFilterAdapter.validate();

        verify(userFilter).validateInputParameters();
    }
}
