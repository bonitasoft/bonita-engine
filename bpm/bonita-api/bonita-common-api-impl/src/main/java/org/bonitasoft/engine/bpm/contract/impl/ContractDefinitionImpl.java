/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.contract.impl;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.contract.ComplexInputDefinition;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.RuleDefinition;
import org.bonitasoft.engine.bpm.contract.SimpleInputDefinition;

/**
 * @author Matthieu Chaffotte
 * @author Laurent Leseigneur
 */
public class ContractDefinitionImpl implements ContractDefinition {

    private static final long serialVersionUID = 786706819903231008L;

    private final List<RuleDefinition> rules;

    private final List<ComplexInputDefinition> complexInputs;

    private final List<SimpleInputDefinition> simpleInputs;

    public ContractDefinitionImpl() {
        simpleInputs = new ArrayList<SimpleInputDefinition>();
        complexInputs = new ArrayList<ComplexInputDefinition>();
        rules = new ArrayList<RuleDefinition>();
    }

    public void addSimpleInput(final SimpleInputDefinition input) {
        simpleInputs.add(input);
    }

    public void addComplexInput(final ComplexInputDefinition complexInput) {
        complexInputs.add(complexInput);
    }

    @Override
    public List<RuleDefinition> getRules() {
        return rules;
    }

    public void addRule(final RuleDefinition rule) {
        rules.add(rule);
    }

    @Override
    public List<SimpleInputDefinition> getSimpleInputs() {
        return simpleInputs;
    }

    @Override
    public List<ComplexInputDefinition> getComplexInputs() {
        return complexInputs;
    }

}
