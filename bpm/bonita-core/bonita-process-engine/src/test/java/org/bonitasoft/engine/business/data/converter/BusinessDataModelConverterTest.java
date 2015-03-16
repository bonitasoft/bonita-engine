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
package org.bonitasoft.engine.business.data.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.bonitasoft.engine.business.data.MultipleBusinessDataReference;
import org.bonitasoft.engine.business.data.SimpleBusinessDataReference;
import org.bonitasoft.engine.core.process.instance.model.impl.business.data.SProcessMultiRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.business.data.SProcessSimpleRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.business.data.SSimpleRefBusinessDataInstanceImpl;
import org.junit.Test;

public class BusinessDataModelConverterTest {

    @Test
    public void convertSSimpleBusinessDataReferencetoClientObject() throws Exception {
        final SSimpleRefBusinessDataInstanceImpl sReference = new SProcessSimpleRefBusinessDataInstanceImpl();
        sReference.setName("employee");
        sReference.setDataClassName("com.bonitasoft.Employee");
        sReference.setId(465L);
        sReference.setDataId(87997L);

        final SimpleBusinessDataReference reference = BusinessDataModelConverter.toSimpleBusinessDataReference(sReference);
        assertThat(reference.getStorageId()).isEqualTo(87997L);
        assertThat(reference.getName()).isEqualTo("employee");
        assertThat(reference.getType()).isEqualTo("com.bonitasoft.Employee");
    }

    @Test
    public void convertSMultiBusinessDataReferencetoClientObject() throws Exception {
        final SProcessMultiRefBusinessDataInstanceImpl sReference = new SProcessMultiRefBusinessDataInstanceImpl();
        sReference.setName("employees");
        sReference.setDataClassName("com.bonitasoft.Employee");
        sReference.setId(465L);
        sReference.setDataIds(Arrays.asList(87997L, 654312354L, 4786454L));

        final MultipleBusinessDataReference reference = BusinessDataModelConverter.toMultipleBusinessDataReference(sReference);
        assertThat(reference.getStorageIds()).isEqualTo(Arrays.asList(87997L, 654312354L, 4786454L));
        assertThat(reference.getName()).isEqualTo("employees");
        assertThat(reference.getType()).isEqualTo("com.bonitasoft.Employee");
    }

}