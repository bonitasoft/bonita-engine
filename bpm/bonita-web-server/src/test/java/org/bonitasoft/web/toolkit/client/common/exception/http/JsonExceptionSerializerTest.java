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
package org.bonitasoft.web.toolkit.client.common.exception.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.bonitasoft.console.common.FakeI18n;
import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n.LOCALE;
import org.bonitasoft.web.toolkit.client.common.i18n.T_;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Vincent Elcrin
 * Date: 23/09/13
 * Time: 18:12
 */
public class JsonExceptionSerializerTest {

    private FakeI18n fakeI18n;

    @Before
    public void setUp() throws Exception {
        fakeI18n = new FakeI18n();
    }

    @After
    public void cleanUp() throws Exception {
        I18n.setInstance(null);
    }

    @Test
    public void testWeCanConvertExceptionWithoutMessage() throws Exception {
        HttpException exception = new HttpException();
        JsonExceptionSerializer serializer = new JsonExceptionSerializer(exception);

        String json = serializer.end();

        assertEquals(exception, json);
    }

    @Test
    public void testWeCanConvertExceptionWithMessageToJson() throws Exception {
        HttpException exception = new HttpException("message");
        JsonExceptionSerializer serializer = new JsonExceptionSerializer(exception);

        String json = serializer.end();

        assertEquals(exception, json);
    }

    @Test
    public void testWeCanAppendNewAttributeBeforeEndCall() throws Exception {
        HttpException exception = new HttpException();
        JsonExceptionSerializer serializer = new JsonExceptionSerializer(exception);

        String json = serializer
                .appendAttribute("attributeKey", "attributeValue")
                .end();

        assertThat(json, equalTo(
                "{\"exception\":\"" + exception.getClass().toString() + "\"," +
                        "\"message\":\"" + exception.getMessage() + "\"," +
                        "\"attributeKey\":\"attributeValue\"}"));
    }

    @Test
    public void testJsonContainsInternationalizedMessageWhenLocalIsSet() throws Exception {
        APIException exception = new APIException(new T_("message"));
        fakeI18n.setL10n("localization");
        exception.setLocale(LOCALE.en);
        JsonExceptionSerializer serializer = new JsonExceptionSerializer(exception);

        String json = serializer.end();

        assertThat(json, equalTo(
                "{\"exception\":\"" + exception.getClass().toString() + "\"," +
                        "\"message\":\"localization\"" + "}"));
    }

    private void assertEquals(Exception e, String json) {
        assertThat(json, equalTo("{\"exception\":\"" + e.getClass().toString() + "\"," +
                "\"message\":\"" + e.getMessage() + "\"}"));
    }

}
