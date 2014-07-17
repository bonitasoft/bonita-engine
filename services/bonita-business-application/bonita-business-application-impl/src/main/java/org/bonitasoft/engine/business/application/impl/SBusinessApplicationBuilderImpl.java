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
package org.bonitasoft.engine.business.application.impl;

import org.bonitasoft.engine.business.application.SBusinessApplication;
import org.bonitasoft.engine.business.application.SBusinessApplicationBuilder;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SBusinessApplicationBuilderImpl implements SBusinessApplicationBuilder {

    private final SBusinessApplicationImpl application;

    public SBusinessApplicationBuilderImpl(final SBusinessApplicationImpl application) {
        this.application = application;
    }

    @Override
    public SBusinessApplication done() {
        return application;
    }

    @Override
    public SBusinessApplicationBuilder setDescription(final String description) {
        application.setDescription(description);
        return this;
    }

}
