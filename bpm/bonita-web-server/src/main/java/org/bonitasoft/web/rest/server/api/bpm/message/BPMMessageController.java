/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.bpm.message;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.bonitasoft.engine.bpm.flownode.SendEventException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.web.rest.server.api.AbstractRESTController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/API/bpm/message")
public class BPMMessageController extends AbstractRESTController {

    private static final Set<String> SUPPORTED_TYPES = new HashSet<>();
    static {
        SUPPORTED_TYPES.add(String.class.getName());
        SUPPORTED_TYPES.add(Integer.class.getName());
        SUPPORTED_TYPES.add(Long.class.getName());
        SUPPORTED_TYPES.add(Float.class.getName());
        SUPPORTED_TYPES.add(Double.class.getName());
        SUPPORTED_TYPES.add(Boolean.class.getName());
        SUPPORTED_TYPES.add(Date.class.getName());
        SUPPORTED_TYPES.add(LocalDate.class.getName());
        SUPPORTED_TYPES.add(LocalDateTime.class.getName());
        SUPPORTED_TYPES.add(OffsetDateTime.class.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendMessage(@RequestBody BPMMessage message, HttpSession httpSession)
            throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException,
            SendEventException, InvalidExpressionException {
        validateMandatoryAttributes(message);

        Map<Expression, Expression> msgContent = new HashMap<>();
        if (message.messageContent() != null) {
            for (Map.Entry<String, BPMMessageValue> entry : message.messageContent().entrySet()) {
                msgContent.put(new ExpressionBuilder().createConstantStringExpression(entry.getKey()),
                        getExpressionFromObject(entry));
            }
        }
        Map<Expression, Expression> correlations = new HashMap<>();
        if (message.correlations() != null) {
            int nbCorrelations = message.correlations().size();
            if (nbCorrelations > 5) {
                throw new IllegalArgumentException(
                        String.format("A maximum of 5 correlations is supported. %s found.", nbCorrelations));
            }
            for (Map.Entry<String, BPMMessageValue> entry : message.correlations().entrySet()) {
                correlations.put(new ExpressionBuilder().createConstantStringExpression(entry.getKey()),
                        getExpressionFromObject(entry));
            }
        }

        Expression targetFlowNodeExpression = null;
        if (message.targetFlowNode() != null) {
            targetFlowNodeExpression = new ExpressionBuilder()
                    .createConstantStringExpression(message.targetFlowNode());
        }
        getProcessAPI(httpSession).sendMessage(message.messageName(),
                new ExpressionBuilder().createConstantStringExpression(message.targetProcess()),
                targetFlowNodeExpression,
                msgContent,
                correlations);
    }

    private Expression getExpressionFromObject(Entry<String, BPMMessageValue> entry)
            throws InvalidExpressionException {
        BPMMessageValue messageValue = entry.getValue();
        if (messageValue == null) {
            throw new IllegalArgumentException(String.format("%s value cannot be null.", entry.getKey()));
        }
        Object value = messageValue.getValue();
        if (value == null) {
            throw new IllegalArgumentException(String.format("%s value cannot be null.", entry.getKey()));
        }
        String type = valueType(messageValue.getType(), value);
        if (!isSupportedType(type)) {
            throw new InvalidExpressionException(
                    String.format(
                            "BPM send message: unsupported value type '%s' for key '%s'. Only primitive types are supported.",
                            messageValue.getType(),
                            entry.getKey()));
        }
        String stringValue = String.valueOf(value);
        String expressionName = stringValue.trim().isEmpty() ? "empty-value" : stringValue;
        return new ExpressionBuilder().createExpression(expressionName,
                stringValue, valueType(type, value), ExpressionType.TYPE_CONSTANT);
    }

    private String valueType(String type, Object value) {
        if (type != null) {
            return type;
        }
        return guessType(value);
    }

    private String guessType(Object value) {
        if (value instanceof String) {
            try {
                LocalDate.parse((String) value);
                return LocalDate.class.getName();
            } catch (DateTimeParseException e) {
                //Ignore
            }
            try {
                LocalDateTime.parse((String) value);
                return LocalDateTime.class.getName();
            } catch (DateTimeParseException e) {
                //Ignore
            }
            try {
                OffsetDateTime.parse((String) value);
                return OffsetDateTime.class.getName();
            } catch (DateTimeParseException e) {
                //Ignore
            }
            return String.class.getName();
        } else if (value instanceof Long) {
            return Long.class.getName();
        } else if (value instanceof Double) {
            return Double.class.getName();
        } else if (value instanceof Float) {
            return Float.class.getName();
        } else if (value instanceof Integer) {
            return Integer.class.getName();
        } else if (value instanceof Boolean) {
            return Boolean.class.getName();
        }
        return null;
    }

    private boolean isSupportedType(String type) {
        return SUPPORTED_TYPES.contains(type);
    }

    private void validateMandatoryAttributes(BPMMessage message) {
        if (message.messageName() == null) {
            throw new IllegalArgumentException("'messageName' attribute is mandatory");
        }
        if (message.targetProcess() == null) {
            throw new IllegalArgumentException("'targetProcess' attribute is mandatory");
        }
    }
}
