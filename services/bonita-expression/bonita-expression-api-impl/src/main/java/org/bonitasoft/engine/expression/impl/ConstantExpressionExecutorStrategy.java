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
package org.bonitasoft.engine.expression.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.ExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Zhao na
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class ConstantExpressionExecutorStrategy implements ExpressionExecutorStrategy {

    private static final String REGEX_PARSE_DATE = "(\\d{4})(-([01]\\d)((-([0-3]\\d)(T(\\d\\d)\\:(\\d\\d)(((\\:(\\d\\d))?(\\.(\\d\\d))?(([\\+-])(\\d\\d)\\:(\\d\\d))?)?)?)?)?)?)?";

    @Override
    public void validate(final SExpression expression) throws SInvalidExpressionException {
        if ("".equals(expression.getContent().trim())) {
            throw new SInvalidExpressionException("The expresssion content cannot be empty. Expression : " + expression, expression.getName());
        }
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_CONSTANT;
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionEvaluationException {
        final String expressionContent = expression.getContent();
        Serializable result;
        final String returnType = expression.getReturnType();
        // here need to improve
        try {
            if (Boolean.class.getName().equals(returnType)) {
                result = Boolean.parseBoolean(expressionContent);
            } else if (Long.class.getName().equals(returnType)) {
                result = Long.parseLong(expressionContent);
            } else if (Double.class.getName().equals(returnType)) {
                result = Double.parseDouble(expressionContent);
            } else if (Float.class.getName().equals(returnType)) {
                result = Float.parseFloat(expressionContent);
            } else if (Integer.class.getName().equals(returnType)) {
                result = Integer.parseInt(expressionContent);
            } else if (String.class.getName().equals(returnType)) {
                result = expressionContent;
            } else if (Date.class.getName().equals(returnType)) { // "2013-01-02T02:42:12.17+02:00"
                result = parseDate(expressionContent);
            } else {
                throw new SExpressionEvaluationException("Unknown return type: " + returnType + " for expression " + expression.getName() + " : "
                        + expressionContent, expression.getName());
            }
        } catch (final NumberFormatException e) {
            throw new SExpressionEvaluationException("The content of the expression \"" + expression.getName() + "\" is not a number :" + expressionContent, e,
                    expression.getName());
        }
        return result;
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionEvaluationException {
        final List<Object> list = new ArrayList<Object>(expressions.size());
        for (final SExpression expression : expressions) {
            list.add(evaluate(expression, context, resolvedExpressions, containerState));
        }
        return list;
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        return false;
    }

    /**
     * @param dateToParse
     * @return null if not a Date, new Date with properties is ISO format is recognized
     */
    private Date parseDate(final String dateToParse) {
        if (dateToParse.matches(REGEX_PARSE_DATE)) {
            final Calendar calendar = Calendar.getInstance();
            final String year = dateToParse.replaceFirst(REGEX_PARSE_DATE, "$1");
            if (year != null && !year.isEmpty() && Integer.valueOf(year) > 1900) {
                calendar.set(Calendar.YEAR, Integer.valueOf(year));
            }

            final String month = dateToParse.replaceFirst(REGEX_PARSE_DATE, "$3");
            if (month != null && !month.isEmpty() && Integer.valueOf(month) < 13) {
                // MONTH value from 0 to 11
                calendar.set(Calendar.MONTH, Integer.valueOf(month) - 1);
            }

            final String day = dateToParse.replaceFirst(REGEX_PARSE_DATE, "$6");
            if (day != null && !day.isEmpty() && Integer.valueOf(day) < 32) {
                calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(day));
            }

            final String hour = dateToParse.replaceFirst(REGEX_PARSE_DATE, "$8");
            if (hour != null && !hour.isEmpty() && Integer.valueOf(hour) < 24) {
                calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hour));
            }

            final String minutes = dateToParse.replaceFirst(REGEX_PARSE_DATE, "$9");
            if (minutes != null && !minutes.isEmpty() && Integer.valueOf(minutes) < 60) {
                calendar.set(Calendar.MINUTE, Integer.valueOf(minutes));
            }

            final String secondes = dateToParse.replaceFirst(REGEX_PARSE_DATE, "$13");
            if (secondes != null && !secondes.isEmpty() && Integer.valueOf(secondes) < 60) {
                calendar.set(Calendar.SECOND, Integer.valueOf(secondes));
            }

            final String fractional = dateToParse.replaceFirst(REGEX_PARSE_DATE, "$15");
            if (fractional != null && !fractional.isEmpty() && Integer.valueOf(fractional) < 60) {
                calendar.set(Calendar.MILLISECOND, Integer.valueOf(fractional));
            }

            final String tzSign = dateToParse.replaceFirst(REGEX_PARSE_DATE, "$17");
            final String tzHour = dateToParse.replaceFirst(REGEX_PARSE_DATE, "$18");
            final String tzMinutes = dateToParse.replaceFirst(REGEX_PARSE_DATE, "$19");
            final TimeZone tz = TimeZone.getTimeZone("GMT" + tzSign + tzHour + tzMinutes);
            if (!tzSign.isEmpty() && !tzHour.isEmpty() && !tzMinutes.isEmpty() && tz != null) {
                calendar.setTimeZone(tz);
            }

            return calendar.getTime();
        }
        return null;
    }

}
