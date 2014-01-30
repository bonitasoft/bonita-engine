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
import org.bonitasoft.engine.core.process.instance.api.TokenService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SToken;

/**
 * @author Elias Ricken de Medeiros
 */
public class TokenProvider {

    private SFlowNodeWrapper flowNodeWrapper;

    private TokenService tokenService;

    private SFlowNodeInstance child;

    private SProcessInstance processInstance;

    private FlowNodeTransitionsWrapper transitionsDescriptor;
    
    private TokenInfo tokenInfo = null;

    public TokenProvider(final SFlowNodeInstance child, final SProcessInstance sProcessInstance, final SFlowNodeWrapper flowNodeWrapper,
            final FlowNodeTransitionsWrapper transitionsDescriptor, TokenService tokenService) {
        this.child = child;
        this.processInstance = sProcessInstance;
        this.flowNodeWrapper = flowNodeWrapper;
        this.transitionsDescriptor = transitionsDescriptor;
        this.tokenService = tokenService;
    }

    public TokenInfo getOutputTokenInfo() throws SObjectReadException, SObjectNotFoundException, SObjectCreationException {
        if(tokenInfo != null) {
            return tokenInfo;
        }
        tokenInfo = calculateTokenInfo();
        return tokenInfo;
    }
    
    private TokenInfo calculateTokenInfo() throws SObjectReadException, SObjectNotFoundException, SObjectCreationException {
     // not in the definition: no merge no split no implicit end
        if (flowNodeWrapper.isNull() || transitionsDescriptor.isLastFlowNode()) {
            return new TokenInfo();
        }

        if (flowNodeWrapper.isBoundaryEvent()) {
            return getOutputTokenRefIdFromBoundaryEvent();
        }

        if (flowNodeWrapper.isExclusive() || transitionsDescriptor.isSimpleMerge()) {
            // always transmit token
            return transmitToken();
        }
        
        if (transitionsDescriptor.isSimpleToMany()) {
            // 1 input , >1 output
            // create children input token
            return new TokenInfo(child.getId(), child.getTokenRefId());
        }
        
        if (transitionsDescriptor.isManyToMany()) {
            if(flowNodeWrapper.isParalleleOrInclusive()) {
                return new TokenInfo(child.getId(), getParentTokenRefId());
            }
            return  new TokenInfo(child.getId(), child.getTokenRefId());
        }
        
        if (transitionsDescriptor.isManyToOne()) {
            if (flowNodeWrapper.isParalleleOrInclusive()) {
                Long parentTokenRefId = getParentTokenRefId();
                return new TokenInfo(parentTokenRefId);
            } else {
                return new TokenInfo(child.getTokenRefId());
            }
        }
        
        return new TokenInfo();
    }
    

    private Long getParentTokenRefId() throws SObjectReadException, SObjectNotFoundException {
        SToken token = tokenService.getToken(processInstance.getId(), child.getTokenRefId());
        return token.getParentRefId();
    }

    private TokenInfo transmitToken() {
        return new TokenInfo(child.getTokenRefId());
    }

    private TokenInfo getOutputTokenRefIdFromBoundaryEvent() throws SObjectReadException, SObjectNotFoundException {
        // the creation of tokens for the boundaries are done inside the ExecutingBoundaryEventStateImpl
        // we don't change tokens
        // still need to get the refId to put on the next element
        if (flowNodeWrapper.isInterrupting()) {
            // we create the same token that activated the activity
            // the activity is canceled so a token will be consumed by the aborted activity
            SToken token = tokenService.getToken(child.getParentProcessInstanceId(), child.getTokenRefId());
            return new TokenInfo(token.getRefId(), token.getParentRefId());
        } else {
            // a token with no parent is produced -> not the same "execution" than activity
            return new TokenInfo(child.getId());
        }
    }

}
