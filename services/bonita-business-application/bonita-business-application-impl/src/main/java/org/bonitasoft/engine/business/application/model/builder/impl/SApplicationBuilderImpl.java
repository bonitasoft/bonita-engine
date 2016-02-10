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
package org.bonitasoft.engine.business.application.model.builder.impl;

import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.builder.SApplicationBuilder;
import org.bonitasoft.engine.business.application.model.impl.SApplicationImpl;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SApplicationBuilderImpl implements SApplicationBuilder {

    private final SApplicationImpl application;

    public SApplicationBuilderImpl(final SApplicationImpl application) {
        this.application = application;
    }

    @Override
    public SApplication done() {
        return application;
    }

    @Override
    public SApplicationBuilder setDescription(final String description) {
        application.setDescription(description);
        return this;
    }

    @Override
    public SApplicationBuilder setIconPath(final String iconPath) {
        application.setIconPath(iconPath);
        return this;
    }

    @Override
    public SApplicationBuilder setProfileId(final Long profileId) {
        application.setProfileId(profileId);
        return this;
    }

    @Override
    public SApplicationBuilder setState(String state) {
        application.setState(state);
        return this;
    }


}
