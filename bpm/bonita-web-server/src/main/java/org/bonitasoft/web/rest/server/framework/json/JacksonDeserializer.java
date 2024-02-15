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
package org.bonitasoft.web.rest.server.framework.json;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n;

/**
 * Jackson ObjectMapper Wrapper to fit our needs
 *
 * @author Colin PUY
 */
public class JacksonDeserializer {

    private final ObjectMapper mapper = new ObjectMapper();

    public <T> T deserialize(String json, Class<T> clazz) {
        return deserialize(json, mapper.getTypeFactory().constructType(clazz));
    }

    public <T> List<T> deserializeList(String json, Class<T> clazz) {
        return deserialize(json, mapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    private <T> T deserialize(String json, JavaType javaType) {
        try {
            return mapper.readValue(json.getBytes(), javaType);
        } catch (JsonParseException e) {
            throw new APIException(AbstractI18n.t_("Can't parse json, non-well formed content"), e);
        } catch (JsonMappingException e) {
            throw new APIException(AbstractI18n.t_("Json can't be mapped to " + javaType.getRawClass().getName()), e);
        } catch (IOException e) {
            // should never appear
            throw new APIException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T convertValue(Object fromValue, Class<?> toValue) {
        return (T) mapper.convertValue(fromValue, toValue);
    }
}
