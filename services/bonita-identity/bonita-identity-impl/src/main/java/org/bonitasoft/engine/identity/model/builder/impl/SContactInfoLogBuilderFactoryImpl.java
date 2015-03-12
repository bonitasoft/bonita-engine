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

import org.bonitasoft.engine.identity.model.builder.SContactInfoLogBuilder;
import org.bonitasoft.engine.identity.model.builder.SContactInfoLogBuilderFactory;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilderFactory;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class SContactInfoLogBuilderFactoryImpl extends CRUDELogBuilderFactory implements SContactInfoLogBuilderFactory {

    public static final int USER_CONTACT_INFO_INDEX = 1;

    public static final int USER_CONTACT_INFO_USERID_INDEX = 2;

    public static final String USER_CONTACT_INFO_INDEX_NAME = "numericIndex2";

    public static final String USER_CONTACT_INFO_USERID_INDEX_NAME = "numericIndex3";

    @Override
    public SContactInfoLogBuilder createNewInstance() {
        return new SContactInfoLogBuilderImpl();
    }
    
    @Override
    public String getObjectIdKey() {
        return USER_CONTACT_INFO_INDEX_NAME;
    }

    @Override
    public String getContactInfoUserIdKey() {
        return USER_CONTACT_INFO_USERID_INDEX_NAME;
    }

}
