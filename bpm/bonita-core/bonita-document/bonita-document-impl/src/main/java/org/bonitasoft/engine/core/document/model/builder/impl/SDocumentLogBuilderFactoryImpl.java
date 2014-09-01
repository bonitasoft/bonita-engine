/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.core.document.model.builder.impl;

import org.bonitasoft.engine.core.document.model.builder.SDocumentLogBuilder;
import org.bonitasoft.engine.core.document.model.builder.SDocumentLogBuilderFactory;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilderFactory;

/**
 * @author Emmanuel Duchastenier
 * @author Nicolas Chabanoles
 */
public class SDocumentLogBuilderFactoryImpl extends CRUDELogBuilderFactory implements SDocumentLogBuilderFactory {

    @Override
    public SDocumentLogBuilder createNewInstance() {
        return new SDocumentLogBuilderImpl();
    }
    
    @Override
    public String getObjectIdKey() {
        return SDocumentMappingLogIndexesMapper.DOCUMENTMAPPING_INDEX_NAME;
    }

    @Override
    public String getProcessInstanceIdKey() {
        return SDocumentMappingLogIndexesMapper.DOCUMENTMAPPING_INDEX_PROC_INSTANCE_ID_NAME;
    }

}
