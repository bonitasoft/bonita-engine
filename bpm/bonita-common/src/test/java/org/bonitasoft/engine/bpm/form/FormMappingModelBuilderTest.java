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

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.bpm.bar.form.model.FormMappingDefinition;
import org.bonitasoft.engine.bpm.bar.form.model.FormMappingModel;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.form.FormMappingType;
import org.junit.Test;


public class FormMappingModelBuilderTest {

    @Test
    public void testWithFormMapping() throws Exception {
        FormMappingDefinition form1 = FormMappingDefinitionBuilder.buildFormMapping("form", FormMappingType.TASK, FormMappingTarget.INTERNAL).withTaskname("task").build();
        FormMappingDefinition form2 = FormMappingDefinitionBuilder.buildFormMapping("form", FormMappingType.PROCESS_OVERVIEW, FormMappingTarget.INTERNAL).build();

        FormMappingModel formMappingModel = FormMappingModelBuilder.buildFormMappingModel().withFormMapping(form1).withFormMapping(form2).build();

        assertThat(formMappingModel.getFormMappings()).containsExactly(form1,form2);
    }

    @Test
    public void testAddProcessStartForm() throws Exception {
        FormMappingDefinition form = FormMappingDefinitionBuilder.buildFormMapping("form", FormMappingType.PROCESS_START, FormMappingTarget.INTERNAL).build();

        FormMappingModel formMappingModel = FormMappingModelBuilder.buildFormMappingModel().addProcessStartForm("form", FormMappingTarget.INTERNAL).build();

        assertThat(formMappingModel.getFormMappings()).containsExactly(form);
    }

    @Test
    public void testAddProcessOverviewForm() throws Exception {
        FormMappingDefinition form = FormMappingDefinitionBuilder.buildFormMapping("form", FormMappingType.PROCESS_OVERVIEW, FormMappingTarget.INTERNAL).build();

        FormMappingModel formMappingModel = FormMappingModelBuilder.buildFormMappingModel().addProcessOverviewForm("form",FormMappingTarget.INTERNAL).build();

        assertThat(formMappingModel.getFormMappings()).containsExactly(form);

    }

    @Test
    public void testAddTaskForm() throws Exception {
        FormMappingDefinition form = FormMappingDefinitionBuilder.buildFormMapping("form", FormMappingType.TASK, FormMappingTarget.INTERNAL).withTaskname("step1").build();

        FormMappingModel formMappingModel = FormMappingModelBuilder.buildFormMappingModel().addTaskForm("form",FormMappingTarget.INTERNAL,"step1").build();

        assertThat(formMappingModel.getFormMappings()).containsExactly(form);

    }
}