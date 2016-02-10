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
package org.bonitasoft.engine.identity.model.builder.impl;

import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.SUserBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserBuilderFactory;
import org.bonitasoft.engine.identity.model.impl.SUserImpl;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SUserBuilderFactoryImpl implements SUserBuilderFactory {

    static final String ID = "id";

    static final String MANAGER_USER_ID = "managerUserId";

    static final String JOB_TITLE = "jobTitle";

    static final String TITLE = "title";

    static final String LAST_NAME = "lastName";

    static final String FIRST_NAME = "firstName";

    static final String USER_NAME = "userName";

    static final String PASSWORD = "password";

    static final String LAST_UPDATE = "lastUpdate";

    static final String LAST_CONNECTION = "lastConnection";

    static final String CREATED_BY = "createdBy";

    static final String CREATION_DATE = "creationDate";

    static final String ICON_NAME = "iconName";

    static final String ICON_PATH = "iconPath";

    static final String ENABLED = "enabled";

    @Override
    public SUserBuilder createNewInstance() {
        final SUserImpl entity = new SUserImpl();
        return new SUserBuilderImpl(entity);
    }

    @Override
    public SUserBuilder createNewInstance(final SUser user) {
        final SUserImpl entity = new SUserImpl(user);
        return new SUserBuilderImpl(entity);
    }

    @Override
    public String getIdKey() {
        return ID;
    }

    @Override
    public String getUserNameKey() {
        return USER_NAME;
    }

    @Override
    public String getPasswordKey() {
        return PASSWORD;
    }

    @Override
    public String getFirstNameKey() {
        return FIRST_NAME;
    }

    @Override
    public String getLastNameKey() {
        return LAST_NAME;
    }

    @Override
    public String getTitleKey() {
        return TITLE;
    }

    @Override
    public String getJobTitleKey() {
        return JOB_TITLE;
    }

    @Override
    public String getManagerUserIdKey() {
        return MANAGER_USER_ID;
    }

    @Override
    public String getIconNameKey() {
        return ICON_NAME;
    }

    @Override
    public String getIconPathKey() {
        return ICON_PATH;
    }

    @Override
    public String getCreatedByKey() {
        return CREATED_BY;
    }

    @Override
    public String getCreationDateKey() {
        return CREATION_DATE;
    }

    @Override
    public String getLastUpdateKey() {
        return LAST_UPDATE;
    }

    @Override
    public String getLastConnectionKey() {
        return LAST_CONNECTION;
    }

    @Override
    public String getEnabledKey() {
        return ENABLED;
    }

}
