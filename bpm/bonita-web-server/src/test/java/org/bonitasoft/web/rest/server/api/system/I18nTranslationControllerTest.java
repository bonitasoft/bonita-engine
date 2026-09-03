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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.web.rest.server.api.AbstractControllerTest;
import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

/**
 * @author Julien Mege
 */
class I18nTranslationControllerTest extends AbstractControllerTest<I18nTranslationController> {

    @Mock
    private I18n i18n;

    @Override
    protected I18nTranslationController createController() {
        return spy(new I18nTranslationController());
    }

    @Override
    protected void configureMocks(I18nTranslationController controller) throws Exception {
        doReturn(i18n).when(controller).getI18n();
    }

    @Test
    void should_return_translation_for_the_given_locale() throws Exception {
        Map<String, String> translations = new HashMap<>();
        translations.put("key1", "<strong>message 1</strong>");
        translations.put("key2", "autre méssage");
        translations.put("key3", "~%^*µ");

        when(i18n.getLocale(AbstractI18n.LOCALE.fr)).thenReturn(translations);

        mockMvc.perform(
                get("/API/system/i18ntranslation")
                        .param("f", "locale=fr")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        [
                            {"key": "key1", "value": "<strong>message 1</strong>"},
                            {"key": "key2", "value": "autre méssage"},
                            {"key": "key3", "value": "~%^*µ"}
                        ]
                        """, true));
    }

    @Test
    void should_return_http400_error_code_when_no_filter_param() throws Exception {
        mockMvc.perform(get("/API/system/i18ntranslation")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(IllegalArgumentException.class.toString()))
                .andExpect(jsonPath("$.message").value("filter locale is mandatory"));
    }

    @Test
    void should_return_http400_error_code_when_no_locale_filter_param() throws Exception {
        mockMvc.perform(
                get("/API/system/i18ntranslation")
                        .param("f", "test")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(IllegalArgumentException.class.toString()))
                .andExpect(jsonPath("$.message").value("filter locale is mandatory"));
    }

    @Test
    void should_return_http400_error_code_when_empty_locale_filter_param() throws Exception {
        mockMvc.perform(
                get("/API/system/i18ntranslation")
                        .param("f", "locale=")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(IllegalArgumentException.class.toString()))
                .andExpect(jsonPath("$.message").value("filter locale is mandatory"));
    }

}
