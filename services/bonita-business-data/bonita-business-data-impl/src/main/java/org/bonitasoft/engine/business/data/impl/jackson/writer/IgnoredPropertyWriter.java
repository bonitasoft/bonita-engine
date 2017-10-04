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

import java.util.Objects;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.business.data.impl.jackson.utils.Link;
import org.bonitasoft.engine.business.data.impl.jackson.utils.LinkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;

/**
 * This implementation does not serialize fields by calling the JsonGenerator. Instead, it creates and stores link
 * instances in the Jackson per-call context.<br>
 * The actual serialization is delegated to the {@link LinkPropertyWriter} class.
 */
public class IgnoredPropertyWriter extends BeanPropertyWriter {

    private static Logger LOG = LoggerFactory.getLogger(IgnoredPropertyWriter.class);

    private final String ignoredFieldName;

    public IgnoredPropertyWriter(BeanPropertyWriter base) {
        super(base);
        this.ignoredFieldName = base.getName();
    }

    @Override
    public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
        LOG.trace("Managing ignored field {} with value {}", ignoredFieldName, bean);

        Object currentlyProcessedObject = getCurrentObjectFromContext(gen);
        if (currentlyProcessedObject instanceof Entity) {
            LOG.trace("Parent is an Entity, so managing links");
            String uriPattern = LinkUtils.getUriPatternFromContext(prov);
            LOG.trace("URI Pattern retrieved from the context: {}", uriPattern);

            Link link = new Link(ignoredFieldName, buildURI((Entity) currentlyProcessedObject, uriPattern, ignoredFieldName));
            LinkUtils.addLinkToContext(currentlyProcessedObject, link, prov);
        }
    }

    private static Object getCurrentObjectFromContext(JsonGenerator gen) {
        JsonStreamContext outputContext = gen.getOutputContext();
        Object currentObj = outputContext.getCurrentValue();
        LOG.trace("Current Object from context: {}", currentObj);
        return currentObj;
    }

    private static String buildURI(final Entity parent, final String uriPattern, final String fieldName) {
        String uri = uriPattern.replace("{className}", parent.getClass().getName());
        uri = uri.replace("{id}", Objects.toString(parent.getPersistenceId(), null));
        return uri.replace("{field}", fieldName);
    }

}
