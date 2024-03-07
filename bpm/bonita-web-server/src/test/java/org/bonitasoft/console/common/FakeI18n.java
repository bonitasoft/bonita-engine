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
package org.bonitasoft.console.common;

import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n;

/**
 * Created by Vincent Elcrin
 * Date: 23/09/13
 * Time: 18:40
 */
public class FakeI18n extends AbstractI18n {

    private String l10n;

    public FakeI18n() {
        I18N_instance = this;
    }

    @Override
    public void loadLocale(LOCALE locale) {
    }

    @Override
    protected String getText(LOCALE locale, String key) {
        return l10n;
    }

    public void setL10n(String value) {
        l10n = value;
    }
}
