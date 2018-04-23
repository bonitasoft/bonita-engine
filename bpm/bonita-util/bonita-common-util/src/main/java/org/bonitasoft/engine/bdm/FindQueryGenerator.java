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

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.model.UniqueConstraint;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.SimpleField;

/**
 * @author Laurent Leseigneur
 */
public class FindQueryGenerator extends AbstractQueryGenerator {

    public String getQueryPrefix() {
        return FIND_PREFIX;
    }

    @Override
    protected void addOrderBy(char tablePrefix, StringBuilder builder) {
        builder.append(ORDER_BY).append(tablePrefix).append('.').append(Field.PERSISTENCE_ID);
    }

    public void buildSelect(char simpleNameAlias, StringBuilder builder) {
        builder.append(SELECT).append(simpleNameAlias).append(NEW_LINE);
    }

    @Override
    public String getListReturnType() {
        return List.class.getName();
    }

    @Override
    public Query createQueryForPersistenceId(BusinessObject businessObject) {
        final SimpleField persistenceIdField = new SimpleField();
        persistenceIdField.setName(Field.PERSISTENCE_ID);
        persistenceIdField.setType(FieldType.LONG);
        final String name = createQueryNameForField(persistenceIdField);
        final UniqueConstraint constraint = new UniqueConstraint();
        constraint.setFieldNames(Arrays.asList(persistenceIdField.getName()));
        final String content = createQueryContentForUniqueConstraint(businessObject.getQualifiedName(), constraint);
        final Query q = new Query(name, content, businessObject.getQualifiedName());
        q.addQueryParameter(persistenceIdField.getName(), persistenceIdField.getType().getClazz().getName());
        return q;
    }

    @Override
    protected String getQualifiedReturnType(BusinessObject businessObject) {
        return businessObject.getQualifiedName();
    }

    @Override
    protected String getQueryContentForLazyField() {
        return "SELECT %s FROM %s %s JOIN %s.%s as %s WHERE %s.%s= :%s";

    }

    protected String createQueryContentForUniqueConstraint(final String businessObjectName, final UniqueConstraint uniqueConstraint) {
        checkArgumentisNotEmpty(businessObjectName);
        final String simpleName = BDMSimpleNameProvider.getSimpleBusinessObjectName(businessObjectName);
        final char alias = BDMSimpleNameProvider.getSimpleNameAlias(simpleName);
        final String selectBlock = buildSelectFrom(simpleName, alias);
        return buildQueryForUniqueConstraint(uniqueConstraint, alias, selectBlock);
    }

    protected String buildQueryForUniqueConstraint(UniqueConstraint uniqueConstraint, char alias, String selectBlock) {
        final StringBuilder builder = new StringBuilder();
        builder.append(selectBlock);
        builder.append(buildWhereAnd(alias, uniqueConstraint.getFieldNames()));
        return builder.toString();
    }

    private String buildWhereAnd(final char prefix, final List<String> parameterNames) {
        final StringBuilder builder = new StringBuilder(WHERE);
        String paramName;
        final int numberOfParameters = parameterNames.size() - 1;
        for (int i = 0; i < numberOfParameters; i++) {
            paramName = parameterNames.get(i);
            builder.append(buildCompareField(prefix, paramName));
            builder.append(LOGIC_AND);
        }
        paramName = parameterNames.get(numberOfParameters);
        builder.append(buildCompareField(prefix, paramName));
        return builder.toString();
    }

}
