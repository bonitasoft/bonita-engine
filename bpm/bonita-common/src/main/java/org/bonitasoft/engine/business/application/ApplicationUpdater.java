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
package org.bonitasoft.engine.business.application;

/**
 * Allows to define which {@link Application} fields will be updated
 *
 * @author Elias Ricken de Medeiros
 * @see Application
 * @since 7.0.0
 * @deprecated This class should no longer be used. Since 9.0.0, Applications should be updated at startup.
 */
@Deprecated(since = "10.2.0")
public class ApplicationUpdater extends AbstractApplicationUpdater<ApplicationUpdater> {

    private static final long serialVersionUID = 4565052647320534796L;

    /**
     * Defines the identifier of the new {@link org.bonitasoft.engine.business.application.ApplicationPage} defined as
     * the {@link Application} home page
     *
     * @param applicationPageId the identifier of {@code ApplicationPage} associated to the {@code Application}
     * @return the current {@code ApplicationUpdater}
     * @see Application
     * @see org.bonitasoft.engine.business.application.ApplicationPage
     */
    public ApplicationUpdater setHomePageId(final Long applicationPageId) {
        getFields().put(ApplicationField.HOME_PAGE_ID, applicationPageId);
        return this;
    }
}
