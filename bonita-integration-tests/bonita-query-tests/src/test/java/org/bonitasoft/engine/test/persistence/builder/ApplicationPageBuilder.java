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

import org.bonitasoft.engine.business.application.model.impl.SApplicationPageImpl;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationPageBuilder extends PersistentObjectBuilder<SApplicationPageImpl, ApplicationPageBuilder> {

    private long applicationId;
    private long pageId;
    private String token;

    public static ApplicationPageBuilder anApplicationPage() {
        return new ApplicationPageBuilder();
    }

    @Override
    SApplicationPageImpl _build() {
        return new SApplicationPageImpl(applicationId, pageId, token);
    }

    public ApplicationPageBuilder withApplicationId(final long applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public ApplicationPageBuilder withPageId(final long pageId) {
        this.pageId = pageId;
        return this;
    }

    public ApplicationPageBuilder withToken(final String token) {
        this.token = token;
        return this;
    }

    @Override
    ApplicationPageBuilder getThisBuilder() {
        return this;
    }

}
