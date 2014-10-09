/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.page;

import java.util.List;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;

import com.bonitasoft.engine.page.Page;
import com.bonitasoft.engine.page.PageService;
import com.bonitasoft.engine.page.SPage;
import com.bonitasoft.engine.service.SPModelConvertor;

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
        } catch (SBonitaReadException e) {
            throw new SBonitaReadException(e);
        }
    }

    @Override
    public List<SPage> executeSearch(final QueryOptions queryOptions) throws SBonitaReadException {
        return pageService.searchPages(queryOptions);
    }

    @Override
    public List<Page> convertToClientObjects(final List<SPage> pages) {
        return SPModelConvertor.toPages(pages);
    }

}
