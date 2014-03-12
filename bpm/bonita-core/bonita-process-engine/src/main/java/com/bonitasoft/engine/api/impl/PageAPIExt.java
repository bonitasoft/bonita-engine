package com.bonitasoft.engine.api.impl;

import java.util.List;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

import com.bonitasoft.engine.api.PageAPI;
import com.bonitasoft.engine.page.Page;
import com.bonitasoft.engine.page.PageCreator;
import com.bonitasoft.engine.page.PageNotFoundException;


public class PageAPIExt implements PageAPI {

    @Override
    public Page getPage(long pageId) throws PageNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] getPageContent(long pageId) throws PageNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SearchResult<Page> searchPages(SearchOptions searchOptions) throws SearchException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Page createPage(PageCreator pageCreator, byte[] content) throws AlreadyExistsException, CreationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deletePage(long pageId) throws DeletionException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deletePages(List<Long> pageIds) throws DeletionException {
        // TODO Auto-generated method stub

    }

}
