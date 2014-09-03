/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
