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
package org.bonitasoft.engine.api.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SRuleDefinition;
import org.mvel2.MVEL;

/**
 * @author Matthieu Chaffotte
 */
public class ContractValidator {

    private final List<String> comments;

    public ContractValidator() {
        comments = new ArrayList<String>();
    }

    public boolean isValid(final SContractDefinition contract, final Map<String, Object> variables) {
        comments.clear();
        final List<SRuleDefinition> rules = contract.getRules();
        if (rules.isEmpty()) {
            for (final SInputDefinition input : contract.getInputs()) {
                if (!variables.containsKey(input.getName())) {
                    comments.add(input.getName() + " is not defined");
                }
            }
        } else {
            for (final SRuleDefinition rule : rules) {
                final Boolean valid = MVEL.evalToBoolean(rule.getExpression(), variables);
                if (!valid) {
                    comments.add(rule.getExplanation());
                }
            }
        }
        return comments.isEmpty();
    }

    public List<String> getComments() {
        return comments;
    }

}
