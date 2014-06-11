/**
 * Copyright (C) 2013 - 2014 BonitaSoft S.A.
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

import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SObjectReadException;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.instance.api.TokenService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SToken;
import org.bonitasoft.engine.execution.TokenProvider;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class FlowNodeCompletionTokenProvider implements TokenProvider {

    private final SFlowNodeDefinition sFlowNodeDefinition;

    private final TokenService tokenService;

    private final SFlowNodeInstance child;

    private final SProcessInstance processInstance;

    private final FlowNodeTransitionsWrapper transitionsDescriptor;

    private TokenInfo tokenInfo = null;

    public FlowNodeCompletionTokenProvider(final SFlowNodeInstance child, final SProcessInstance sProcessInstance,
            final SFlowNodeDefinition sFlowNodeDefinition,
            final FlowNodeTransitionsWrapper transitionsDescriptor, TokenService tokenService) {
        this.child = child;
        this.processInstance = sProcessInstance;
        this.sFlowNodeDefinition = sFlowNodeDefinition;
        this.transitionsDescriptor = transitionsDescriptor;
        this.tokenService = tokenService;
    }

    @Override
    public TokenInfo getOutputTokenInfo() throws SObjectReadException, SObjectNotFoundException {
        if (tokenInfo != null) {
            return tokenInfo;
        }
        tokenInfo = calculateTokenInfo();
        return tokenInfo;
    }

    private TokenInfo calculateTokenInfo() throws SObjectReadException, SObjectNotFoundException {
        // not in the definition: no merge no split no implicit end
        if (sFlowNodeDefinition == null || transitionsDescriptor.isLastFlowNode()) {
            return new TokenInfo();
        }

        if (sFlowNodeDefinition.isBoundaryEvent() && sFlowNodeDefinition.isInterrupting()) {
            return tansmitAllTokenInfo();
        }

        if (sFlowNodeDefinition.isExclusive() || transitionsDescriptor.isSimpleMerge() || isNonInterruptingBoundaryEvent()) {
            // always transmit token
            return transmitOnlyTokenRefId();
        }

        if (transitionsDescriptor.isSimpleToMany()) {
            // 1 input , >1 output
            // create children input token
            return new TokenInfo(child.getId(), child.getTokenRefId());
        }

        if (transitionsDescriptor.isManyToMany()) {
            if (sFlowNodeDefinition.isParalleleOrInclusive()) {
                return new TokenInfo(child.getId(), getParentTokenRefId());
            }
            return new TokenInfo(child.getId(), child.getTokenRefId());
        }

        if (transitionsDescriptor.isManyToOne()) {
            if (sFlowNodeDefinition.isParalleleOrInclusive()) {
                Long parentTokenRefId = getParentTokenRefId();
                return new TokenInfo(parentTokenRefId);
            }
            return new TokenInfo(child.getTokenRefId());
        }

        return new TokenInfo();
    }

    private boolean isNonInterruptingBoundaryEvent() {
        return sFlowNodeDefinition.isBoundaryEvent() && !sFlowNodeDefinition.isInterrupting();
    }

    private Long getParentTokenRefId() throws SObjectReadException, SObjectNotFoundException {
        SToken token = tokenService.getToken(processInstance.getId(), child.getTokenRefId());
        return token.getParentRefId();
    }

    private TokenInfo transmitOnlyTokenRefId() {
        return new TokenInfo(child.getTokenRefId());
    }

    private TokenInfo tansmitAllTokenInfo() throws SObjectReadException, SObjectNotFoundException {
        SToken token = tokenService.getToken(child.getParentProcessInstanceId(), child.getTokenRefId());
        return new TokenInfo(token.getRefId(), token.getParentRefId());
    }

}
