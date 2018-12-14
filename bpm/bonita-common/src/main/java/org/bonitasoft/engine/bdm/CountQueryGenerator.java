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
public class CountQueryGenerator extends AbstractQueryGenerator {

    public String getQueryPrefix() {
        return COUNT_PREFIX + "Find";
    }

    @Override
    public Query createQueryForPersistenceId(BusinessObject businessObject) {
        return null;
    }

    @Override
    protected String getQualifiedReturnType(BusinessObject businessObject) {
        return Long.class.getName();
    }

    @Override
    protected String getQueryContentForLazyField() {
        return "SELECT COUNT(%s) FROM %s %s JOIN %s.%s as %s WHERE %s.%s= :%s";
    }

    @Override
    protected void addOrderBy(char tablePrefix, StringBuilder builder) {
        //nothing to do
    }

    public void buildSelect(char simpleNameAlias, StringBuilder builder) {
        builder.append(SELECT)
                .append(COUNT).append(OPENING_PARENTHESIS).append(simpleNameAlias).append(CLOSING_PARENTHESIS)
                .append(NEW_LINE);
    }

    @Override
    public String getListReturnType() {
        return Long.class.getName();
    }

    @Override
    public Query createQueryForLazyField(BusinessObject businessObject, RelationField relationField) {
        if (relationField.isCollection()) {
            return super.createQueryForLazyField(businessObject, relationField);
        }
        return null;
    }

    @Override
    public Query createQueryForUniqueConstraint(BusinessObject businessObject, UniqueConstraint uniqueConstraint) {
        return null;
    }

    @Override
    public Query createQueryForField(BusinessObject businessObject, Field field) {
        if (hasUniqueConstraintOnField(businessObject, field)) {
            return null;
        }
        return super.createQueryForField(businessObject, field);
    }

    private boolean hasUniqueConstraintOnField(BusinessObject businessObject, Field field) {
        for (UniqueConstraint uniqueConstraint : businessObject.getUniqueConstraints()) {
            if (uniqueConstraint.getFieldNames().size() == 1 && uniqueConstraint.getFieldNames().contains(field.getName())) {
                return true;
            }
        }
        return false;

    }

}
