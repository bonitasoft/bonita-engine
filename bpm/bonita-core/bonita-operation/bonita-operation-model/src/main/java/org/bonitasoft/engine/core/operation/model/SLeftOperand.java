/**
 * Copyright (C) 2011, 2014 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.operation.model;

import java.io.Serializable;

/**
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public interface SLeftOperand extends Serializable {

    static final String TYPE_EXTERNAL_DATA = "EXTERNAL_DATA";

    static final String TYPE_DOCUMENT = "DOCUMENT";

    static final String TYPE_SEARCH_INDEX = "SEARCH_INDEX";

    static final String TYPE_DATA = "DATA";

    static final String TYPE_BUSINESS_DATA = "BUSINESS_DATA";

    String getName();

    String getType();

}
