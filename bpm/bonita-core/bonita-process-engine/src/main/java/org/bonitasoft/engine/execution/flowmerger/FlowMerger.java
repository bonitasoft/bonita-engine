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
package org.bonitasoft.engine.execution.flowmerger;

import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SObjectReadException;
import org.bonitasoft.engine.execution.TokenProvider;

/**
 * @author Elias Ricken de Medeiros
 * 
 */
public class FlowMerger {

    private final SFlowNodeWrapper flowNodewrapper;

    private final FlowNodeTransitionsWrapper transitionsWrapper;

    private final TokenProvider tokenProvider;

    public FlowMerger(final SFlowNodeWrapper flowNodeWrapper, final FlowNodeTransitionsWrapper transitionsDescriptor, TokenProvider tokenProvider) {
        super();
        this.flowNodewrapper = flowNodeWrapper;
        this.transitionsWrapper = transitionsDescriptor;
        this.tokenProvider = tokenProvider;
    }

    /**
     * @return return true if the input token must be consume on taking the next transition; false otherwise.
     */
    public boolean mustConsumeInputTokenOnTakingTransition() {
        return !flowNodewrapper.isBoundaryEvent()
                && flowNodewrapper.isParalleleOrInclusive()
                && !transitionsWrapper.isLastFlowNode()
                && transitionsWrapper.hasMultipleIncomingTransitions();
    }

    public boolean mustCreateTokenOnFinish() {
        if (flowNodewrapper.isNull() || flowNodewrapper.isBoundaryEvent() || flowNodewrapper.isExclusive() || transitionsWrapper.isLastFlowNode()) {
            return false;
        }
        return transitionsWrapper.hasMultipleOutgoingTransitions();
    }

    public boolean isImplicitEnd() {
        if (flowNodewrapper.isNull()) {
            return false;
        }
        return transitionsWrapper.isLastFlowNode();
    }

    public TokenInfo getOutputTokenInfo() throws SObjectReadException, SObjectNotFoundException, SObjectCreationException {
        return tokenProvider.getOutputTokenInfo();
    }

}
