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
 */
package org.bonitasoft.engine.page.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.bonitasoft.engine.commons.exceptions.SDeletionException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.page.PageMappingService;
import org.bonitasoft.engine.page.PageServiceListener;
import org.bonitasoft.engine.page.SContentType;
import org.bonitasoft.engine.page.SInvalidPageZipMissingPropertiesException;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Laurent Leseigneur
 * @author Matthieu Chaffotte
 */
public class ApiExtensionPageServiceListenerImpl implements PageServiceListener {

    private static final int MAX_RESULTS = 100;

    private final PageMappingService pageMappingService;

    private final SPageContentHelper helper;

    public ApiExtensionPageServiceListenerImpl(final PageMappingService pageMappingService) {
        this(pageMappingService, new SPageContentHelper());
    }

    public ApiExtensionPageServiceListenerImpl(final PageMappingService pageMappingService, final SPageContentHelper helper) {
        super();
        this.pageMappingService = pageMappingService;
        this.helper = helper;
    }

    @Override
    public void pageInserted(final SPage sPage, final byte[] content) throws SObjectCreationException {
        if (SContentType.API_EXTENSION.equals(sPage.getContentType())){
            try {
                addPageMapping(sPage, helper.loadPageProperties(content));
            } catch (final IOException | SInvalidPageZipMissingPropertiesException e) {
                throw new SObjectCreationException(e);
            }
        }
    }

    private void addPageMapping(final SPage page, final Properties apiProperties) throws SObjectCreationException, IOException,
            SInvalidPageZipMissingPropertiesException {
        final List<String> mappings = getKeysOfPageMappings(apiProperties);
        for (final String mapping : mappings) {
            pageMappingService.create(mapping, page.getId(), Collections.<String> emptyList());
        }
    }

    private String getMappingKey(final String method, final String pathTemplate) {
        return new StringBuilder().append("apiExtension|").append(method).append("|").append(pathTemplate).toString();
    }

    private String getRequiredProperty(final Properties properties, final String propertyName) throws SObjectCreationException {
        final String property = (String) properties.get(propertyName);
        if (property == null || property.trim().length() == 0) {
            throw new SObjectCreationException("the property '" + propertyName + "' is missing or is empty");
        }
        return property.trim();
    }

    @Override
    public void pageDeleted(final SPage page) throws SBonitaReadException, SDeletionException {
        if (SContentType.API_EXTENSION.equals(page.getContentType())) {
            List<SPageMapping> mappings;
            do {
                mappings = pageMappingService.get(page.getId(), 0, MAX_RESULTS);
                for (final SPageMapping mapping : mappings) {
                    pageMappingService.delete(mapping);
                }
            } while (mappings.size() == MAX_RESULTS);
        }
    }

    @Override
    public void pageUpdated(final SPage page, final byte[] content) throws SObjectModificationException {
        if (SContentType.API_EXTENSION.equals(page.getContentType())) {
            try {
                updateMappings(page, helper.loadPageProperties(content));
            } catch (SBonitaReadException | SDeletionException | SObjectCreationException | SInvalidPageZipMissingPropertiesException | IOException e) {
                throw new SObjectModificationException(e);
            }
        }
    }

    private void updateMappings(final SPage page, final Properties apiProperties) throws SObjectCreationException, SBonitaReadException, SDeletionException {
        final List<String> keys = getKeysOfPageMappings(apiProperties);
        final List<String> existingKeys = new ArrayList<>();
        List<SPageMapping> mappings;
        do {
            mappings = pageMappingService.get(page.getId(), 0, MAX_RESULTS);
            for (final SPageMapping mapping : mappings) {
                if (keys.contains(mapping.getKey())) {
                    existingKeys.add(mapping.getKey());
                } else {
                    pageMappingService.delete(mapping);
                }
            }
        } while (mappings.size() == MAX_RESULTS);
        keys.removeAll(existingKeys);
        for (final String key : keys) {
            pageMappingService.create(key, page.getId(), Collections.<String> emptyList());
        }
    }

    private List<String> getKeysOfPageMappings(final Properties apiProperties) throws SObjectCreationException {
        final List<String> keys = new ArrayList<>();
        final String apiExtensions = getRequiredProperty(apiProperties, "apiExtensions");
        final String[] resourceNames = apiExtensions.split(",");
        for (final String resource : resourceNames) {
            final String resourceName = resource.trim();
            final String method = getRequiredProperty(apiProperties, resourceName + ".method");
            final String pathTemplate = getRequiredProperty(apiProperties, resourceName + ".pathTemplate");
            getRequiredProperty(apiProperties, resourceName + ".classFileName");
            getRequiredProperty(apiProperties, resourceName + ".permissions");
            keys.add(getMappingKey(method, pathTemplate));
        }
        return keys;
    }

}
