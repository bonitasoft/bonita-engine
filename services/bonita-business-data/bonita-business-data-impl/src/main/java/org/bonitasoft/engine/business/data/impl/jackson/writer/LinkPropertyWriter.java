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

import java.util.List;

import org.bonitasoft.engine.business.data.impl.jackson.utils.Link;
import org.bonitasoft.engine.business.data.impl.jackson.utils.LinkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter;
import com.fasterxml.jackson.databind.util.Annotations;

public class LinkPropertyWriter extends VirtualBeanPropertyWriter {

    private static Logger LOG = LoggerFactory.getLogger(LinkPropertyWriter.class);

    // Needed by Jackson
    protected LinkPropertyWriter() {
        super();
    }

    private LinkPropertyWriter(BeanPropertyDefinition propDef, Annotations ctxtAnn, JavaType type) {
        super(propDef, ctxtAnn, type);
    }

    @Override
    protected Object value(Object bean, JsonGenerator jgen, SerializerProvider prov) {
        LOG.trace("Post processing links for {}", bean);
        List<Link> links = LinkUtils.getLinksFromContext(bean, prov);
        LOG.trace("Retrieved links: {}", links);
        return links;
    }

    @Override
    public VirtualBeanPropertyWriter withConfig(MapperConfig<?> config,
                                                AnnotatedClass declaringClass, BeanPropertyDefinition propDef,
                                                JavaType type) {
        return new LinkPropertyWriter(propDef, declaringClass.getAnnotations(), type);
    }

}
