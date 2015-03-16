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
package org.bonitasoft.engine.core.expression.control.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Zhao Na
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SExpressionContext implements Serializable {

    private static final long serialVersionUID = 6417383664862870145L;

    public static final String CONTAINER_ID_KEY = "containerId";

    public static final String CONTAINER_TYPE_KEY = "containerType";

    public static final String TIME_KEY = "time";

    public static final String PROCESS_DEFINITION_ID_KEY = "processDefinitionId";

    public static final String PROCESS_DEFINITION_KEY = "processDefinition";

    private Long containerId;

    private String containerType;

    private ContainerState containerState;

    private long time;

    private Long processDefinitionId;

    private Long parentProcessDefinitionId;

    private SProcessDefinition processDefinition;

    private Map<String, Object> inputValues;

    private boolean evaluateInDefinition = false;

    private Map<String, SExpression> dataMap;

    private Map<SExpression, String> invertedDataMap;

    public SExpressionContext() {
        inputValues = new HashMap<String, Object>();
    }

    public SExpressionContext(final Long containerId, final String containerType, final Long processDefinitionId) {
        this(containerId, containerType, (Map<String, Object>) null);
        this.processDefinitionId = processDefinitionId;
    }

    public SExpressionContext(final long containerId, final String containerType, final Map<String, Object> inputValues) {
        this.containerId = containerId;
        this.containerType = containerType;
        if (inputValues == null) {
            this.inputValues = new HashMap<String, Object>();
        } else {
            this.inputValues = new HashMap<String, Object>(inputValues);
        }
    }

    public Long getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(final Long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(final long time) {
        this.time = time;
    }

    public Long getContainerId() {
        return containerId;
    }

    public void setContainerId(final Long containerId) {
        this.containerId = containerId;
    }

    public String getContainerType() {
        return containerType;
    }

    public void setContainerType(final String containerType) {
        this.containerType = containerType;
    }

    public ContainerState getContainerState() {
        return containerState;
    }

    public void setContainerState(final ContainerState containerState) {
        this.containerState = containerState;
    }

    public Map<String, Object> getInputValues() {
        return inputValues;
    }

    public SProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public void setProcessDefinition(final SProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
        final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
        final List<SDataDefinition> dataDefinitions = processContainer.getDataDefinitions();
        dataMap = new HashMap<String, SExpression>(dataDefinitions.size());
        invertedDataMap = new HashMap<SExpression, String>(dataDefinitions.size());
        for (final SDataDefinition dataDef : dataDefinitions) {
            dataMap.put(dataDef.getName(), dataDef.getDefaultValueExpression());
            invertedDataMap.put(dataDef.getDefaultValueExpression(), dataDef.getName());
        }
    }

    public SExpression getDefaultValueFor(final String name) {
        if (evaluateInDefinition) {
            return dataMap.get(name);
        }
        return null;
    }

    public String isDefaultValueOf(final SExpression exp) {
        if (evaluateInDefinition) {
            return invertedDataMap.get(exp);
        }
        return null;
    }

    public void setInputValues(final Map<String, Object> inputValues) {
        if (inputValues == null) {
            this.inputValues = new HashMap<String, Object>();
        } else {
            this.inputValues = new HashMap<String, Object>(inputValues);
        }
    }

    public void putAllInputValues(final Map<String, Object> inputValues) {
        if (inputValues != null) {
            this.inputValues.putAll(inputValues);
        }
    }

    public void setSerializableInputValues(final Map<String, Serializable> inputValues) {
        if (inputValues != null) {
            this.inputValues.putAll(inputValues);
        }
    }

    public void setEvaluateInDefinition(final boolean evaluateInDefinition) {
        this.evaluateInDefinition = evaluateInDefinition;
    }

    public boolean isEvaluateInDefinition() {
        return evaluateInDefinition;
    }

    @Override
    public String toString() {
        return "context [containerId=" + containerId + ", containerType=" + containerType + ", processDefinitionId="
                + processDefinitionId
                + (processDefinition != null ? ", processDefinition=" + processDefinition.getName() + " -- " + processDefinition.getVersion() : "") + "]";
    }

    public Long getParentProcessDefinitionId() {
        return parentProcessDefinitionId;
    }

    public void setParentProcessDefinitionId(final Long parentProcessDefinitionId) {
        this.parentProcessDefinitionId = parentProcessDefinitionId;
    }


}
