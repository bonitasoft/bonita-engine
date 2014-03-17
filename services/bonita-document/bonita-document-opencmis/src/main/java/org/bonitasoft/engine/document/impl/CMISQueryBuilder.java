/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.document.impl;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.bonitasoft.engine.document.DocumentCriterion;
import org.bonitasoft.engine.document.DocumentQueryBuilder;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class CMISQueryBuilder {

    private static final ThreadLocal<SimpleDateFormat> CMIS_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected synchronized SimpleDateFormat initialValue() {
            final SimpleDateFormat gmtTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
            gmtTime.setTimeZone(TimeZone.getTimeZone("GMT"));
            return gmtTime;
        }

    };

    /**
     * Builds a CMIS query from a document query builder
     * 
     * @param builder
     * @return
     */
    public StringBuilder buildQuery(final DocumentQueryBuilder builder) {
        final StringBuilder sQuery = new StringBuilder();
        sQuery.append("SELECT * FROM cmis:document");
        final List<Object> query = builder.getQuery();
        if (!query.isEmpty()) {
            sQuery.append(" WHERE ");
        }
        for (final Object object : query) {
            if (object instanceof DocumentCriterion) {
                final DocumentCriterion criterion = (DocumentCriterion) object;
                switch (criterion.getField()) {
                    case ID:
                        createEqualsOrInClause(sQuery, criterion, "cmis:objectId");
                        break;
                    case FILENAME:
                        createEqualsOrInClause(sQuery, criterion, "cmis:contentStreamFileName");
                        break;
                    case CREATION_DATE:
                        getTimeComparison(sQuery, criterion, "cmis:creationDate");
                        break;
                    case AUTHOR:
                        createEqualsOrInClause(sQuery, criterion, "cmis:createdBy");
                        break;
                    case SERIES_ID:
                        createEqualsOrInClause(sQuery, criterion, "cmis:versionSeriesId");
                        break;
                    case IS_EMPTY:
                        if ((Boolean) criterion.getValue()) {
                            sQuery.append(" cmis:contentStreamLength = 0 ");
                        } else {
                            sQuery.append(" cmis:contentStreamLength > 0 ");
                        }
                        break;
                    default:
                        throw new IllegalStateException();
                }
            } else {
                sQuery.append(" ").append(object).append(" ");
            }
        }

        if (!query.isEmpty()) {
            sQuery.append(" )");
        }
        return sQuery;
    }

    private void createEqualsOrInClause(final StringBuilder whereClause, final DocumentCriterion criterion, final String field) {
        if (criterion.getValues() != null) {
            whereClause.append(" " + field + " IN (");
            final Collection<?> values = criterion.getValues();
            for (final Iterator<?> iterator = values.iterator(); iterator.hasNext();) {
                final Object object2 = iterator.next();
                whereClause.append("'");
                whereClause.append(object2);
                whereClause.append("'");
                if (iterator.hasNext()) {
                    whereClause.append(",");
                }
            }
            whereClause.append(") ");
        } else {
            whereClause.append(" " + field + " = '");
            whereClause.append(criterion.getValue());
            whereClause.append("' ");
        }
    }

    private void getTimeComparison(final StringBuilder whereClause, final DocumentCriterion criterion, final String attribute) {
        final SimpleDateFormat cmisDateFormat = CMIS_DATE_FORMAT.get();
        if (criterion.getValue() != null) {
            whereClause.append(attribute);
            whereClause.append(" = TIMESTAMP '");
            final Date value = (Date) criterion.getValue();
            final String fromDate = cmisDateFormat.format(value);
            whereClause.append(fromDate);
            whereClause.append("' ");
        } else {
            whereClause.append(" (");
            whereClause.append(attribute);
            whereClause.append(" >= TIMESTAMP '");
            final Date from = (Date) criterion.getFrom();
            final String fromDate = cmisDateFormat.format(from);
            whereClause.append(fromDate);
            whereClause.append("' AND ");
            whereClause.append(attribute);
            whereClause.append(" <= TIMESTAMP '");
            final Date to = (Date) criterion.getTo();
            final String toDate = cmisDateFormat.format(to);
            whereClause.append(toDate);
            whereClause.append("') ");
        }
    }

}
