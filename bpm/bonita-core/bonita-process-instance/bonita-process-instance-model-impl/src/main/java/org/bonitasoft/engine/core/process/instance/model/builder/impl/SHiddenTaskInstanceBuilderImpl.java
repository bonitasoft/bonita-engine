/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.SHiddenTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SHiddenTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.impl.SHiddenTaskInstanceImpl;

/**
 * @author Emmanuel Duchastenier
 */
public class SHiddenTaskInstanceBuilderImpl implements SHiddenTaskInstanceBuilder {

    private static final String USER_ID_KEY = "userId";

    private static final String ACTIVITY_INSTANCE_ID_KEY = "activityInstanceId";

    private SHiddenTaskInstanceImpl entity;

    @Override
    public SHiddenTaskInstanceBuilderImpl createNewInstance(final long activityId, final long userId) {
        entity = new SHiddenTaskInstanceImpl(activityId, userId);
        return this;
    }

    @Override
    public SHiddenTaskInstance done() {
        return entity;
    }

    @Override
    public String getUserIdKey() {
        return USER_ID_KEY;
    }

    @Override
    public String getActivityInstanceIdKey() {
        return ACTIVITY_INSTANCE_ID_KEY;
    }

}
