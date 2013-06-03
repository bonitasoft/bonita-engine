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
package org.bonitasoft.engine.core.process.document.mapping.model.builder.impl;

import org.bonitasoft.engine.core.process.document.mapping.model.archive.builder.SADocumentMappingBuilder;
import org.bonitasoft.engine.core.process.document.mapping.model.archive.builder.impl.SADocumentMappingBuilderImpl;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilder;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilderAccessor;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingLogBuilder;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingUpdateBuilder;

/**
 * @author Emmanuel Duchastenier
 * @author Nicolas Chabanoles
 */
public class SDocumentMappingBuilderAccessorImpl implements SDocumentMappingBuilderAccessor {

    @Override
    public SDocumentMappingBuilder getSDocumentMappingBuilder() {
        return new SDocumentMappingBuilderImpl();
    }

    @Override
    public SDocumentMappingLogBuilder getSDocumentMappingLogBuilder() {
        return new SDocumentMappingLogBuilderImpl();
    }

    @Override
    public SADocumentMappingBuilder getSADocumentMappingBuilder() {
        return new SADocumentMappingBuilderImpl();
    }

    @Override
    public SDocumentMappingUpdateBuilder getSDocumentMappingUpdateBuilder() {
        return new SDocumentMappingUpdateBuilderImpl();
    }
}
