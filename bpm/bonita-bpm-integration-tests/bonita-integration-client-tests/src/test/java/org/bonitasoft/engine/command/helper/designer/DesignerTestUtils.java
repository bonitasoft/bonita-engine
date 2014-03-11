/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
        List<String> strings = new ArrayList<String>(items.size());
        for (O item : items) {
            strings.add(stringifier.stringify(item));
        }
        Collections.sort(strings);
        return strings.toString();
    }

    public static String getGateways(DesignProcessDefinition design) {
        return stringify(design.getProcessContainer().getGatewaysList(), new Stringifier<GatewayDefinition>() {
            @Override
            public String stringify(GatewayDefinition gateway) {
                String text = gateway.getName();
                if (gateway.getDefaultTransition() != null) {
                    text += " (" + gateway.getDefaultTransition().getName() + ")";
                }
                return text;
            }
        });
    }

    public static String getActivities(DesignProcessDefinition design) {
        return stringify(design.getProcessContainer().getActivities(), new Stringifier<ActivityDefinition>() {
            @Override
            public String stringify(ActivityDefinition activity) {
                String text = activity.getName();
                if (activity.getDefaultTransition() != null) {
                    text += " (" + activity.getDefaultTransition().getName() + ")";
                }
                return text;
            }
        });
    }

    public static String getTransactions(DesignProcessDefinition design) {
        return stringify(design.getProcessContainer().getTransitions(), new Stringifier<TransitionDefinition>() {
            @Override
            public String stringify(TransitionDefinition transition) {
                String text = transition.getName();
                if (transition.getCondition() != null) {
                    text += " (" + transition.getCondition().getName() + ")";
                }
                return text;
            }
        });
    }


}
