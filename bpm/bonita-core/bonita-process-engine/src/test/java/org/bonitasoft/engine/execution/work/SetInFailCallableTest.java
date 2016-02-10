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
package org.bonitasoft.engine.execution.work;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SetInFailCallableTest {

    @Mock
    private FailedStateSetter failedStateSetter;

    private final long FLOW_NODE_INSTANCE_ID = 15L;

    public static final long PROCESS_DEFINITION_ID = 25L;

    private SetInFailCallable setInFailCallable;

    @Before
    public void setUp() throws Exception {
        setInFailCallable = new SetInFailCallable(failedStateSetter, FLOW_NODE_INSTANCE_ID);
    }

    @Test
    public void call_should_setState() throws Exception {

        //when
        setInFailCallable.call();

        //then
        verify(failedStateSetter).setAsFailed(FLOW_NODE_INSTANCE_ID);
    }

}
