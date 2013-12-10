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

import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SObjectReadException;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SGatewayDefinition;
import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.instance.api.TokenService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SToken;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class GatewayMergeDescriptor {
    
    private boolean consumeInputToken;
    
    private boolean createToken;
    
    private boolean implicitEnd;
    
    private Long outputTokenRefId;
    
    private Long outputParentTokenRefId;
    
    private boolean isParalleleOrInclusive = false;
    
    private boolean isExclusive = false;

    public GatewayMergeDescriptor(final SFlowNodeInstance child, final SProcessInstance sProcessInstance,
            final SFlowNodeDefinition flowNode, final FlowNodeTransitionsDescriptor transitionsDescriptor, TokenService tokenService) throws SObjectReadException, SObjectNotFoundException {
        super();
        setGatewayType(flowNode);
        
        handleTokenUpdateInfo(child, sProcessInstance, flowNode, transitionsDescriptor, tokenService);
    }

    private void handleTokenUpdateInfo(final SFlowNodeInstance child, final SProcessInstance sProcessInstance, final SFlowNodeDefinition flowNode,
            final FlowNodeTransitionsDescriptor transitionsDescriptor, TokenService tokenService) throws SObjectReadException, SObjectNotFoundException {
        if (flowNode != null) { // if null -> not in the definition: no merge no split no implicit end
            if (SFlowNodeType.BOUNDARY_EVENT.equals(child.getType())) {
                setBoundaryMergeInfo(child, tokenService);
            } else {
                setNormalFlowNodeMergeInfo(child, sProcessInstance, transitionsDescriptor, tokenService);
            }
        }
    }

    private void setNormalFlowNodeMergeInfo(final SFlowNodeInstance child, final SProcessInstance sProcessInstance,
            final FlowNodeTransitionsDescriptor transitionsDescriptor, TokenService tokenService) throws SObjectReadException, SObjectNotFoundException {
        if (transitionsDescriptor.getValidOutgoingTransitionDefinitions().size() > 0) {
            // depending on the split type and number of input/output transition we determine if token are created, with with type and if we consume
            // incoming
            // tokens
            if (isExclusive) {
                // always transmit token
                outputTokenRefId = child.getTokenRefId();
            } else {
                setMergeInfoForNonExclusiveGateway(child, sProcessInstance, transitionsDescriptor, tokenService);
            }
        } else {
            // implicit end
            implicitEnd = true;
            consumeInputToken = false;
            createToken = false;
        }
    }

    private void setMergeInfoForNonExclusiveGateway(final SFlowNodeInstance child, final SProcessInstance sProcessInstance,
            final FlowNodeTransitionsDescriptor transitionsDescriptor, TokenService tokenService) throws SObjectReadException, SObjectNotFoundException {
        if (transitionsDescriptor.getInputTransitionsSize() <= 1) {
            if (transitionsDescriptor.getAllOutgoingTransitionDefinitions().size() <= 1) {
                // 1 input , 1 output
                // input token is transmit
                outputTokenRefId = child.getTokenRefId();
            } else {
                // 1 input , >1 output
                // create children input token
                outputTokenRefId = child.getId();
                outputParentTokenRefId = child.getTokenRefId();
                createToken = true;
            }
        } else {
            if (transitionsDescriptor.getAllOutgoingTransitionDefinitions().size() <= 1) {
                // >1 input , 1 output
                // consume input and transmit parent token for parallele and inclusive
                if (isParalleleOrInclusive) {
                    // consume input token + transmit parent token
                    consumeInputToken = true;
                    final SToken token = tokenService.getToken(sProcessInstance.getId(), child.getTokenRefId());
                    outputTokenRefId = token.getParentRefId();
                    createToken = false;
                } else {
                    // implicit gateway: input token is transmit
                    consumeInputToken = false;
                    outputTokenRefId =child.getTokenRefId();
                    createToken = false;
                }
            } else {
                // >1 input , >1 output
                if (isParalleleOrInclusive) {
                    // consume input tokens and create children token having the same parent
                    consumeInputToken = true;
                    outputTokenRefId =child.getId();
                    // TODO get ParentTokenRefId
                    final SToken token = tokenService.getToken(sProcessInstance.getId(), child.getTokenRefId());
                    outputParentTokenRefId = token.getParentRefId();
                    createToken = true;
                } else {
                    // implicit gateway: create children token of input
                    consumeInputToken = false;
                    outputTokenRefId =child.getId();
                    outputParentTokenRefId = child.getTokenRefId();
                    createToken = true;
                }
            }
        }
    }

    private void setBoundaryMergeInfo(final SFlowNodeInstance child, TokenService tokenService) throws SObjectReadException, SObjectNotFoundException {
        // the creation of tokens for the boundaries are done inside the ExecutingBoundaryEventStateImpl
        // we don't change tokens

        // still need to get the refId to put on the next element
        final SBoundaryEventInstance boundaryEventInstance = (SBoundaryEventInstance) child;
        if (boundaryEventInstance.isInterrupting()) {
            // we create the same token that activated the activity
            // the activity is canceled so a token will be consumed by the aborted activity
            final SToken token = tokenService.getToken(boundaryEventInstance.getParentProcessInstanceId(), boundaryEventInstance.getTokenRefId());
            outputTokenRefId = token.getRefId();
            outputParentTokenRefId = token.getParentRefId();
        } else {
            // a token with no parent is produced -> not the same "execution" than activity
            outputTokenRefId = boundaryEventInstance.getId();
            outputParentTokenRefId = null;
        }
    }

    private void setGatewayType(final SFlowNodeDefinition flowNode) {
        if (SFlowNodeType.GATEWAY.equals(flowNode.getType())) {
            final SGatewayType gatewayType = ((SGatewayDefinition) flowNode).getGatewayType();
            switch (gatewayType) {
                case EXCLUSIVE:
                    isExclusive = true;
                    break;
                case INCLUSIVE:
                    isParalleleOrInclusive = true;
                    break;
                case PARALLEL:
                    isParalleleOrInclusive = true;
                    break;
            }
        }
    }
    
    public boolean mustConsumeInputToken() {
        return consumeInputToken;
    }
    
    public boolean mustCreateToken() {
        return createToken;
    }
    
    public boolean isImplicitEnd() {
        return implicitEnd;
    }
    
    public void setConsumeInputToken(boolean consumeInputToken) {
        this.consumeInputToken = consumeInputToken;
    }
    
    public void setCreateToken(boolean createToken) {
        this.createToken = createToken;
    }
    
    public void setImplicitEnd(boolean implicitEnd) {
        this.implicitEnd = implicitEnd;
    }

    
    public Long getOutputTokenRefId() {
        return outputTokenRefId;
    }

    public void setOutputTokenRefId(Long outputTokenRefId) {
        this.outputTokenRefId = outputTokenRefId;
    }

    
    public Long getOutputParentTokenRefId() {
        return outputParentTokenRefId;
    }

    
    public void setOutputParentTokenRefId(Long outputParentTokenRefId) {
        this.outputParentTokenRefId = outputParentTokenRefId;
    }

    public boolean isParalleleOrInclusive() {
        return isParalleleOrInclusive;
    }

    
    public void setParalleleOrInclusive(boolean isParalleleOrInclusive) {
        this.isParalleleOrInclusive = isParalleleOrInclusive;
    }

    
    public boolean isExclusive() {
        return isExclusive;
    }

    
    public void setExclusive(boolean isExclusive) {
        this.isExclusive = isExclusive;
    }
    

}
