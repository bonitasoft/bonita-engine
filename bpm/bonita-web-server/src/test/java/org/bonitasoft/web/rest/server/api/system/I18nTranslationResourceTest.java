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
package org.bonitasoft.web.rest.server.api.system;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.bonitasoft.web.rest.server.utils.ResponseAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import net.javacrumbs.jsonunit.JsonAssert;
import net.javacrumbs.jsonunit.core.Option;
import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.web.rest.server.utils.RestletTest;
import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;

/**
 * @author Julien Mege
 */
@RunWith(MockitoJUnitRunner.class)
public class I18nTranslationResourceTest extends RestletTest {

    @Mock
    private I18n i18n;

    @Override
    protected ServerResource configureResource() {
        return new I18nTranslationResource(i18n);
    }

    @Test
    public void should_return_translation_for_the_given_local() throws Exception {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("key1", "<strong>message 1</strong>");
        parameters.put("key2", "autre méssage");
        parameters.put("key3", "~%^*µ");

        when(i18n.getLocale(AbstractI18n.LOCALE.fr)).thenReturn(parameters);

        Response response = request("/system/i18ntranslation?f=locale%3Dfr").get();

        assertThat(response).hasStatus(Status.SUCCESS_OK);

        assertJsonEquals("[" +
                "{\"key\": \"key1\", \"value\": \"<strong>message 1</strong>\"}," +
                "{\"key\": \"key2\", \"value\": \"autre méssage\"}," +
                "{\"key\": \"key3\", \"value\": \"~%^*µ\"}" +
                "]",
                response.getEntityAsText(),
                JsonAssert.when(Option.IGNORING_ARRAY_ORDER));
    }

    @Test
    public void should_return_http400_error_code_when_no_queryString() throws Exception {

        Response response = request("/system/i18ntranslation").get();

        assertThat(response).hasStatus(Status.CLIENT_ERROR_BAD_REQUEST);

    }

    @Test
    public void should_return_http400_error_code_when_no_locale_param() throws Exception {

        Response response = request("/system/i18ntranslation?f=test").get();

        assertThat(response).hasStatus(Status.CLIENT_ERROR_BAD_REQUEST);

    }

}
