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

import static org.mockito.Mockito.times;

import org.bonitasoft.web.rest.server.api.bdm.BusinessDataQueryResource;
import org.bonitasoft.web.rest.server.api.bdm.BusinessDataReferenceResource;
import org.bonitasoft.web.rest.server.api.bdm.BusinessDataReferencesResource;
import org.bonitasoft.web.rest.server.api.bdm.BusinessDataResource;
import org.bonitasoft.web.rest.server.api.system.I18nTranslationResource;
import org.bonitasoft.web.rest.server.utils.BonitaJacksonConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BonitaRestletApplicationTest {

    @Mock
    FinderFactory finderFactory;

    @Mock
    BonitaJacksonConverter bonitaJacksonConverter;

    @Test
    public void should_application_register_bdm_resources() throws Exception {
        //given
        final BonitaRestletApplication bonitaRestletApplication = new BonitaRestletApplication(finderFactory,
                bonitaJacksonConverter);

        //when
        bonitaRestletApplication.buildRouter();

        //then
        Mockito.verify(finderFactory).create(BusinessDataQueryResource.class);
        Mockito.verify(finderFactory).create(BusinessDataReferenceResource.class);
        Mockito.verify(finderFactory).create(BusinessDataReferencesResource.class);
        Mockito.verify(finderFactory, times(2)).create(BusinessDataResource.class);
    }

    @Test
    public void should_application_register_extension_resources() throws Exception {
        //given
        final BonitaRestletApplication bonitaRestletApplication = new BonitaRestletApplication(finderFactory,
                bonitaJacksonConverter);

        //when
        bonitaRestletApplication.buildRouter();

        //then
        Mockito.verify(finderFactory).createExtensionResource();
    }

    @Test
    public void application_should_register_i18n_resources() throws Exception {
        //given
        final BonitaRestletApplication bonitaRestletApplication = new BonitaRestletApplication(finderFactory,
                bonitaJacksonConverter);

        //when
        bonitaRestletApplication.buildRouter();

        //then
        Mockito.verify(finderFactory).create(I18nTranslationResource.class);
    }

}
