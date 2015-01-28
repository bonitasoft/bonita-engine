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
package org.bonitasoft.engine.business.application;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes the information about an {@link ApplicationMenu} to be created
 *
 * @author Elias Ricken de Medeiros
 * @since 6.4
 * @see ApplicationMenu
 */
public class ApplicationMenuCreator implements Serializable {

    private static final long serialVersionUID = 5253969343647340983L;

    private final Map<ApplicationMenuField, Serializable> fields;

    /**
     * Creates an instance of {@code ApplicationMenuCreator}
     *
     * @param applicationId the identifier of related {@link org.bonitasoft.engine.business.application.Application}
     * @param displayName the {@link org.bonitasoft.engine.business.application.ApplicationMenu} display name
     * @param applicationPageId the identifier of related {@link org.bonitasoft.engine.business.application.ApplicationPage}
     * @see ApplicationMenu
     */
    public ApplicationMenuCreator(final Long applicationId, final String displayName, final Long applicationPageId) {
        this(applicationId, displayName);
        fields.put(ApplicationMenuField.APPLICATION_PAGE_ID, applicationPageId);
    }

    /**
     * Creates an instance of {@code ApplicationMenuCreator}
     *
     * @param applicationId the identifier of related {@link Application}
     * @param displayName the {@link ApplicationMenu} display name
     * @see ApplicationMenu
     * @see org.bonitasoft.engine.business.application.Application
     */
    public ApplicationMenuCreator(final Long applicationId, final String displayName) {
        fields = new HashMap<ApplicationMenuField, Serializable>(4);
        fields.put(ApplicationMenuField.DISPLAY_NAME, displayName);
        fields.put(ApplicationMenuField.APPLICATION_ID, applicationId);
    }

    /**
     * Defines the identifier of parent {@link ApplicationMenu}
     *
     * @param parentId the identifier of parent {@code ApplicationMenu}
     * @return
     */
    public ApplicationMenuCreator setParentId(final long parentId) {
        fields.put(ApplicationMenuField.PARENT_ID, parentId);
        return this;
    }

    /**
     * Retrieves the identifier of the parent {@link ApplicationMenu}. If no parent is defined this method will return null.
     * @return the identifier of the parent {@code ApplicationMenu} or null if no parent is defined
     * @see org.bonitasoft.engine.business.application.ApplicationMenu
     */
    public Long getParentId() {
        return (Long)fields.get(ApplicationMenuField.PARENT_ID);
    }

    /**
     * Retrieves all fields defined in this {@code ApplicationMenuCreator}
     *
     * @return a {@link Map}<{@link ApplicationMenuField}, {@link Serializable}> containing all fields defined in this {@code ApplicationMenuCreator}
     */
    public Map<ApplicationMenuField, Serializable> getFields() {
        return Collections.unmodifiableMap(fields);
    }

}
