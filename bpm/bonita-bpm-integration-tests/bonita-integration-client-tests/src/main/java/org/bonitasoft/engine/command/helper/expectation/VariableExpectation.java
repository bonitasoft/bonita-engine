/*
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

package org.bonitasoft.engine.command.helper.expectation;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;

/**
 * @author Vincent Elcrin
 */
public class VariableExpectation {

    private CommonAPITest testCase;

    private ProcessInstance process;

    private String name;

    public VariableExpectation(CommonAPITest testCase, ProcessInstance process, String name) {
        this.testCase = testCase;
        this.process = process;
        this.name = name;
    }

    public void toBe(Serializable variable) throws DataNotFoundException {
        assertEquals(variable, testCase.getProcessAPI().getProcessDataInstance(name, process.getId()).getValue());
    }

}
