/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.engine.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.commons.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * @author Baptiste Mesta
 */
@ExtendWith(MockitoExtension.class)
class ServicesResolverTest {

    @Mock
    private ServicesLookup servicesLookup;
    @InjectMocks
    private ServicesResolver servicesResolver;

    private final class BeanThatNeedMyService {

        private static final long serialVersionUID = 1L;

        private Object myService;

        @InjectedService
        public void setMyService(final Object myService) {
            this.myService = myService;
        }

        Object getMyService() {
            return myService;
        }

        public String getName() {
            return null;
        }

        public String getDescription() {
            return null;
        }

        public void execute() {
        }

        public void setAttributes(final Map<String, Serializable> attributes) {
        }
    }

    @Test
    void should_injectService_inject_setter_having_the_annotation() throws Exception {
        final BeanThatNeedMyService beanThatNeedMyService = new BeanThatNeedMyService();

        final Object myService = new Object();
        when(servicesLookup.lookupOnTenant(123L, "myService")).thenReturn(myService);

        servicesResolver.injectServices(123L, beanThatNeedMyService);

        assertEquals(myService, beanThatNeedMyService.getMyService());

    }
}
