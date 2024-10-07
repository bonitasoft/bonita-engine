/**
 * Copyright (C) 2024 Bonitasoft S.A.
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

import java.io.Serializable;
import java.util.Map;

/**
 * Describes the information about an {@link ApplicationLink} to be created
 *
 * @see ApplicationLink
 * @since 10.2.0
 * @deprecated This class should no longer be used. Since 9.0.0, Applications should be created at startup.
 */
@Deprecated(since = "10.2.0")
public class ApplicationLinkCreator extends AbstractApplicationCreator<ApplicationLinkCreator> {

    private static final long serialVersionUID = 5045658936235401181L;
    private transient Map<ApplicationField, Serializable> fieldsCheckedMap;

    /**
     * Creates an instance of <code>ApplicationCreator</code> containing mandatory information.
     *
     * @param token the {@code ApplicationLink} token. The token will be part of application URL. It cannot be null or
     *        empty and should contain only alpha numeric
     *        characters and the following special characters '-', '.', '_' or '~'. In addition, the following words are
     *        reserved key words and cannot be used
     *        as token: 'api', 'content', 'theme'.
     * @param displayName the <code>ApplicationLink</code> display name. It cannot be null or empty
     * @param version the <code>ApplicationLink</code> version
     * @see ApplicationLink
     */
    public ApplicationLinkCreator(final String token, final String displayName, final String version) {
        super(token, displayName, version);
    }

    @Override
    public boolean isLink() {
        return true;
    }

    @Override
    public Map<ApplicationField, Serializable> getFields() {
        // make sure no field for Legacy Application and not suitable for Application Link will get inserted there
        if (fieldsCheckedMap == null) {
            fieldsCheckedMap = new CheckedApplicationFieldMap(super.getFields(),
                    k -> k.isForClass(ApplicationLink.class));
        }
        return fieldsCheckedMap;
    }

}
