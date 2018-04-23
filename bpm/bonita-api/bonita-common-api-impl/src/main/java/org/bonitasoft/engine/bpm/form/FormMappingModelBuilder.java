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
package org.bonitasoft.engine.bpm.form;

import org.bonitasoft.engine.bpm.bar.form.model.FormMappingDefinition;
import org.bonitasoft.engine.bpm.bar.form.model.FormMappingModel;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.form.FormMappingType;

/**
 * @author Emmanuel Duchastenier
 */
public class FormMappingModelBuilder {

    private final FormMappingModel formMappingModel = new FormMappingModel();

    public static FormMappingModelBuilder buildFormMappingModel() {
        return new FormMappingModelBuilder();
    }

    public FormMappingModelBuilder withFormMapping(final FormMappingDefinition mapping) {
        formMappingModel.addFormMapping(mapping);
        return this;
    }

    public FormMappingModel build() {
        return formMappingModel;
    }

    public FormMappingModelBuilder addProcessStartForm(String form, FormMappingTarget target) {
        return withFormMapping(FormMappingDefinitionBuilder.buildFormMapping(form, FormMappingType.PROCESS_START,target).build());
    }

    public FormMappingModelBuilder addProcessOverviewForm(String form, FormMappingTarget target) {
        return withFormMapping(FormMappingDefinitionBuilder.buildFormMapping(form, FormMappingType.PROCESS_OVERVIEW,target).build());
    }

    public FormMappingModelBuilder addTaskForm(String form, FormMappingTarget target, String task) {
        return withFormMapping(FormMappingDefinitionBuilder.buildFormMapping(form, FormMappingType.TASK,target).withTaskname(task).build());
    }
}
