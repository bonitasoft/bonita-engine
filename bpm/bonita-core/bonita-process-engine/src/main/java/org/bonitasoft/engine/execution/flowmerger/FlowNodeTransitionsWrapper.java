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
package org.bonitasoft.engine.execution.flowmerger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;

/**
 * @author Elias Ricken de Medeiros
 */
public class FlowNodeTransitionsWrapper {

    //TODO: calculate the value of this three attributes in this class

    private int inputTransitionsSize;

    private List<STransitionDefinition> allOutgoingTransitionDefinitions = new ArrayList<STransitionDefinition>();

    private List<STransitionDefinition> validOutgoingTransitionDefinitions = new ArrayList<STransitionDefinition>();

    private STransitionDefinition defaultTransition;

    public int getInputTransitionsSize() {
        return inputTransitionsSize;
    }

    public void setInputTransitionsSize(final int inputTransitionsSize) {
        this.inputTransitionsSize = inputTransitionsSize;
    }

    public List<STransitionDefinition> getAllOutgoingTransitionDefinitions() {
        return allOutgoingTransitionDefinitions;
    }

    public void setAllOutgoingTransitionDefinitions(final List<STransitionDefinition> allOutgoingTransitionDefinitions) {
        if (allOutgoingTransitionDefinitions != null) {
            this.allOutgoingTransitionDefinitions = allOutgoingTransitionDefinitions;
        } else {
            this.allOutgoingTransitionDefinitions = new ArrayList<STransitionDefinition>();
        }
    }

    public List<STransitionDefinition> getValidOutgoingTransitionDefinitions() {
        return validOutgoingTransitionDefinitions;
    }

    public void setValidOutgoingTransitionDefinitions(final List<STransitionDefinition> validOutgoingTransitionDefinitions) {
        if (validOutgoingTransitionDefinitions != null) {
            this.validOutgoingTransitionDefinitions = validOutgoingTransitionDefinitions;
        } else {
            this.validOutgoingTransitionDefinitions = new ArrayList<STransitionDefinition>();
        }
    }

    public boolean isLastFlowNode() {
        return validOutgoingTransitionDefinitions == null || validOutgoingTransitionDefinitions.isEmpty();
    }

    public boolean hasMultipleOutgoingTransitions() {
        return hasMultipleElements(allOutgoingTransitionDefinitions);
    }

    private boolean hasMultipleElements(final Collection<?> collection) {
        return collection != null && collection.size() > 1;
    }

    public boolean hasMultipleIncomingTransitions() {
        return inputTransitionsSize > 1;
    }

    public boolean isManyToMany() {
        return hasMultipleIncomingTransitions() && hasMultipleOutgoingTransitions();
    }

    /**
     * from 0 or 1 input transition to one outgoing transition
     * 
     * @return true for flow node with 0 or 1 input transition and one outgoing transitions
     * @since 6.2
     */
    public boolean isSimpleMerge() {
        return !hasMultipleIncomingTransitions() && hasOneElement();
    }

    private boolean hasOneElement() {
        return allOutgoingTransitionDefinitions.size() == 1 || allOutgoingTransitionDefinitions.isEmpty() && validOutgoingTransitionDefinitions.size() == 1;
    }

    /**
     * from 0 or 1 input transition to more than one outgoing transitions
     * 
     * @return true for flow node with 0 or 1 input transition and more than one outgoing transitions
     * @since 6.2
     */
    public boolean isSimpleToMany() {
        return !hasMultipleIncomingTransitions() && hasMultipleOutgoingTransitions();
    }

    /**
     * from more than 1 input transition to one outgoing transition
     * 
     * @return true for flow node with more than 1 input transition and one outgoing transitions
     * @since 6.2
     */
    public boolean isManyToOne() {
        return hasMultipleIncomingTransitions() && hasOneElement();
    }

    public void setDefaultTransition(final STransitionDefinition defaultTransition) {
        this.defaultTransition = defaultTransition;
    }

    public STransitionDefinition getDefaultTransition() {
        return defaultTransition;
    }
}
