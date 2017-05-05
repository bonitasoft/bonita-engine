/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.theme.builder.impl;

import org.bonitasoft.engine.theme.builder.SThemeBuilder;
import org.bonitasoft.engine.theme.builder.SThemeBuilderFactory;
import org.bonitasoft.engine.theme.model.STheme;
import org.bonitasoft.engine.theme.model.SThemeType;
import org.bonitasoft.engine.theme.model.impl.SThemeImpl;

/**
 * @author Celine Souchet
 */
public class SThemeBuilderFactoryImpl implements SThemeBuilderFactory {

    @Override
    public SThemeBuilder createNewInstance(final byte[] content, final boolean isDefault, final SThemeType type, final long lastUpdateDate) {
        final SThemeImpl theme = new SThemeImpl(content, isDefault, type, lastUpdateDate);
        return new SThemeBuilderImpl(theme);
    }

    @Override
    public SThemeBuilder createNewInstance(final STheme theme) {
        return new SThemeBuilderImpl(new SThemeImpl(theme));
    }
}
