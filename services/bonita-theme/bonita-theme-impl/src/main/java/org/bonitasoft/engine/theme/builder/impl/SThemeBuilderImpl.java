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
import org.bonitasoft.engine.theme.model.STheme;
import org.bonitasoft.engine.theme.model.SThemeType;
import org.bonitasoft.engine.theme.model.impl.SThemeImpl;

/**
 * @author Celine Souchet
 */
public class SThemeBuilderImpl implements SThemeBuilder {

    private final SThemeImpl theme;

    public SThemeBuilderImpl(final SThemeImpl theme) {
        super();
        this.theme = theme;
    }

    @Override
    public SThemeBuilder setDefault(final boolean isDefault) {
        theme.setDefault(isDefault);
        return this;
    }

    @Override
    public SThemeBuilder setContent(byte[] content) {
        theme.setContent(content);
        return this;
    }

    @Override
    public SThemeBuilder setCSSContent(byte[] cssContent) {
        theme.setCssContent(cssContent);
        return this;
    }

    @Override
    public SThemeBuilder setType(SThemeType type) {
        theme.setType(type);
        return this;
    }

    @Override
    public SThemeBuilder setLastUpdateDate(long lastUpdateDate) {
        theme.setLastUpdateDate(lastUpdateDate);
        return this;
    }

    @Override
    public STheme done() {
        return theme;
    }
}
