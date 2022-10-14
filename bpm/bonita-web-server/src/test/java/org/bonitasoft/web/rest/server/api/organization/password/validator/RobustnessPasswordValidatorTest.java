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
package org.bonitasoft.web.rest.server.api.organization.password.validator;

import static org.junit.Assert.*;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.junit.Test;

public class RobustnessPasswordValidatorTest {

    RobustnessPasswordValidator robustnessPasswordValidator = new RobustnessPasswordValidator();

    @Test
    public void testWithWrongPassword() {
        I18n.getInstance();
        robustnessPasswordValidator.setLocale("en");
        robustnessPasswordValidator.check("password");
        assertFalse(robustnessPasswordValidator.getErrors().isEmpty());
    }

    @Test
    public void testwithLongPassword() {
        I18n.getInstance();
        robustnessPasswordValidator.setLocale("en");
        robustnessPasswordValidator.check("myreallylongpassword");
        assertFalse(robustnessPasswordValidator.getErrors().isEmpty());
    }

    @Test
    public void testwithGoodPassword() {
        I18n.getInstance();
        robustnessPasswordValidator.setLocale("en");
        robustnessPasswordValidator.check("MyPasswOrd!?321D*");
        assertTrue(robustnessPasswordValidator.getErrors().isEmpty());
    }
}
