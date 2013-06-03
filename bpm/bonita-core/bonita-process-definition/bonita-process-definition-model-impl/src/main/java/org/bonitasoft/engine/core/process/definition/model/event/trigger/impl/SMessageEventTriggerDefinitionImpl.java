/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.definition.model.event.trigger.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.data.TextDataDefinition;
import org.bonitasoft.engine.bpm.data.XMLDataDefinition;
import org.bonitasoft.engine.bpm.flownode.CorrelationDefinition;
import org.bonitasoft.engine.bpm.flownode.MessageEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.ServerModelConvertor;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SCatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SCorrelationDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerType;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SMessageEventTriggerDefinition;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilder;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilders;
import org.bonitasoft.engine.data.definition.model.builder.SXMLDataDefinitionBuilder;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class SMessageEventTriggerDefinitionImpl extends SEventTriggerDefinitionImpl implements SMessageEventTriggerDefinition {

    private static final long serialVersionUID = 4603391860834299674L;

    private String messageName;

    private final List<SCorrelationDefinition> correlations;

    public SMessageEventTriggerDefinitionImpl(final String name, final List<SCorrelationDefinition> correlations) {
        messageName = name;
        this.correlations = correlations;
    }

    public SMessageEventTriggerDefinitionImpl() {
        correlations = new ArrayList<SCorrelationDefinition>(1);
    }

    public SMessageEventTriggerDefinitionImpl(SCatchMessageEventTriggerDefinition trigger) {
        messageName = trigger.getMessageName();
        this.correlations = trigger.getCorrelations();
    }

    public SMessageEventTriggerDefinitionImpl(final MessageEventTriggerDefinition messageEventTrigger, final SDataDefinitionBuilders sDataDefinitionBuilder,
            final SExpressionBuilders sExpressionBuilders) {
        messageName = messageEventTrigger.getMessageName();
        correlations = new ArrayList<SCorrelationDefinition>(messageEventTrigger.getCorrelations().size());
        for (final CorrelationDefinition correlation : messageEventTrigger.getCorrelations()) {
            correlations.add(new SCorrelationDefinitionImpl(ServerModelConvertor.convertExpression(sExpressionBuilders, correlation.getKey()),
                    ServerModelConvertor.convertExpression(sExpressionBuilders, correlation.getValue())));
        }
    }

    @Override
    public String getMessageName() {
        return messageName;
    }

    @Override
    public List<SCorrelationDefinition> getCorrelations() {
        return Collections.unmodifiableList(correlations);
    }

    public void setMessageName(final String name) {
        messageName = name;
    }

    public void addCorrelation(final SCorrelationDefinition correlation) {
        correlations.add(correlation);
    }

    protected SDataDefinition buildSDataDefinition(final DataDefinition dataDefinition, final SDataDefinitionBuilders dataDefinitionBuilders,
            final SExpressionBuilders sExpressionBuilders) {
        if (isXMLDataDefinition(dataDefinition)) {
            final XMLDataDefinition xmlDataDef = (XMLDataDefinition) dataDefinition;
            final SXMLDataDefinitionBuilder xmlDataDefinitionBuilder = dataDefinitionBuilders.getXMLDataDefinitionBuilder();
            xmlDataDefinitionBuilder.createNewXMLData(messageName).setElement(xmlDataDef.getElement()).setNamespace(xmlDataDef.getNamespace());
            xmlDataDefinitionBuilder.setDefaultValue(ServerModelConvertor.convertExpression(sExpressionBuilders, dataDefinition.getDefaultValueExpression()));
            xmlDataDefinitionBuilder.setDescription(dataDefinition.getDescription());
            xmlDataDefinitionBuilder.setTransient(dataDefinition.isTransientData());
            return xmlDataDefinitionBuilder.done();
        } else {
            final SDataDefinitionBuilder dataDefinitionBuilder = dataDefinitionBuilders.getDataDefinitionBuilder();
            if (isTextDataDefinition(dataDefinition)) {
                final TextDataDefinition textDataDefinition = (TextDataDefinition) dataDefinition;
                dataDefinitionBuilder.createNewTextData(dataDefinition.getName()).setAsLongText(textDataDefinition.isLongText());
            } else {
                dataDefinitionBuilder.createNewInstance(dataDefinition.getName(), dataDefinition.getClassName());
            }
            dataDefinitionBuilder.setDefaultValue(ServerModelConvertor.convertExpression(sExpressionBuilders, dataDefinition.getDefaultValueExpression()));
            dataDefinitionBuilder.setDescription(dataDefinition.getDescription());
            dataDefinitionBuilder.setTransient(dataDefinition.isTransientData());
            return dataDefinitionBuilder.done();
        }
    }

    private boolean isXMLDataDefinition(final DataDefinition dataDefinition) {
        return dataDefinition instanceof XMLDataDefinition;
    }

    private boolean isTextDataDefinition(final DataDefinition dataDefinition) {
        return dataDefinition instanceof TextDataDefinition;
    }

    @Override
    public SEventTriggerType getEventTriggerType() {
        return SEventTriggerType.MESSAGE;
    }

}
