/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.business.application.model.AbstractSApplication;
import org.bonitasoft.engine.business.application.model.SApplicationState;
import org.bonitasoft.engine.business.application.model.SApplicationWithIcon;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationBuilder extends PersistentObjectBuilder<AbstractSApplication, ApplicationBuilder> {

    private String name;
    private String version;
    private String path;
    private String displayName;
    private Long layoutId;
    private Long themeId;
    private Long profileId;

    public static ApplicationBuilder anApplication() {
        return new ApplicationBuilder();
    }

    @Override
    SApplicationWithIcon _build() {
        SApplicationWithIcon application = new SApplicationWithIcon(name, displayName, version,
                System.currentTimeMillis(), 21,
                SApplicationState.DEACTIVATED.name());
        application.setIconPath(path);
        application.setProfileId(profileId);
        application.setThemeId(themeId);
        application.setLayoutId(layoutId);
        return application;
    }

    public ApplicationBuilder withToken(final String name) {
        this.name = name;
        return this;
    }

    public ApplicationBuilder withDisplayName(final String displayName) {
        this.displayName = displayName;
        return this;
    }

    public ApplicationBuilder withVersion(final String version) {
        this.version = version;
        return this;
    }

    public ApplicationBuilder withPath(final String path) {
        this.path = path;
        return this;
    }

    public ApplicationBuilder withLayout(final Long layoutId) {
        this.layoutId = layoutId;
        return this;
    }

    public ApplicationBuilder withTheme(final Long themeId) {
        this.themeId = themeId;
        return this;
    }

    public ApplicationBuilder withProfile(final long profileId) {
        this.profileId = profileId;
        return this;
    }

    @Override
    ApplicationBuilder getThisBuilder() {
        return this;
    }

}
