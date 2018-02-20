/**
 * Copyright (C) 2016 BonitaSoft S.A.
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
package org.bonitasoft.engine.bdm.validator.rule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.bdm.builder.IndexBuilder.anIndex;

import org.bonitasoft.engine.bdm.model.Index;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;
import org.junit.Test;

public class IndexValidationRuleTest {

    @Test
    public void should_add_an_error_message_when_index_fields_are_null() throws Exception {
        final Index index = anIndex().withName("nameIndex").build();

        ValidationStatus status = new IndexValidationRule().checkRule(index);

        assertThat(status.getErrors()).contains("nameIndex index must have at least one field declared");
    }

}
