/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.execution;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class FlowNodeTransitionsDescriptor {
    
    private int inputTransitionsSize;
    
    private List<STransitionDefinition> allOutgoingTransitionDefinitions;
    
    private List<STransitionDefinition> validOutgoingTransitionDefinitions;
    
    public int getInputTransitionsSize() {
        return inputTransitionsSize;
    }

    
    public void setInputTransitionsSize(int inputTransitionsSize) {
        this.inputTransitionsSize = inputTransitionsSize;
    }

    
    public List<STransitionDefinition> getAllOutgoingTransitionDefinitions() {
        return Collections.unmodifiableList(allOutgoingTransitionDefinitions);
    }

    public void setAllOutgoingTransitionDefinitions(List<STransitionDefinition> allOutgoingTransitionDefinitions) {
        this.allOutgoingTransitionDefinitions = allOutgoingTransitionDefinitions;
    }

    
    public List<STransitionDefinition> getValidOutgoingTransitionDefinitions() {
        return Collections.unmodifiableList(validOutgoingTransitionDefinitions);
    }

    public void setValidOutgoingTransitionDefinitions(List<STransitionDefinition> validOutgoingTransitionDefinitions) {
        this.validOutgoingTransitionDefinitions = validOutgoingTransitionDefinitions;
    }
    
}
