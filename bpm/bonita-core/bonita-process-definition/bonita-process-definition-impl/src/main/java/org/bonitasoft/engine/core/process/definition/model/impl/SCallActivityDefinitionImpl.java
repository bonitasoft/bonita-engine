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
package org.bonitasoft.engine.core.process.definition.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.CallActivityDefinition;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.model.SCallActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SCallableElementType;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.ServerModelConvertor;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SCallActivityDefinitionImpl extends SActivityDefinitionImpl implements SCallActivityDefinition {

    private static final long serialVersionUID = -5347512435504138388L;

    private SExpression callableElement;

    private SExpression callableElementVersion;

    private final List<SOperation> dataInputOperations;

    private final List<SOperation> dataOutputOperations;

    private SCallableElementType callableElementType;

    public SCallActivityDefinitionImpl(final long id, final String name) {
        super(id, name);
        dataInputOperations = new ArrayList<SOperation>(3);
        dataOutputOperations = new ArrayList<SOperation>(3);
    }

    public SCallActivityDefinitionImpl(final CallActivityDefinition activityDefinition,
            final Map<String, STransitionDefinition> transitionsMap) {
        super(activityDefinition, transitionsMap);
        callableElement = ServerModelConvertor.convertExpression(activityDefinition.getCallableElement());
        callableElementVersion = ServerModelConvertor.convertExpression(activityDefinition.getCallableElementVersion());
        dataInputOperations = ServerModelConvertor.convertOperations(activityDefinition.getDataInputOperations());
        dataOutputOperations = ServerModelConvertor.convertOperations(activityDefinition.getDataOutputOperations());
        callableElementType = SCallableElementType.valueOf(activityDefinition.getCallableElementType().name());

    }

    @Override
    public SExpression getCallableElement() {
        return callableElement;
    }

    public void setCallableElement(final SExpression callableElement) {
        this.callableElement = callableElement;
    }

    @Override
    public SExpression getCallableElementVersion() {
        return callableElementVersion;
    }

    public void setCallableElementVersion(final SExpression callableElementVersion) {
        this.callableElementVersion = callableElementVersion;
    }

    @Override
    public List<SOperation> getDataInputOperations() {
        return Collections.unmodifiableList(dataInputOperations);
    }

    public void addDataInputOperation(final SOperation dataInputOperation) {
        dataInputOperations.add(dataInputOperation);
    }

    @Override
    public List<SOperation> getDataOutputOperations() {
        return Collections.unmodifiableList(dataOutputOperations);
    }

    public void addDataOutputOperation(final SOperation dataOutputOperation) {
        dataOutputOperations.add(dataOutputOperation);
    }

    @Override
    public SCallableElementType getCallableElementType() {
        return callableElementType;
    }

    public void setCallableElementType(final SCallableElementType callableElementType) {
        this.callableElementType = callableElementType;
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.CALL_ACTIVITY;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (callableElement == null ? 0 : callableElement.hashCode());
        result = prime * result + (callableElementType == null ? 0 : callableElementType.hashCode());
        result = prime * result + (callableElementVersion == null ? 0 : callableElementVersion.hashCode());
        result = prime * result + (dataInputOperations == null ? 0 : dataInputOperations.hashCode());
        result = prime * result + (dataOutputOperations == null ? 0 : dataOutputOperations.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SCallActivityDefinitionImpl other = (SCallActivityDefinitionImpl) obj;
        if (callableElement == null) {
            if (other.callableElement != null) {
                return false;
            }
        } else if (!callableElement.equals(other.callableElement)) {
            return false;
        }
        if (callableElementType != other.callableElementType) {
            return false;
        }
        if (callableElementVersion == null) {
            if (other.callableElementVersion != null) {
                return false;
            }
        } else if (!callableElementVersion.equals(other.callableElementVersion)) {
            return false;
        }
        if (dataInputOperations == null) {
            if (other.dataInputOperations != null) {
                return false;
            }
        } else if (!dataInputOperations.equals(other.dataInputOperations)) {
            return false;
        }
        if (dataOutputOperations == null) {
            if (other.dataOutputOperations != null) {
                return false;
            }
        } else if (!dataOutputOperations.equals(other.dataOutputOperations)) {
            return false;
        }
        return true;
    }

}
