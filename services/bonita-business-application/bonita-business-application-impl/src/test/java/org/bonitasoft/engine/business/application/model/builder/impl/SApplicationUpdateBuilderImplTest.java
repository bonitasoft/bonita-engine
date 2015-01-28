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
package org.bonitasoft.engine.business.application.model.builder.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationFields;
import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationUpdateBuilderImpl;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;

public class SApplicationUpdateBuilderImplTest {

    @Test
    public void constructorShoulUpdateUpdatedByAndLastUpdateDateFields() throws Exception {
        final EntityUpdateDescriptor desc = new SApplicationUpdateBuilderImpl(17L).done();
        final Map<String, Object> fields = desc.getFields();
        assertThat(fields.size()).isEqualTo(2);
        assertThat(fields.get(SApplicationFields.UPDATED_BY)).isEqualTo(17L);
        assertThat(fields.get(SApplicationFields.LAST_UPDATE_DATE)).isNotNull();
    }
}
