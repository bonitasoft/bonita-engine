package com.bonitasoft.engine.api.impl;

import java.util.List;

import org.bonitasoft.engine.api.impl.SessionInfos;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.api.PageAPI;
import com.bonitasoft.engine.api.impl.transaction.page.AddPage;
import com.bonitasoft.engine.api.impl.transaction.reporting.GetReport;
import com.bonitasoft.engine.page.Page;
import com.bonitasoft.engine.page.PageCreator;
import com.bonitasoft.engine.page.PageNotFoundException;
import com.bonitasoft.engine.page.PageService;
import com.bonitasoft.engine.page.SPage;
import com.bonitasoft.engine.service.SPModelConvertor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;

public class PageAPIExt implements PageAPI {

    @Override
    public Page getPage(final long pageId) throws PageNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] getPageContent(final long pageId) throws PageNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SearchResult<Page> searchPages(final SearchOptions searchOptions) throws SearchException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Page createPage(final PageCreator pageCreator, final byte[] content) throws AlreadyExistsException, CreationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final long userId = SessionInfos.getUserIdFromSession();
        PageService pageService = tenantAccessor.getPageService();
        final SPage sPage = SPModelConvertor.constructSPage(pageCreator, userId);
        final AddPage addPage = new AddPage(pageService, sPage, content);
        checkPageAlreadyExists((String) pageCreator.getFields().get(PageCreator.PageField.NAME), tenantAccessor);
        try {
            addPage.execute();
            return SPModelConvertor.toPage(addPage.getResult());
        } catch (final SBonitaException sbe) {
            throw new CreationException(sbe);
        }
    }

    @Override
    public void deletePage(final long pageId) throws DeletionException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deletePages(final List<Long> pageIds) throws DeletionException {
        // TODO Auto-generated method stub

    }

    private static TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    protected void checkPageAlreadyExists(final String name, final TenantServiceAccessor tenantAccessor) throws AlreadyExistsException {
        // Check if the problem is primary key duplication:
        try {
            final GetReport getReport = new GetReport(tenantAccessor, name);

            getReport.execute();
            if (getReport.getResult() != null) {
                throw new AlreadyExistsException("A report already exists with the name " + name);
            }
        } catch (SBonitaException e) {
            // ignore it
        }
    }

}
