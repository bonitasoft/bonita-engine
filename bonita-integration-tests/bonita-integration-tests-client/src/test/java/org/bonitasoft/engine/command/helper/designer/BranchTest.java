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
package org.bonitasoft.engine.command.helper.designer;

import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Vincent Elcrin
 */
@RunWith(MockitoJUnitRunner.class)
public class BranchTest {

    @Mock
    ProcessDefinitionBuilder builder;

    @Test
    public void bind_should_link_first_fragment_of_the_branch() {
        Branch branch = new Branch().start(new UserTask("2")).then(new UserTask("3"));

        branch.bind(Arrays.<Fragment> asList(new UserTask("1")), builder);

        verify(builder).addTransition("1", "2");
    }

    @Test
    public void bind_should_link_last_fragment_of_the_branch() {
        Branch branch = new Branch().start(new UserTask("1")).then(new UserTask("2"));
        UserTask task = new UserTask("3");

        task.bind(Arrays.<Fragment> asList(branch), builder);

        verify(builder).addTransition("2", "3");
    }
}
