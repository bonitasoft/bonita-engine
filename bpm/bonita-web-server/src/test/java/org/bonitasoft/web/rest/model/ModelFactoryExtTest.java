/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.web.rest.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.web.rest.model.application.ApplicationDefinition;
import org.bonitasoft.web.rest.model.applicationpage.ApplicationPageDefinition;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.junit.Test;

public class ModelFactoryExtTest {

    private final ModelFactory factory = new ModelFactory();

    @Test
    public void defineItemDefinitions_should_return_instanceOf_ApplicationDefinition_for_application_token()
            throws Exception {
        //when
        final ItemDefinition<?> definition = factory.defineItemDefinitions(ApplicationDefinition.TOKEN);

        //then
        assertThat(definition).isNotNull();
        assertThat(definition).isInstanceOf(ApplicationDefinition.class);
    }

    @Test
    public void defineItemDefinitions_should_return_instanceOf_ApplicationPageDefinition_for_applicationPage_token()
            throws Exception {
        //when
        final ItemDefinition<?> definition = factory.defineItemDefinitions(ApplicationPageDefinition.TOKEN);

        //then
        assertThat(definition).isNotNull();
        assertThat(definition).isInstanceOf(ApplicationPageDefinition.class);
    }

}
