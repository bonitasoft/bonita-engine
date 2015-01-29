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
 **/
package org.bonitasoft.engine.api.impl.transaction.page;

import java.util.List;

import org.bonitasoft.engine.api.impl.converter.PageModelConverter;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;

/**
 * @author Baptiste Mesta
 */
public class SearchPages extends AbstractSearchEntity<Page, SPage> {

    private final PageService pageService;

    public SearchPages(final PageService pageService, final SearchEntityDescriptor searchDescriptor, final SearchOptions options) {
        super(searchDescriptor, options);
        this.pageService = pageService;
    }

    @Override
    public long executeCount(final QueryOptions queryOptions) throws SBonitaReadException {
        try {
            return pageService.getNumberOfPages(queryOptions);
        } catch (final SBonitaReadException e) {
            throw new SBonitaReadException(e);
        }
    }

    @Override
    public List<SPage> executeSearch(final QueryOptions queryOptions) throws SBonitaReadException {
        return pageService.searchPages(queryOptions);
    }

    @Override
    public List<Page> convertToClientObjects(final List<SPage> pages) {
        return new PageModelConverter().toPages(pages);
    }

}
