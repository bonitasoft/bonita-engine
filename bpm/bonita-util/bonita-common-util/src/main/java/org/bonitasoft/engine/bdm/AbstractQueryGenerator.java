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

import java.util.List;

import org.apache.commons.lang3.text.WordUtils;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.model.UniqueConstraint;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.SimpleField;

/**
 * @author Laurent Leseigneur
 */
public abstract class AbstractQueryGenerator implements QueryGenerator {

    @Override
    public String createQueryNameForField(Field field) {
        checkObjectIsNotNull(field, "field cannot be null");
        return getQueryName(field.getName());
    }

    public String getSelectAllQueryName() {
        return getQueryName();
    }

    @Override
    public String getQueryName(final String... fieldNames) {
        final StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(getQueryPrefix());
        if (fieldNames != null && fieldNames.length > 0) {
            nameBuilder.append("By");
            String paramName;
            final int numberOfFields = fieldNames.length - 1;
            for (int i = 0; i < numberOfFields; i++) {
                final String fieldName = fieldNames[i];
                paramName = WordUtils.capitalize(fieldName);
                nameBuilder.append(paramName).append("And");
            }
            final String fieldName = fieldNames[numberOfFields];
            paramName = WordUtils.capitalize(fieldName);
            nameBuilder.append(paramName);
        }
        return nameBuilder.toString();
    }

    protected abstract String getQueryPrefix();

    protected void checkObjectIsNotNull(Object object, String cause) {
        if (object == null) {
            throw new IllegalArgumentException(cause);
        }
    }

    protected void checkFieldIsNotACollection(Field field) {
        if (field.isCollection() != null && field.isCollection()) {
            throw new IllegalArgumentException("Collection field are not supported");
        }
    }

    @Override
    public abstract Query createQueryForPersistenceId(BusinessObject businessObject);

    @Override
    public Query createQueryForField(BusinessObject businessObject, Field field) {
        checkObjectIsNotNull(field, "field cannot be null");
        checkFieldIsNotACollection(field);
        final Query query = new Query(createQueryNameForField(field),
                createQueryContentForField(businessObject.getQualifiedName(), field), getListReturnType());
        if (field instanceof SimpleField) {
            query.addQueryParameter(field.getName(), ((SimpleField) field).getType().getClazz().getName());
        }
        return query;
    }

    @Override
    public Query createSelectAllQueryForBusinessObject(BusinessObject businessObject) {
        checkObjectIsNotNull(businessObject, "businessObject cannot be null");
        final String queryName = getSelectAllQueryName();
        final String content = createSelectAllQueryContent(businessObject.getQualifiedName());
        return new Query(queryName, content, getListReturnType());
    }

    protected abstract String getListReturnType();

    @Override
    public Query createQueryForUniqueConstraint(final BusinessObject businessObject, final UniqueConstraint uniqueConstraint) {
        final String name = createQueryNameForUniqueConstraint(uniqueConstraint);
        final String content = createQueryContentForUniqueConstraint(businessObject.getQualifiedName(), uniqueConstraint);
        final Query q = new Query(name, content, getQualifiedReturnType(businessObject));
        for (final String fieldName : uniqueConstraint.getFieldNames()) {
            final Field f = getField(fieldName, businessObject);
            if (f instanceof SimpleField) {
                q.addQueryParameter(f.getName(), ((SimpleField) f).getType().getClazz().getName());
            }
        }
        return q;
    }

    protected abstract String getQualifiedReturnType(BusinessObject businessObject);

    @Override
    public Query createQueryForLazyField(BusinessObject businessObject, RelationField relationField) {
        checkObjectIsNotNull(relationField, "relationField cannot be null");
        final String name = createQueryNameForLazyField(businessObject, relationField);
        final String content = createQueryContentForLazyField(businessObject.getQualifiedName(), relationField);
        final Query q = new Query(name, content, relationField.getReference().getQualifiedName());
        if (relationField.isCollection()) {
            q.setReturnType(List.class.getName());
        }
        q.addQueryParameter(Field.PERSISTENCE_ID, Long.class.getName());
        return q;
    }

    private String createQueryContentForLazyField(String businessObjectName, RelationField relationField) {
        checkArgumentisNotEmpty(businessObjectName);
        checkObjectIsNotNull(relationField, "field cannot be null");
        final String boName = BDMSimpleNameProvider.getSimpleBusinessObjectName(businessObjectName);
        final String boAlias = boName.toLowerCase() + "_0";
        final String fieldName = relationField.getName();
        final String fieldAlias = fieldName.toLowerCase() + "_1";

        return String.format(getQueryContentForLazyField(), fieldAlias, boName, boAlias,
                boAlias,
                fieldName, fieldAlias, boAlias, Field.PERSISTENCE_ID, Field.PERSISTENCE_ID);

    }

    protected abstract String getQueryContentForLazyField();

    private String createQueryNameForLazyField(BusinessObject businessObject, RelationField relationField) {
        final StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(getQueryPrefix());
        nameBuilder.append(WordUtils.capitalize(relationField.getName()));
        nameBuilder.append("By");
        nameBuilder.append(BDMSimpleNameProvider.getSimpleBusinessObjectName(businessObject.getQualifiedName()));
        nameBuilder.append("PersistenceId");
        return nameBuilder.toString();

    }

    private Field getField(String fieldName, BusinessObject businessObject) {
        Field field = businessObject.getField(fieldName);
        if(field == null) {
            throw new IllegalArgumentException(fieldName + " doesn't exist in " + businessObject.getQualifiedName());
        }
        return field;
    }

    private String createQueryContentForUniqueConstraint(String businessObjectName, UniqueConstraint uniqueConstraint) {
        checkArgumentisNotEmpty(businessObjectName);
        final String simpleName = BDMSimpleNameProvider.getSimpleBusinessObjectName(businessObjectName);
        final char alias = BDMSimpleNameProvider.getSimpleNameAlias(simpleName);
        final String selectBlock = buildSelectFrom(simpleName, alias);
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

    public String createQueryNameForUniqueConstraint(UniqueConstraint uniqueConstraint) {
        checkObjectIsNotNull(uniqueConstraint, "uniqueConstraint cannot be null");
        return getQueryName(uniqueConstraint.getFieldNames().toArray(new String[0]));//FIXME concat all fields instead of taking first field
    }

    protected String createQueryContentForField(String businessObjectName, Field field) {
        checkArgumentisNotEmpty(businessObjectName);
        checkObjectIsNotNull(field, "field cannot be null");
        final String simpleName = BDMSimpleNameProvider.getSimpleBusinessObjectName(businessObjectName);
        final char var = BDMSimpleNameProvider.getSimpleNameAlias(simpleName);
        final StringBuilder builder = new StringBuilder();
        builder.append(buildSelectFrom(simpleName, var));
        builder.append(buildWhere(var, field.getName()));
        builder.append(buildOrderBy(var));
        return builder.toString();
    }

    protected String createSelectAllQueryContent(String businessObjectName) {
        checkArgumentisNotEmpty(businessObjectName);
        final String simpleName = BDMSimpleNameProvider.getSimpleBusinessObjectName(businessObjectName);
        final char alias = BDMSimpleNameProvider.getSimpleNameAlias(simpleName);
        final StringBuilder sb = new StringBuilder();
        sb.append(buildSelectFrom(simpleName, alias));
        sb.append(buildOrderBy(alias));
        return sb.toString();
    }

    private String buildOrderBy(final char tablePrefix) {
        final StringBuilder builder = new StringBuilder();
        addOrderBy(tablePrefix, builder);
        return builder.toString();
    }

    protected abstract void addOrderBy(char tablePrefix, StringBuilder builder);

    private String buildWhere(final char prefix, final String parameterName) {
        final StringBuilder builder = new StringBuilder(WHERE);
        builder.append(buildCompareField(prefix, parameterName));
        return builder.toString();
    }

    protected String buildCompareField(final char prefix, final String paramName) {
        final StringBuilder builder = new StringBuilder();
        builder.append(prefix).append('.').append(paramName).append("= :").append(paramName).append(NEW_LINE);
        return builder.toString();
    }

    protected void checkArgumentisNotEmpty(String argument) {
        checkObjectIsNotNull(argument, "businessObjectName is null");
        if (argument.isEmpty()) {
            throw new IllegalArgumentException("businessObjectName is empty");
        }
    }

    public String buildSelectFrom(final String simpleName, final char simpleNameAlias) {
        final StringBuilder builder = new StringBuilder();
        buildSelect(simpleNameAlias, builder);
        builder.append(FROM).append(simpleName).append(BLANK_SPACE).append(simpleNameAlias).append(NEW_LINE);
        return builder.toString();
    }

    protected abstract void buildSelect(char simpleNameAlias, StringBuilder builder);
}
