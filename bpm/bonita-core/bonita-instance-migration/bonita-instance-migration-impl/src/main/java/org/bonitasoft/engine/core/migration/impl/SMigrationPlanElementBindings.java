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
package org.bonitasoft.engine.core.migration.impl;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.core.migration.model.impl.xml.SConnectorDefinitionBinding;
import org.bonitasoft.engine.core.migration.model.impl.xml.SConnectorDefinitionInputBinding;
import org.bonitasoft.engine.core.migration.model.impl.xml.SEnablementExpressionBinding;
import org.bonitasoft.engine.core.migration.model.impl.xml.SExpressionBinding;
import org.bonitasoft.engine.core.migration.model.impl.xml.SLeftOperandBinding;
import org.bonitasoft.engine.core.migration.model.impl.xml.SMappingOperationBinding;
import org.bonitasoft.engine.core.migration.model.impl.xml.SMigrationMappingBinding;
import org.bonitasoft.engine.core.migration.model.impl.xml.SMigrationPlanBinding;
import org.bonitasoft.engine.core.migration.model.impl.xml.SOperationBinding;
import org.bonitasoft.engine.core.migration.model.impl.xml.SRightOperandBinding;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilders;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;
import org.bonitasoft.engine.xml.ElementBinding;
import org.bonitasoft.engine.xml.ElementBindingsFactory;

/**
 * @author Baptiste Mesta
 */
final class SMigrationPlanElementBindings implements ElementBindingsFactory {

    private final List<ElementBinding> list;

    private final SExpressionBuilders expressionBuilders;

    private final SOperationBuilders sOperationBuilders;

    public SMigrationPlanElementBindings(final SExpressionBuilders expressionBuilders, final SOperationBuilders sOperationBuilders) {
        this.expressionBuilders = expressionBuilders;
        this.sOperationBuilders = sOperationBuilders;
        list = new ArrayList<ElementBinding>();
        list.add(new SConnectorDefinitionBinding());
        list.add(new SConnectorDefinitionInputBinding());
        list.add(new SEnablementExpressionBinding(expressionBuilders));
        list.add(new SExpressionBinding(expressionBuilders));
        list.add(new SLeftOperandBinding(sOperationBuilders));
        list.add(new SMigrationMappingBinding());
        list.add(new SMigrationPlanBinding());
        list.add(new SMappingOperationBinding(sOperationBuilders));
        list.add(new SRightOperandBinding(expressionBuilders));
        list.add(new SOperationBinding(sOperationBuilders));
    }

    @Override
    public List<ElementBinding> getElementBindings() {
        return list;
    }

    @Override
    public ElementBinding createNewInstance(final Class<? extends ElementBinding> binderClass) {
        if (SConnectorDefinitionBinding.class.equals(binderClass)) {
            return new SConnectorDefinitionBinding();
        }
        if (SConnectorDefinitionInputBinding.class.equals(binderClass)) {
            return new SConnectorDefinitionInputBinding();
        }
        if (SEnablementExpressionBinding.class.equals(binderClass)) {
            return new SEnablementExpressionBinding(expressionBuilders);
        }
        if (SExpressionBinding.class.equals(binderClass)) {
            return new SExpressionBinding(expressionBuilders);
        }
        if (SLeftOperandBinding.class.equals(binderClass)) {
            return new SLeftOperandBinding(sOperationBuilders);
        }
        if (SMigrationMappingBinding.class.equals(binderClass)) {
            return new SMigrationMappingBinding();
        }
        if (SMigrationPlanBinding.class.equals(binderClass)) {
            return new SMigrationPlanBinding();
        }
        if (SOperationBinding.class.equals(binderClass)) {
            return new SOperationBinding(sOperationBuilders);
        }
        if (SMappingOperationBinding.class.equals(binderClass)) {
            return new SMappingOperationBinding(sOperationBuilders);
        }
        if (SRightOperandBinding.class.equals(binderClass)) {
            return new SRightOperandBinding(expressionBuilders);
        }
        return null;
    }
}
