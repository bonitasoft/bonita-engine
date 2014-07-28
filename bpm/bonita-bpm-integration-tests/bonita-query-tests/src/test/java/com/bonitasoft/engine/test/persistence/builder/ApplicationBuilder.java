/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package com.bonitasoft.engine.test.persistence.builder;

import com.bonitasoft.engine.business.application.SApplicationState;
import com.bonitasoft.engine.business.application.impl.SApplicationImpl;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationBuilder extends PersistentObjectBuilder<SApplicationImpl> {

    private String name;
    private String version;
    private String path;

    public static ApplicationBuilder anApplication() {
        return new ApplicationBuilder();
    }

    @Override
    SApplicationImpl _build() {
        return new SApplicationImpl(name, version, path, System.currentTimeMillis(), 21, SApplicationState.DEACTIVATED.name());
    }

    public ApplicationBuilder withName(final String name) {
        this.name = name;
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

}
