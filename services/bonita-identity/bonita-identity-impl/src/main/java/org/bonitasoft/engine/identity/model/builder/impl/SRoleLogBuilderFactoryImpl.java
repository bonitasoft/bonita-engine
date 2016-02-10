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

import org.bonitasoft.engine.identity.model.builder.SRoleLogBuilder;
import org.bonitasoft.engine.identity.model.builder.SRoleLogBuilderFactory;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilderFactory;

/**
 * @author Yanyan Liu
 * @author Matthieu Chafotte
 */
public class SRoleLogBuilderFactoryImpl extends CRUDELogBuilderFactory implements SRoleLogBuilderFactory {

    public static final int ROLE_INDEX = 0;

    public static final String ROLE_INDEX_NAME = "numericIndex1";

    @Override
    public SRoleLogBuilder createNewInstance() {
        return new SRoleLogBuilderImpl();
    }
    
    @Override
    public String getObjectIdKey() {
        return ROLE_INDEX_NAME;
    }

}
