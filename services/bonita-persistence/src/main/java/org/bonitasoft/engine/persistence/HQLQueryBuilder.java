/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.persistence;

import java.util.Collection;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.query.ParameterMetadata;
import org.hibernate.query.Query;
import org.hibernate.query.QueryParameter;
import org.hibernate.type.CustomType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Baptiste Mesta
 */
public class HQLQueryBuilder<T> extends QueryBuilder<T> {

    private static final Logger logger = LoggerFactory.getLogger(HQLQueryBuilder.class);

    HQLQueryBuilder(Session session, Query baseQuery, OrderByBuilder orderByBuilder,
            Map<String, String> classAliasMappings,
            char likeEscapeCharacter,
            boolean wordSearchEnabled,
            OrderByCheckingMode orderByCheckingMode,
            SelectListDescriptor<T> selectDescriptor) {
        super(session, baseQuery, orderByBuilder, classAliasMappings, likeEscapeCharacter, wordSearchEnabled,
                orderByCheckingMode, selectDescriptor);
    }

    @Override
    Query rebuildQuery(AbstractSelectDescriptor<T> selectDescriptor, Session session, Query query) {
        Query generatedQuery = session.createQuery(stringQueryBuilder.toString());
        ParameterMetadata parameterMetadata = generatedQuery.getParameterMetadata();
        for (Map.Entry<String, Object> parameter : getQueryParameters().entrySet()) {
            if (parameter.getValue() instanceof Collection) {
                generatedQuery.setParameterList(parameter.getKey(), (Collection) parameter.getValue());
            } else {
                generatedQuery.setParameter(parameter.getKey(),
                        convertParameterValueToFieldType(parameter, parameterMetadata));
            }
        }
        return generatedQuery;
    }

    private Object convertParameterValueToFieldType(Map.Entry<String, Object> parameter,
            ParameterMetadata parameterMetadata) {
        QueryParameter<Object> namedParameterDescriptor = parameterMetadata.getQueryParameter(parameter.getKey());
        Object convertedParameterValue = parameter.getValue();
        if (convertedParameterValue != null) {
            String parameterValueType = convertedParameterValue.getClass().getSimpleName();
            String expectedType = namedParameterDescriptor.getHibernateType().getName();
            if (!expectedType.equals(parameterValueType.toLowerCase())) {
                logger.debug("Trying to convert from {} to expected type {} ", parameterValueType, expectedType);
                switch (expectedType) {
                    case "long":
                        convertedParameterValue = convertToLong(parameterValueType.toLowerCase(),
                                convertedParameterValue);
                        break;
                    case "string":
                        convertedParameterValue = convertToString(parameterValueType.toLowerCase(),
                                convertedParameterValue);
                        break;
                    case "org.hibernate.type.EnumType":
                        convertedParameterValue = convertToEnum(parameterValueType.toLowerCase(),
                                convertedParameterValue,
                                (((CustomType) namedParameterDescriptor.getHibernateType())
                                        .getUserType()).returnedClass());
                        break;
                    default:
                        logger.debug("Not converting from {} to {}", parameterValueType, expectedType);
                        break;
                }
            }
        }
        return convertedParameterValue;
    }

    private Object convertToEnum(String parameterValueType, Object parameterValue,
            Class<? extends Enum> expectedTypeClass) {
        if (parameterValueType.equals("string")) {
            return Enum.valueOf(expectedTypeClass, (String) parameterValue);
        }
        return parameterValue;
    }

    private Long convertToLong(String parameterValueType, Object parameterValue) {
        if (parameterValueType.equals("string")) {
            return Long.valueOf((String) parameterValue);
        } else if (parameterValueType.equals("integer")) {
            return Long.valueOf((Integer) parameterValue);
        } else {
            throw new IllegalArgumentException(
                    "Unsupported type " + parameterValueType + ", cannot convert it to 'long'.");
        }
    }

    private String convertToString(String parameterValueType, Object parameterValue) {
        if (parameterValueType.equals("long")) {
            return String.valueOf(parameterValue);
        } else if (parameterValueType.equals("integer")) {
            return String.valueOf(parameterValue);
        } else {
            throw new IllegalArgumentException(
                    "Unsupported type " + parameterValueType + ", cannot convert it to 'String'");
        }
    }

    @Override
    protected void addConstantsAsParameters(Query query) {
        // nothing to do, Native queries require to inject constant parameters here
    }
}
