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
package org.bonitasoft.engine.bdm;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.model.UniqueConstraint;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.model.field.RelationField;

/**
 * @author Laurent Leseigneur
 */
public interface QueryGenerator {

    String FIND_PREFIX = "find";
    String COUNT_PREFIX = "countFor";

    char BLANK_SPACE = ' ';

    String NEW_LINE = "\n";
    String OPENING_PARENTHESIS = "(";
    String CLOSING_PARENTHESIS = ")";

    String SELECT = "SELECT ";
    String COUNT = "COUNT";
    String FROM = "FROM ";
    String WHERE = "WHERE ";
    String LOGIC_AND = "AND ";
    String ORDER_BY = "ORDER BY ";

    String createQueryNameForField(Field field);

    String createQueryNameForUniqueConstraint(UniqueConstraint uc);

    String getSelectAllQueryName();

    String getQueryName(final String... fieldNames);

    Query createSelectAllQueryForBusinessObject(BusinessObject businessObject);

    Query createQueryForField(BusinessObject businessObject, Field f);

    Query createQueryForPersistenceId(final BusinessObject businessObject);

    Query createQueryForUniqueConstraint(final BusinessObject businessObject, final UniqueConstraint uniqueConstraint);

    Query createQueryForLazyField(BusinessObject businessObject, RelationField relationField);
}
