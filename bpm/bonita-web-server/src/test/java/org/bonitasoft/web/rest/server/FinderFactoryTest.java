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
package org.bonitasoft.web.rest.server;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.restlet.resource.ServerResource;

@RunWith(MockitoJUnitRunner.class)
public class FinderFactoryTest {

    private FinderFactory factory;

    @Before
    public void setUp() {
        factory = new FinderFactory();
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_RuntimeException_for_a_not_supported_class() {

        factory.create(NotSupportedResource.class);
    }

    private class NotSupportedResource extends ServerResource {

    }

}
