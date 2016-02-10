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
package org.bonitasoft.engine.execution.transition;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;

/**
 * @author Elias Ricken de Medeiros
 */
public class EvaluatedTransitions {

    private List<STransitionDefinition> unconditionalTransitions = new ArrayList<STransitionDefinition>();
    private List<STransitionDefinition> trueTransitions = new ArrayList<STransitionDefinition>();
    private List<STransitionDefinition> falseTransitions = new ArrayList<STransitionDefinition>();

    /**
     * Add a transition to unconditional transitions
     * @param transitionDefinition unconditional transition
     */
    void addUnconditionalTransition(STransitionDefinition transitionDefinition) {
        unconditionalTransitions.add(transitionDefinition);
    }

    /**
     * Add a transition to transitions having conditions evaluated to true
     * @param transitionDefinition the transition which condition was evaluated to true
     */
    void addTrueTransition(STransitionDefinition transitionDefinition) {
        trueTransitions.add(transitionDefinition);
    }

    /**
     * Add a transition to transitions having conditions evaluated to false
     * @param transitionDefinition the transition which condition was evaluated to false
     */
    void addFalseTransition(STransitionDefinition transitionDefinition) {
        falseTransitions.add(transitionDefinition);
    }

    /**
     * @return list of unconditional transitions
     */
    public List<STransitionDefinition> getUnconditionalTransitions() {
        return unconditionalTransitions;
    }

    /**
     * @return list of transitions which conditions were evaluated to true
     */
    public List<STransitionDefinition> getTrueTransitions() {
        return trueTransitions;
    }

    /**
     * @return true if there is at least one unconditional transition; false otherwise
     */
    public boolean hasUnconditionalTransitions() {
        return !unconditionalTransitions.isEmpty();
    }

    /**
     * @return true if there is at least one transition which condition was evaluated to true; false otherwise
     */
    public boolean hasTrueTransitions() {
        return !trueTransitions.isEmpty();
    }

    /**
     * @return true if there is at least one transition which condition was evaluated to false; false otherwise
     */
    public boolean hasFalseTransitons() {
        return !falseTransitions.isEmpty();
    }

}
