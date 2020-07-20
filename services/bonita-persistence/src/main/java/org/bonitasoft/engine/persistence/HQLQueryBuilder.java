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

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.engine.query.spi.NamedParameterDescriptor;
import org.hibernate.engine.query.spi.ParameterMetadata;
import org.hibernate.internal.QueryImpl;
import org.hibernate.type.CustomType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Baptiste Mesta
 */

public class HQLQueryBuilder extends QueryBuilder {

    private static final Logger logger = LoggerFactory.getLogger(HQLQueryBuilder.class);

    HQLQueryBuilder(String baseQuery, OrderByBuilder orderByBuilder, Map<String, String> classAliasMappings,
            char likeEscapeCharacter) {
        super(baseQuery, orderByBuilder, classAliasMappings, likeEscapeCharacter);
    }

    Query buildQuery(Session session) {
        Query query = session.createQuery(stringQueryBuilder.toString());
        ParameterMetadata parameterMetadata = ((QueryImpl) query).getParameterMetadata();
        for (Map.Entry<String, Object> parameter : getQueryParameters().entrySet()) {
            if (parameter.getValue() instanceof Collection) {
                query.setParameterList(parameter.getKey(), (Collection) parameter.getValue());
            } else {
                query.setParameter(parameter.getKey(), convertParameterValueToFieldType(parameter, parameterMetadata));
            }
        }
        return query;
    }

    private Object convertParameterValueToFieldType(Map.Entry<String, Object> parameter,
            ParameterMetadata parameterMetadata) {
        NamedParameterDescriptor namedParameterDescriptor = parameterMetadata
                .getNamedParameterDescriptor(parameter.getKey());
        Object convertedParameterValue = parameter.getValue();
        if (convertedParameterValue != null) {
            String parameterValueType = convertedParameterValue.getClass().getSimpleName();
            String expectedType = namedParameterDescriptor.getExpectedType().getName();
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
                    case "org.bonitasoft.engine.persistence.GenericEnumUserType":
                        convertedParameterValue = convertToEnum(parameterValueType.toLowerCase(),
                                convertedParameterValue,
                                ((GenericEnumUserType) ((CustomType) namedParameterDescriptor.getExpectedType())
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
            throw new IllegalArgumentException("Unsupported type " + parameterValueType + ", cannot convert ");
        }
    }

    private String convertToString(String parameterValueType, Object parameterValue) {
        if (parameterValueType.equals("long")) {
            return String.valueOf(parameterValue);
        } else if (parameterValueType.equals("integer")) {
            return String.valueOf(parameterValue);
        } else {
            throw new IllegalArgumentException("Unsupported type " + parameterValueType + ", cannot convert ");
        }
    }

    @Override
    public void setTenantId(Query query, long tenantId) {
        //set using filters
    }
}
