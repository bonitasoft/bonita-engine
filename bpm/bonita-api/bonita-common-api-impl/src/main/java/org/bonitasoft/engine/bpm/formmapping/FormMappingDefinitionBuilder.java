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
package org.bonitasoft.engine.bpm.formmapping;

import org.bonitasoft.engine.bpm.bar.formmapping.model.FormMappingDefinition;
import org.bonitasoft.engine.form.mapping.FormMappingType;

public class FormMappingDefinitionBuilder {

    private final FormMappingDefinition formMapping;

    public FormMappingDefinitionBuilder(final String page, final FormMappingType type, final boolean external) {
        formMapping = new FormMappingDefinition(page, type, external);
    }

    public static FormMappingDefinitionBuilder buildFormMapping(final String page, final FormMappingType type, final boolean external) {
        return new FormMappingDefinitionBuilder(page, type, external);
    }

    public FormMappingDefinitionBuilder withTaskname(final String taskname) {
        formMapping.setTaskname(taskname);
        return this;
    }

    public FormMappingDefinition build() {
        return formMapping;
    }

}
