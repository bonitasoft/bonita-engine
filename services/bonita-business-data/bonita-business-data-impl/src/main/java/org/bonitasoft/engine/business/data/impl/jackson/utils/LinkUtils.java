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
package org.bonitasoft.engine.business.data.impl.jackson.utils;

import java.util.LinkedHashSet;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkUtils {

    private static Logger LOG = LoggerFactory.getLogger(LinkUtils.class);

    private static final String ATTRIBUTE_KEY_LINK_BASE_NAME = "IgnoredToLinkSerializer$$ATTRIBUTE_KEY_LINK";
    private static final String ATTRIBUTE_KEY_URI_PATTERN = "IgnoredToLinkSerializer$$PATTERN_URI";

    public static LinkedHashSet<Link> getLinksFromContext(Object value, SerializerProvider prov) {
        LOG.trace("Retrieving links from context");
        String attributeKeyLink = getAttributeKeyLink(value);
        LinkedHashSet<Link> links = (LinkedHashSet<Link>) prov.getAttribute(attributeKeyLink);
        if (links == null) {
            LOG.trace("No found links, initialize them in the context");
            links = new LinkedHashSet<>();
            prov.setAttribute(attributeKeyLink, links);
        }
        LOG.trace("Links: {}", links);
        return links;
    }

    public static void addLinkToContext(Object value, Link link, SerializerProvider prov) {
        LinkedHashSet<Link> links = getLinksFromContext(value, prov);
        links.add(link);
        LOG.trace("Added to context: {}", link);
    }

    private static String getAttributeKeyLink(Object value) {
        return ATTRIBUTE_KEY_LINK_BASE_NAME + "$$" + value.getClass().getCanonicalName() + "@" + value.hashCode();
    }

    public static String getUriPatternFromContext(SerializerProvider prov) {
        return (String) prov.getAttribute(ATTRIBUTE_KEY_URI_PATTERN);
    }

    public static ObjectWriter putUriPatternIntoContext(ObjectWriter writer, String uriPattern) {
        return writer.withAttribute(ATTRIBUTE_KEY_URI_PATTERN, uriPattern);
    }
}
