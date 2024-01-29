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
package org.bonitasoft.web.toolkit.client.common.texttemplate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class TextTemplateTest {

    public static final String BASE_TEXT = "This is a text template made by %the_user% \n" +
            "It has %multiple_vars% with even single %\n" +
            "and also %vars with spaces@@%\n" +
            "and %missing vars%";

    @Test
    public void should_find_expected_parameters_of_text_template() {
        TextTemplate textTemplate = new TextTemplate(BASE_TEXT);

        List<String> expectedParameters = textTemplate.getExpectedParameters();

        assertThat(expectedParameters).containsExactly(
                "the_user",
                "multiple_vars",
                "vars with spaces@@",
                "missing vars");
    }

    @Test
    public void should_replace_parameters_of_text_template() {
        TextTemplate textTemplate = new TextTemplate(BASE_TEXT);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("the_user", "Walter bates");
        parameters.put("multiple_vars", "Multiple variables to replace");
        parameters.put("vars with spaces@@", "Variables With all kind of special \nchars and space: @#%ˆ&*(%%%");
        String output = textTemplate.toString(parameters);

        assertThat(output).isEqualTo("This is a text template made by Walter bates \n" +
                "It has Multiple variables to replace with even single %\n" +
                "and also Variables With all kind of special \nchars and space: @#%ˆ&*(%%%\n" +
                "and %missing vars%");
    }

}
