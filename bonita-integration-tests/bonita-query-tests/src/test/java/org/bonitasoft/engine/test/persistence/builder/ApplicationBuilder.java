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
package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.business.application.model.SApplicationState;
import org.bonitasoft.engine.business.application.model.impl.SApplicationImpl;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationBuilder extends PersistentObjectBuilder<SApplicationImpl, ApplicationBuilder> {

    private String name;
    private String version;
    private String path;
    private String displayName;

    public static ApplicationBuilder anApplication() {
        return new ApplicationBuilder();
    }

    @Override
    SApplicationImpl _build() {
        return new SApplicationImpl(name, displayName, version, System.currentTimeMillis(), 21, SApplicationState.DEACTIVATED.name());
    }

    public ApplicationBuilder withToken(final String name) {
        this.name = name;
        return this;
    }

    public ApplicationBuilder withDispalyName(final String displayName) {
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

    @Override
    ApplicationBuilder getThisBuilder() {
        return this;
    }

}
