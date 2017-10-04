/**
 * Copyright (C) 2017 Bonitasoft S.A.
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

package org.bonitasoft.engine.business.data.impl.jackson.writer;

import org.bonitasoft.engine.business.data.impl.jackson.serializer.ExtraPropertyStringListSerializer;
import org.bonitasoft.engine.business.data.impl.jackson.serializer.ExtraPropertyStringSerializer;
import org.bonitasoft.engine.business.data.impl.jackson.utils.ExtraPropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.type.CollectionType;

public class ExtraBeanPropertyWriter extends BeanPropertyWriter {

    private static Logger LOG = LoggerFactory.getLogger(ExtraBeanPropertyWriter.class);

    private ExtraBeanPropertyWriter(BeanPropertyWriter base) {
        super(base, new PropertyName(ExtraPropertyUtils.getExtraPropertyName(base)));
    }

    public static ExtraBeanPropertyWriter newWriter(BeanPropertyWriter base) {
        LOG.trace("Creating new instance");
        JavaType initialJavaType = base.getType();
        LOG.trace("Initial java type: {}", initialJavaType);
        final JsonSerializer serializer;
        if (initialJavaType.getClass().isAssignableFrom(CollectionType.class)) {
            serializer = new ExtraPropertyStringListSerializer();
        } else {
            serializer = new ExtraPropertyStringSerializer();
        }

        ExtraBeanPropertyWriter writer = new ExtraBeanPropertyWriter(base);
        writer.assignSerializer(serializer);
        return writer;
    }

}
