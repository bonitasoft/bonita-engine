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
 * Describes the information about a Legacy {@link Application} to be created
 *
 * @author Elias Ricken de Medeiros
 * @see Application
 * @since 7.0.0
 * @deprecated This class should no longer be used. Since 9.0.0, Applications should be created at startup.
 */
@Deprecated(since = "10.2.0")
public class ApplicationCreator extends AbstractApplicationCreator<ApplicationCreator> {

    private static final long serialVersionUID = -916041825489100271L;

    /**
     * Creates an instance of <code>ApplicationCreator</code> containing mandatory information.
     * <p>The created {@link Application} will used the default layout.</p>
     *
     * @param token the {@code Application} token. The token will be part of application URL. It cannot be null or empty
     *        and should contain only alpha numeric
     *        characters and the following special characters '-', '.', '_' or '~'. In addition, the following words are
     *        reserved key words and cannot be used
     *        as token: 'api', 'content', 'theme'.
     * @param displayName the <code>Application</code> display name. It cannot be null or empty
     * @param version the <code>Application</code> version
     * @see Application
     */
    public ApplicationCreator(final String token, final String displayName, final String version) {
        super(token, displayName, version);
    }

    @Override
    public boolean isLink() {
        return false;
    }

}
