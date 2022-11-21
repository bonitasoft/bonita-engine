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
package org.bonitasoft.web.toolkit.client.data.item.attribute.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.junit.Before;
import org.junit.Test;

public class StringFormatURLValidatorTest {

    private StringFormatURLValidator stringFormatURLValidator = new StringFormatURLValidator();

    @Before
    public void before() {
        // to initialize the system:
        I18n.getInstance();
    }

    @Test
    public void should_verify_urls() {
        checkUrl("toto", true);
        checkUrl("www.toto", false);
        checkUrl("https://toto", false);
        checkUrl("ftp://toto", false);
        checkUrl("http://toto", false);
        checkUrl("http://toto?tata.titi=tutu", false);
    }

    private void checkUrl(String url, boolean shouldHaveErrors) throws AssertionError {
        stringFormatURLValidator.reset();
        stringFormatURLValidator._check(url);
        assertThat(stringFormatURLValidator.getErrors().isEmpty()).isEqualTo(!shouldHaveErrors);
    }

}
