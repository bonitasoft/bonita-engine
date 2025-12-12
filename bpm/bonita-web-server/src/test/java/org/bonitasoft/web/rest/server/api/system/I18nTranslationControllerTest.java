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

import static org.bonitasoft.web.rest.server.api.RestControllerUtils.initMockMvcWithSessionAttributes;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @author Julien Mege
 */
@MockitoSettings(strictness = LENIENT)
class I18nTranslationControllerTest {

    private final Map<String, Object> sessionAttributes = new HashMap<>();
    private MockMvc mockMvc;

    @Mock
    private APISession apiSession;

    @Mock
    private I18n i18n;

    @BeforeEach
    void setUp() {
        I18nTranslationController controller = spy(new I18nTranslationController());
        mockMvc = initMockMvcWithSessionAttributes(controller, sessionAttributes, apiSession);
        doReturn(i18n).when(controller).getI18n();
    }

    @Test
    void should_return_translation_for_the_given_locale() throws Exception {
        Map<String, String> translations = new HashMap<>();
        translations.put("key1", "<strong>message 1</strong>");
        translations.put("key2", "autre méssage");
        translations.put("key3", "~%^*µ");

        when(i18n.getLocale(AbstractI18n.LOCALE.fr)).thenReturn(translations);

        mockMvc.perform(get("/API/system/i18ntranslation?f=locale=fr")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        [
                            {"key": "key1", "value": "<strong>message 1</strong>"},
                            {"key": "key2", "value": "autre méssage"},
                            {"key": "key3", "value": "~%^*µ"}
                        ]
                        """));
    }

    @Test
    void should_return_http400_error_code_when_no_queryString() throws Exception {
        mockMvc.perform(get("/API/system/i18ntranslation")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_http400_error_code_when_no_locale_param() throws Exception {
        mockMvc.perform(get("/API/system/i18ntranslation?f=test")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}
