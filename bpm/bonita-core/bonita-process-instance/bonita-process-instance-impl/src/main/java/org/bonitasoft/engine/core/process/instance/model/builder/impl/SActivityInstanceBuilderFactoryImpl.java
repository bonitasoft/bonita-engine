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
package org.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.builder.SActivityInstanceBuilderFactory;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public abstract class SActivityInstanceBuilderFactoryImpl extends SFlowNodeInstanceBuilderFactoryImpl implements SActivityInstanceBuilderFactory {

    private static final String ID_KEY = "id";

    private static final String NAME_KEY = "name";

    private static final String TOKEN_COUNT_KEY = "tokenCount";

    private static final String ABORTED_BY_BOUNDARY_KEY = "abortedByBoundary";

    @Override
    public String getIdKey() {
        return ID_KEY;
    }

    @Override
    public String getNameKey() {
        return NAME_KEY;
    }

    @Override
    public String getTokenCountKey() {
        return TOKEN_COUNT_KEY;
    }

    @Override
    public String getAbortedByBoundaryEventIdKey() {
        return ABORTED_BY_BOUNDARY_KEY;
    }

}
