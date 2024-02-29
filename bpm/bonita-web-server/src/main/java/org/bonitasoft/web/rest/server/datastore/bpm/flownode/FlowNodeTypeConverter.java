/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.web.rest.server.datastore.bpm.flownode;

import org.bonitasoft.engine.bpm.flownode.FlowNodeType;
import org.bonitasoft.web.rest.model.bpm.flownode.IFlowNodeItem;
import org.bonitasoft.web.rest.server.datastore.converter.ValueConverter;
import org.bonitasoft.web.rest.server.framework.api.EnumConverter;

/**
 * @author Vincent Elcrin
 */
public class FlowNodeTypeConverter implements EnumConverter<FlowNodeType>, ValueConverter<FlowNodeType> {

    @Override
    public FlowNodeType convert(final String attributeValue) {
        if (IFlowNodeItem.VALUE_TYPE_AUTOMATIC_TASK.equals(attributeValue)) {
            return FlowNodeType.AUTOMATIC_TASK;
        } else if (IFlowNodeItem.VALUE_TYPE_BOUNDARY_EVENT.equals(attributeValue)) {
            return FlowNodeType.BOUNDARY_EVENT;
        } else if (IFlowNodeItem.VALUE_TYPE_CALL_ACTIVITY.equals(attributeValue)) {
            return FlowNodeType.CALL_ACTIVITY;
        } else if (IFlowNodeItem.VALUE_TYPE_END_EVENT.equals(attributeValue)) {
            return FlowNodeType.END_EVENT;
        } else if (IFlowNodeItem.VALUE_TYPE_GATEWAY.equals(attributeValue)) {
            return FlowNodeType.GATEWAY;
        } else if (IFlowNodeItem.VALUE_TYPE_INTERMEDIATE_CATCH_EVENT.equals(attributeValue)) {
            return FlowNodeType.INTERMEDIATE_CATCH_EVENT;
        } else if (IFlowNodeItem.VALUE_TYPE_INTERMEDIATE_THROW_EVENT.equals(attributeValue)) {
            return FlowNodeType.INTERMEDIATE_THROW_EVENT;
        } else if (IFlowNodeItem.VALUE_TYPE_LOOP_ACTIVITY.equals(attributeValue)) {
            return FlowNodeType.LOOP_ACTIVITY;
        } else if (IFlowNodeItem.VALUE_TYPE_MANUAL_TASK.equals(attributeValue)) {
            return FlowNodeType.MANUAL_TASK;
        } else if (IFlowNodeItem.VALUE_TYPE_START_EVENT.equals(attributeValue)) {
            return FlowNodeType.START_EVENT;
        } else if (IFlowNodeItem.VALUE_TYPE_USER_TASK.equals(attributeValue)) {
            return FlowNodeType.USER_TASK;
        } else if (IFlowNodeItem.VALUE_TYPE_SUB_PROCESS_ACTIVITY.equals(attributeValue)) {
            return FlowNodeType.SUB_PROCESS;
        } /*
           * else if (IFlowNodeItem.VALUE_TYPE_MULTI_INSTANCE_ACTIVITY.equals(attributeValue)) {
           * return FlowNodeType.MULTI_INSTANCE;
           * }
           */else {
            throw new RuntimeException("Can't convert <" + attributeValue + ">. Unknown flow node type.");
        }
    }

    @Override
    public String convert(final FlowNodeType enumValue) {
        switch (enumValue) {
            case AUTOMATIC_TASK:
                return IFlowNodeItem.VALUE_TYPE_AUTOMATIC_TASK;
            case BOUNDARY_EVENT:
                return IFlowNodeItem.VALUE_TYPE_BOUNDARY_EVENT;
            case CALL_ACTIVITY:
                return IFlowNodeItem.VALUE_TYPE_CALL_ACTIVITY;
            case END_EVENT:
                return IFlowNodeItem.VALUE_TYPE_END_EVENT;
            case GATEWAY:
                return IFlowNodeItem.VALUE_TYPE_GATEWAY;
            case INTERMEDIATE_CATCH_EVENT:
                return IFlowNodeItem.VALUE_TYPE_INTERMEDIATE_CATCH_EVENT;
            case INTERMEDIATE_THROW_EVENT:
                return IFlowNodeItem.VALUE_TYPE_INTERMEDIATE_THROW_EVENT;
            case LOOP_ACTIVITY:
                return IFlowNodeItem.VALUE_TYPE_LOOP_ACTIVITY;
            case MANUAL_TASK:
                return IFlowNodeItem.VALUE_TYPE_MANUAL_TASK;
            case START_EVENT:
                return IFlowNodeItem.VALUE_TYPE_START_EVENT;
            case USER_TASK:
                return IFlowNodeItem.VALUE_TYPE_USER_TASK;
            case SUB_PROCESS:
                return IFlowNodeItem.VALUE_TYPE_SUB_PROCESS_ACTIVITY;
            /*
             * case MULTI_INSTANCE:
             * return IFlowNodeItem.VALUE_TYPE_MULTI_INSTANCE_ACTIVITY;
             */
            default:
                throw new RuntimeException("Can't convert <" + enumValue + ">. Flow node type not supported.");
        }
    }
}
