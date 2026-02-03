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
package org.bonitasoft.web.rest.server.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bonitasoft.engine.bdm.serialization.CustomLocalDateDeserializer;
import org.bonitasoft.engine.bdm.serialization.CustomLocalDateSerializer;
import org.bonitasoft.engine.bdm.serialization.CustomLocalDateTimeDeserializer;
import org.bonitasoft.engine.bdm.serialization.CustomLocalDateTimeSerializer;
import org.bonitasoft.engine.bdm.serialization.CustomOffsetDateTimeDeserializer;
import org.bonitasoft.engine.bdm.serialization.CustomOffsetDateTimeSerializer;

/**
 * Provides Jackson module configuration for Bonita REST APIs.
 * <p>
 * This ensures backward compatibility with the Restlet JSON format, including:
 * <ul>
 * <li>_string suffix fields for numeric IDs (for JavaScript precision)</li>
 * <li>Custom date/time serialization</li>
 * </ul>
 * <p>
 * Used by both Spring MVC configuration and test infrastructure.
 */
public final class BonitaJacksonModuleProvider {

    private BonitaJacksonModuleProvider() {
        // Utility class
    }

    /**
     * Creates a Jackson module with Bonita custom serializers.
     * Same configuration as BonitaJacksonConverter for Restlet.
     *
     * @return the configured Jackson module
     */
    public static Module createBonitaModule() {
        SimpleModule bonitaModule = new SimpleModule("BonitaModule");

        // Add custom serializers for _string suffix fields
        bonitaModule.addSerializer(new DataInstanceSerializer());
        bonitaModule.addSerializer(new TimerEventTriggerInstanceSerializer());

        // Date/time serializers and deserializers
        bonitaModule.addDeserializer(LocalDate.class, new CustomLocalDateDeserializer());
        bonitaModule.addDeserializer(LocalDateTime.class, new CustomLocalDateTimeDeserializer());
        bonitaModule.addDeserializer(OffsetDateTime.class, new CustomOffsetDateTimeDeserializer());
        bonitaModule.addSerializer(LocalDate.class, new CustomLocalDateSerializer());
        bonitaModule.addSerializer(LocalDateTime.class, new CustomLocalDateTimeSerializer());
        bonitaModule.addSerializer(OffsetDateTime.class, new CustomOffsetDateTimeSerializer());

        return bonitaModule;
    }

    /**
     * Configures the given ObjectMapper with Bonita custom serializers.
     *
     * @param mapper the ObjectMapper to configure
     */
    public static void configureObjectMapper(ObjectMapper mapper) {
        mapper.registerModule(createBonitaModule());
    }
}
