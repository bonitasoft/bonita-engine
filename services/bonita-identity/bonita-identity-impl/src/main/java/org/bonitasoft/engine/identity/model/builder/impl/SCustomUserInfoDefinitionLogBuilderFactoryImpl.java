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

import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionLogBuilder;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionLogBuilderFactory;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilderFactory;

/**
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 */
public class SCustomUserInfoDefinitionLogBuilderFactoryImpl extends CRUDELogBuilderFactory implements SCustomUserInfoDefinitionLogBuilderFactory {

    public static final int SPROFILE_METADATA_DEFINITION_INDEX = 1;

    public static final String SPROFILE_METADATA_DEFINITION_INDEX_NAME = "numericIndex2";

    @Override
    public SCustomUserInfoDefinitionLogBuilder createNewInstance() {
        return new SCustomUserInfoDefinitionLogBuilderImpl();
    }
    
    @Override
    public String getObjectIdKey() {
        return SPROFILE_METADATA_DEFINITION_INDEX_NAME;
    }

}
