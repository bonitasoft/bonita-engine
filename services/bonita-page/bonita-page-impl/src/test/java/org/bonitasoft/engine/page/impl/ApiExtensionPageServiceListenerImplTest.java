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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.bonitasoft.engine.commons.exceptions.SDeletionException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.page.PageMappingService;
import org.bonitasoft.engine.page.SContentType;
import org.bonitasoft.engine.page.SInvalidPageZipMissingPropertiesException;
import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApiExtensionPageServiceListenerImplTest {

    @Mock
    private PageMappingService pageMappingService;

    @Mock
    private SPageContentHelper helper;

    @InjectMocks
    private ApiExtensionPageServiceListenerImpl listener;

    private SPageImpl buildPage(final long id) {
        final SPageImpl page = new SPageImpl();
        page.setId(id);
        page.setContentType(SContentType.API_EXTENSION);
        return page;
    }

    @Test
    public void pageInserted_should_only_care_of_api_extension() throws Exception {
        final SPageImpl page = new SPageImpl();
        page.setId(2L);
        page.setContentType(SContentType.PAGE);
        final byte[] content = new byte[] { 1, 0, 0 };

        listener.pageInserted(page, content);

        verifyZeroInteractions(pageMappingService, helper);
    }

    @Test
    public void pageInserted_should_add_api_resources() throws Exception {
        final long pageId = 1983L;
        final SPageImpl page = buildPage(pageId);
        final byte[] content = new byte[] { 1, 0, 0 };
        final Properties properties = new Properties();
        properties.setProperty("apiExtensions", "employee, address");
        properties.setProperty("employee.method", "GET");
        properties.setProperty("employee.pathTemplate", "employees");
        properties.setProperty("employee.classFileName", "Index.groovy");
        properties.setProperty("address.method", "GET");
        properties.setProperty("address.pathTemplate", "employees/{employeeId}/address");
        properties.setProperty("address.classFileName", "Index1.groovy");
        when(helper.loadPageProperties(content)).thenReturn(properties);

        listener.pageInserted(page, content);

        verify(pageMappingService).create("apiExtension|GET|employees", pageId, Collections.<String> emptyList());
        verify(pageMappingService).create("apiExtension|GET|employees/{employeeId}/address", pageId, Collections.<String> emptyList());
        verifyNoMoreInteractions(pageMappingService);
    }

    @Test(expected = SObjectCreationException.class)
    public void pageInserted_should_throw_an_exception_if_the_file_does_not_exists() throws Exception {
        final SPageImpl page = buildPage(10L);
        final byte[] content = new byte[] { 1, 0, 0 };

        when(helper.loadPageProperties(content)).thenThrow(new SInvalidPageZipMissingPropertiesException());

        listener.pageInserted(page, content);
    }

    @Test(expected = SObjectCreationException.class)
    public void pageInserted_should_throw_an_exception_if_an_io_exception_occurs() throws Exception {
        final SPageImpl page = buildPage(10L);
        final byte[] content = new byte[] { 1, 0, 0 };

        when(helper.loadPageProperties(content)).thenThrow(new IOException());

        listener.pageInserted(page, content);
    }

    @Test(expected = SObjectCreationException.class)
    public void pageInserted_should_throw_an_exception_if_the_resource_path_template_is_missing() throws Exception {
        final long pageId = 1983L;
        final SPageImpl page = buildPage(pageId);
        final byte[] content = new byte[] { 1, 0, 0 };
        final Properties properties = new Properties();
        properties.setProperty("apiExtensions", "employee");
        properties.setProperty("employee.method", "GET");
        properties.setProperty("employee.classFileName", "Index.groovy");
        when(helper.loadPageProperties(content)).thenReturn(properties);

        listener.pageInserted(page, content);
    }

    @Test(expected = SObjectCreationException.class)
    public void pageInserted_should_throw_an_exception_if_the_resource_path_template_is_empty() throws Exception {
        final long pageId = 1983L;
        final SPageImpl page = buildPage(pageId);
        final byte[] content = new byte[] { 1, 0, 0 };
        final Properties properties = new Properties();
        properties.setProperty("apiExtensions", "employee");
        properties.setProperty("employee.method", "GET");
        properties.setProperty("employee.pathTemplate", "   ");
        properties.setProperty("employee.classFileName", "Index.groovy");
        when(helper.loadPageProperties(content)).thenReturn(properties);

        listener.pageInserted(page, content);
    }

    @Test(expected = SObjectCreationException.class)
    public void pageInserted_should_throw_an_exception_if_the_method_is_missing() throws Exception {
        final long pageId = 1983L;
        final SPageImpl page = buildPage(pageId);
        final byte[] content = new byte[] { 1, 0, 0 };
        final Properties properties = new Properties();
        properties.setProperty("apiExtensions", "employee");
        properties.setProperty("employee.pathTemplate", "employees/{id}");
        properties.setProperty("employee.classFileName", "Index.groovy");
        when(helper.loadPageProperties(content)).thenReturn(properties);

        listener.pageInserted(page, content);
    }

    @Test(expected = SObjectCreationException.class)
    public void pageInserted_should_throw_an_exception_if_the_class_file_name_is_missing() throws Exception {
        final long pageId = 1983L;
        final SPageImpl page = buildPage(pageId);
        final byte[] content = new byte[] { 1, 0, 0 };
        final Properties properties = new Properties();
        properties.setProperty("apiExtensions", "employee ");
        properties.setProperty("employee.method", "GET");
        properties.setProperty("employee.pathTemplate", "employees");
        when(helper.loadPageProperties(content)).thenReturn(properties);

        listener.pageInserted(page, content);
    }

    @Test(expected = SObjectCreationException.class)
    public void pageInserted_should_throw_an_exception_if_api_extension_is_missing() throws Exception {
        final long pageId = 1983L;
        final SPageImpl page = buildPage(pageId);
        final byte[] content = new byte[] { 1, 0, 0 };
        final Properties properties = new Properties();
        properties.setProperty("employee.method", "GET");
        properties.setProperty("employee.classFileName", "Index.groovy");
        when(helper.loadPageProperties(content)).thenReturn(properties);

        listener.pageInserted(page, content);
    }

    @Test
    public void pageDeleted_should_only_care_of_api_extension() throws Exception {
        final SPageImpl page = new SPageImpl();
        page.setId(2L);
        page.setContentType(SContentType.PAGE);

        listener.pageDeleted(page);

        verifyZeroInteractions(pageMappingService, helper);
    }

    @Test
    public void pageDeleted_should_delete_all_mappings() throws Exception {
        final long pageId = 10L;
        final SPageImpl page = buildPage(pageId);
        final List<SPageMapping> mappings = buildPageMappings(100);
        final List<SPageMapping> mappings2 = buildPageMappings(53);
        when(pageMappingService.get(pageId, 0, 100)).thenReturn(mappings, mappings2);

        listener.pageDeleted(page);

        verify(pageMappingService, times(153)).delete(any(SPageMapping.class));
    }

    private List<SPageMapping> buildPageMappings(final int numberOfResults) {
        final List<SPageMapping> mappings = new ArrayList<>();
        for (int i = 0; i < numberOfResults; i++) {
            mappings.add(new SPageMappingImpl());
        }
        return mappings;
    }

    @Test(expected = SBonitaReadException.class)
    public void pageDeleted_should_throw_an_exception_when_an_exception_occurs_when_getting_mappings() throws Exception {
        final long pageId = 10L;
        final SPageImpl page = buildPage(pageId);
        when(pageMappingService.get(pageId, 0, 100)).thenThrow(new SBonitaReadException("exception"));

        listener.pageDeleted(page);
    }

    @Test(expected = SDeletionException.class)
    public void pageDeleted_should_throw_an_exception_when_an_exception_occurs_when_deleting_mappings() throws Exception {
        final long pageId = 10L;
        final SPageImpl page = buildPage(pageId);
        final List<SPageMapping> mappings = buildPageMappings(10);
        when(pageMappingService.get(pageId, 0, 100)).thenReturn(mappings);
        doThrow(new SDeletionException()).when(pageMappingService).delete(any(SPageMapping.class));

        listener.pageDeleted(page);
    }

}
