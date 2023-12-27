/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.toolkit.client.common.json;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.bonitasoft.web.toolkit.client.common.exception.http.JsonExceptionSerializer;

/**
 * @author Séverin Moussel
 */
public class JSonSerializer extends JSonUtil {

    // Thread local as recommended in the javadoc
    private static final ThreadLocal<SimpleDateFormat> dateTimeFormat = ThreadLocal
            .withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));

    public static String serialize(final JsonSerializable object) {
        return serializeInternal(object).toString();
    }

    private static StringBuilder serializeInternal(JsonSerializable object) {
        if (object == null) {
            return new StringBuilder("null");
        }
        return new StringBuilder(object.toJson());
    }

    public static String serialize(final Object object) {
        return serializeInternal(object).toString();
    }

    private static StringBuilder serializeInternal(Object object) {
        if (object == null) {
            return new StringBuilder("null");
        } else if (object instanceof JsonSerializable) {
            return serializeInternal((JsonSerializable) object);
        } else if (object instanceof Collection<?>) {
            return serializeCollectionInternal((Collection<?>) object);
        } else if (object instanceof Map<?, ?>) {
            return serializeMapInternal((Map<?, ?>) object);
        } else if (object instanceof Number) {
            return new StringBuilder(object.toString());
        } else if (object instanceof Boolean) {
            return new StringBuilder((Boolean) object ? "true" : "false");
        } else if (object instanceof Date) {
            return quoteInternal(dateTimeFormat.get().format((Date) object));
        } else if (object instanceof Throwable) {
            return new StringBuilder(serializeException((Throwable) object));
        }

        return quoteInternal(object.toString());
    }

    public static String serializeCollection(final Collection<?> list) {
        return serializeCollectionInternal(list).toString();
    }

    private static StringBuilder serializeCollectionInternal(Collection<?> list) {
        final StringBuilder json = new StringBuilder("[");

        boolean first = true;
        for (final Object item : list) {
            json.append(!first ? "," : "").append(serializeInternal(item));
            first = false;
        }

        json.append("]");

        return json;
    }

    public static String serializeMap(final Map<?, ?> map) {
        return serializeMapInternal(map).toString();
    }

    private static StringBuilder serializeMapInternal(Map<?, ?> map) {
        final StringBuilder json = new StringBuilder().append("{");

        boolean first = true;
        for (final Object key : map.keySet()) {
            json.append(!first ? "," : "").append(quoteInternal(key.toString())).append(":")
                    .append(serializeInternal(map.get(key)));
            first = false;
        }

        json.append("}");

        return json;
    }

    public static String serializeException(final Throwable e) {
        return new JsonExceptionSerializer(e).end();
    }

    public static String serializeStringMap(final Map<?, String> map) {
        return serializeStringMapInternal(map).toString();
    }

    private static StringBuilder serializeStringMapInternal(Map<?, String> map) {
        final StringBuilder json = new StringBuilder("{");

        boolean first = true;
        for (final Object key : map.keySet()) {
            json.append(!first ? "," : "").append(quoteInternal(key.toString())).append(":")
                    .append(quoteInternal(map.get(key)));
            first = false;
        }

        json.append("}");

        return json;
    }

}
