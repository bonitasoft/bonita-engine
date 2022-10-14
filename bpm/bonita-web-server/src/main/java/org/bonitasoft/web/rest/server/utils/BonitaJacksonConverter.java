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
package org.bonitasoft.web.rest.server.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.restlet.data.MediaType;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;

/**
 * @author Laurent Leseigneur
 */
public class BonitaJacksonConverter extends JacksonConverter {

    @Override
    protected <T> JacksonRepresentation<T> create(MediaType mediaType, T source) {
        ObjectMapper mapper = createMapper();
        JacksonRepresentation jr = new JacksonRepresentation<>(mediaType, source);
        jr.setObjectMapper(mapper);
        return jr;
    }

    @Override
    protected <T> JacksonRepresentation<T> create(Representation source, Class<T> objectClass) {
        ObjectMapper mapper = createMapper();
        JacksonRepresentation jr = new JacksonRepresentation<>(source, objectClass);
        jr.setObjectMapper(mapper);
        return jr;
    }

    private ObjectMapper createMapper() {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        ObjectMapper mapper = new ObjectMapper(jsonFactory);

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(new DataInstanceSerializer());
        simpleModule.addSerializer(new TimerEventTriggerInstanceSerializer());

        mapper.registerModule(simpleModule);
        return mapper;
    }

}
