/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.command.helper.designer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.GatewayDefinition;
import org.bonitasoft.engine.bpm.flownode.TransitionDefinition;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;

/**
 * @author Vincent Elcrin
 */
public class DesignerTestUtils {

    interface Stringifier<O> {

        String stringify(O object);
    }

    public static <O> String stringify(Collection<O> items, Stringifier<O> stringifier) {
        List<String> strings = new ArrayList<>(items.size());
        for (O item : items) {
            strings.add(stringifier.stringify(item));
        }
        Collections.sort(strings);
        return strings.toString();
    }

    public static String getGateways(final DesignProcessDefinition design) {
        return stringify(design.getFlowElementContainer().getGatewaysList(), new Stringifier<GatewayDefinition>() {

            @Override
            public String stringify(GatewayDefinition gateway) {
                String text = gateway.getName();
                if (gateway.getDefaultTransition() != null) {
                    text += " (" + getTransitionName(gateway.getDefaultTransition(), design) + ")";
                }
                return text;
            }
        });
    }

    public static String getActivities(final DesignProcessDefinition design) {
        return stringify(design.getFlowElementContainer().getActivities(), new Stringifier<ActivityDefinition>() {

            @Override
            public String stringify(ActivityDefinition activity) {
                String text = activity.getName();
                if (activity.getDefaultTransition() != null) {
                    text += " (" + getTransitionName(activity.getDefaultTransition(), design) + ")";
                }
                return text;
            }
        });
    }

    public static String getTransitions(final DesignProcessDefinition design) {
        return stringify(design.getFlowElementContainer().getTransitions(), new Stringifier<TransitionDefinition>() {

            @Override
            public String stringify(TransitionDefinition transition) {
                String text = getTransitionName(transition, design);
                if (transition.getCondition() != null) {
                    text += " (" + transition.getCondition().getName() + ")";
                }
                return text;
            }
        });
    }

    private static String getTransitionName(TransitionDefinition transition, DesignProcessDefinition design) {
        String text = design.getFlowElementContainer().getFlowNode(transition.getSource()).getName();
        text += "_->_";
        text += design.getFlowElementContainer().getFlowNode(transition.getTarget()).getName();
        return text;
    }

}
