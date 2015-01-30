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
package org.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.data.impl.TextDataDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ActivityDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.CatchMessageEventTriggerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Feng Hui
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class TextDataDefinitionBuilder extends DataDefinitionBuilder {

    public TextDataDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl process,
            final String name, final String className, final Expression defaultValue) {
        super(processDefinitionBuilder, process, getTextData(name, className, defaultValue));
        processDefinitionBuilder.checkExpression(toString(), defaultValue);
    }

    public TextDataDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl process,
            final ActivityDefinitionImpl activity, final String name, final String className, final Expression defaultValue) {
        super(processDefinitionBuilder, process, activity, getTextData(name, className, defaultValue));
        processDefinitionBuilder.checkExpression(toString(), defaultValue);
    }

    public TextDataDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final CatchMessageEventTriggerDefinitionImpl messageEventTrigger, final String name, final String className, final Expression defaultValue) {
        super(processDefinitionBuilder, container, messageEventTrigger, getTextData(name, className, defaultValue));
        processDefinitionBuilder.checkExpression(toString(), defaultValue);
    }

    private static TextDataDefinitionImpl getTextData(final String name, final String className, final Expression defaultValue) {
        final TextDataDefinitionImpl text = new TextDataDefinitionImpl(name, defaultValue);
        text.setClassName(className);
        return text;
    }

    /**
     * Sets this data as long text (more than 255 characters).
     * @return
     */
    public TextDataDefinitionBuilder isLongText() {
        ((TextDataDefinitionImpl) getDataDefinition()).setLongText(true);
        return this;
    }

}
