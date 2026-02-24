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

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.web.rest.server.utils.BonitaJacksonConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.restlet.routing.Router;

@ExtendWith(MockitoExtension.class)
class BonitaRestletApplicationTest {

    @Mock
    FinderFactory finderFactory;

    @Mock
    BonitaJacksonConverter bonitaJacksonConverter;

    @Test
    void should_build_router_with_no_routes() {
        //given
        var bonitaRestletApplication = new BonitaRestletApplication(finderFactory, bonitaJacksonConverter);

        //when
        Router router = bonitaRestletApplication.buildRouter();

        //then
        assertThat(router.getRoutes()).isEmpty();
    }

}
