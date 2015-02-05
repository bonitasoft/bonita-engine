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
import org.bonitasoft.engine.xml.ElementBinding;
import org.bonitasoft.engine.xml.ElementBindingsFactory;

/**
 * @author Baptiste Mesta
 */
final class SMigrationPlanElementBindings implements ElementBindingsFactory {

    private final List<ElementBinding> list;

    public SMigrationPlanElementBindings() {
        list = new ArrayList<ElementBinding>();
        list.add(new SConnectorDefinitionBinding());
        list.add(new SConnectorDefinitionInputBinding());
        list.add(new SEnablementExpressionBinding());
        list.add(new SExpressionBinding());
        list.add(new SLeftOperandBinding());
        list.add(new SMigrationMappingBinding());
        list.add(new SMigrationPlanBinding());
        list.add(new SMappingOperationBinding());
        list.add(new SRightOperandBinding());
        list.add(new SOperationBinding());
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
            return new SEnablementExpressionBinding();
        }
        if (SExpressionBinding.class.equals(binderClass)) {
            return new SExpressionBinding();
        }
        if (SLeftOperandBinding.class.equals(binderClass)) {
            return new SLeftOperandBinding();
        }
        if (SMigrationMappingBinding.class.equals(binderClass)) {
            return new SMigrationMappingBinding();
        }
        if (SMigrationPlanBinding.class.equals(binderClass)) {
            return new SMigrationPlanBinding();
        }
        if (SOperationBinding.class.equals(binderClass)) {
            return new SOperationBinding();
        }
        if (SMappingOperationBinding.class.equals(binderClass)) {
            return new SMappingOperationBinding();
        }
        if (SRightOperandBinding.class.equals(binderClass)) {
            return new SRightOperandBinding();
        }
        return null;
    }
}
