/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.datastore.page;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.console.common.server.page.CustomPageService;
import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;
import org.bonitasoft.console.common.server.servlet.FileUploadServlet;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.console.common.server.utils.UnzipUtil;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.page.PageCreator;
import org.bonitasoft.engine.page.PageSearchDescriptor;
import org.bonitasoft.engine.page.PageUpdater;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.extension.page.PageResourceProvider;
import org.bonitasoft.web.rest.model.portal.page.PageItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
import org.bonitasoft.web.rest.server.datastore.filter.Filters;
import org.bonitasoft.web.rest.server.datastore.utils.SearchOptionsCreator;
import org.bonitasoft.web.rest.server.datastore.utils.Sorts;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasAdd;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasDelete;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasGet;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasSearch;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasUpdate;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Fabio Lombardi, Anthony Birembaut
 */

public class PageDatastore extends CommonDatastore<PageItem, Page>
        implements DatastoreHasAdd<PageItem>, DatastoreHasUpdate<PageItem>,
        DatastoreHasGet<PageItem>, DatastoreHasSearch<PageItem>, DatastoreHasDelete {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PageDatastore.class.getName());

    /**
     * page files
     */
    public static final String UNMAPPED_ATTRIBUTE_ZIP_FILE = "pageZip";

    static final String PAGE_TOKEN_PREFIX = "custompage_";

    protected final WebBonitaConstantsUtils constants;

    protected final PageAPI pageAPI;

    protected final CustomPageService customPageService;

    private final BonitaHomeFolderAccessor tenantFolder;

    private final CustomPageContentValidator pageContentValidator;

    private static boolean hasShownIsHiddenLog = false;

    public PageDatastore(final APISession engineSession, final WebBonitaConstantsUtils constantsValue,
            final PageAPI pageAPI,
            final CustomPageService customPageService,
            final BonitaHomeFolderAccessor tenantFolder) {
        super(engineSession);
        constants = constantsValue;
        this.pageAPI = pageAPI;
        this.customPageService = customPageService;
        this.tenantFolder = tenantFolder;
        this.pageContentValidator = new CustomPageContentValidator();
    }

    @Override
    public PageItem add(final PageItem pageItem) {
        final String zipFileAttribute = pageItem.getAttributeValue(UNMAPPED_ATTRIBUTE_ZIP_FILE);
        final String[] filenames = zipFileAttribute.split(FileUploadServlet.RESPONSE_SEPARATOR);
        final String filename = filenames[0];
        String originalFileName = getOriginalFilename(filenames, filename, pageItem.getAttributes());
        pageItem.setContentName(originalFileName);

        try {
            final APISession engineSession = getEngineSession();
            final File zipFile = tenantFolder.getTempFile(filename);
            final File unzipPageTempFolder = unzipContentFile(zipFile);
            pageContentValidator.validate(unzipPageTempFolder);
            final Page page = createEnginePage(pageItem, zipFile);
            final PageItem addedPage = convertEngineToConsoleItem(page);

            PageResourceProvider pageResourceProvider = customPageService.getPageResourceProvider(page);
            customPageService.writePageToPageDirectory(page, pageResourceProvider, unzipPageTempFolder, engineSession);
            deleteTempDirectory(unzipPageTempFolder);
            return addedPage;
        } catch (final Exception e) {
            throw new APIException(e);
        } finally {
            tenantFolder.removeUploadedTempContent(filename);
        }
    }

    protected File unzipContentFile(final File zipFile) throws InvalidPageZipContentException {
        File unzipPageTempFolder = null;
        try {
            final Random randomGen = new Random();
            final int tempPageFolder = randomGen.nextInt();
            unzipPageTempFolder = new File(constants.getTempFolder(), String.valueOf(tempPageFolder));
            UnzipUtil.unzip(zipFile, unzipPageTempFolder.getPath(), false);
        } catch (final Exception e) {
            deleteTempDirectory(unzipPageTempFolder);
            throw new InvalidPageZipContentException("Unable to unzip the page content.", e);
        }
        return unzipPageTempFolder;
    }

    protected boolean isPageTokenValid(final String urlToken) {
        return urlToken.matches(PAGE_TOKEN_PREFIX + "\\p{Alnum}+");
    }

    protected void deleteTempDirectory(final File unzipPage) {
        try {
            if (unzipPage.isDirectory()) {
                IOUtilDeleteDir(unzipPage);
            }
        } catch (final IOException e) {
            throw new APIException(e);
        }
    }

    protected void IOUtilDeleteDir(final File unzipPage) throws IOException {
        IOUtil.deleteDir(unzipPage);
    }

    protected Page createEnginePage(final PageItem pageItem, final File zipFile)
            throws CreationException, IOException, UpdateException {
        try {
            final byte[] zipContent = readZipFile(zipFile);
            Page page = pageAPI.createPage(pageItem.getContentName(), zipContent);
            if (pageItem.getProcessId() != null) {
                final PageUpdater pageUpdater = new PageUpdater();
                pageUpdater.setProcessDefinitionId(pageItem.getProcessId().toLong());
                if (pageItem.getContentType() != null) {
                    pageUpdater.setContentType(pageItem.getContentType());
                }
                page = pageAPI.updatePage(page.getId(), pageUpdater);
            }
            return page;
        } finally {
            zipFile.delete();
        }
    }

    protected byte[] readZipFile(final File zipFile) throws IOException {
        return FileUtils.readFileToByteArray(zipFile);
    }

    protected PageCreator buildPageCreatorFrom(final PageItem pageItem) {
        final PageCreator pageCreator = new PageCreator(pageItem.getUrlToken(), pageItem.getContentName());
        pageCreator.setDescription(pageItem.getDescription());
        pageCreator.setDisplayName(pageItem.getDisplayName());
        return pageCreator;
    }

    @Override
    public PageItem get(final APIID id) {
        try {
            final Page pageItem = pageAPI.getPage(id.toLong());
            return convertEngineToConsoleItem(pageItem);
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    @Override
    public void delete(final List<APIID> ids) {
        try {
            for (final APIID id : ids) {
                final Page page = pageAPI.getPage(id.toLong());
                final APISession engineSession = getEngineSession();
                PageResourceProvider pageResourceProvider = customPageService.getPageResourceProvider(page);
                customPageService.ensurePageFolderIsUpToDate(engineSession, pageResourceProvider);
                pageAPI.deletePage(id.toLong());
                customPageService.removePageLocally(pageResourceProvider);
            }
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    protected List<Long> APIIdsToLong(final List<APIID> ids) {
        final List<Long> result = new ArrayList<>(ids.size());
        for (final APIID id : ids) {
            result.add(id.toLong());
        }
        return result;
    }

    @Override
    public ItemSearchResult<PageItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        // Build search
        final SearchOptionsCreator creator = makeSearchOptionCreator(page, resultsByPage, search, orders, filters);

        // Run search depending on filters passed
        SearchResult<Page> searchResult;
        try {
            searchResult = runSearch(creator);
            // Convert to ConsoleItems
            return new ItemSearchResult<>(page, resultsByPage, searchResult.getCount(),
                    convertEngineToConsoleItemsList(searchResult.getResult()));
        } catch (final SearchException e) {
            throw new APIException(e);
        }

    }

    protected PageSearchDescriptorConverter getSearchDescriptorConverter() {
        return new PageSearchDescriptorConverter();
    }

    protected SearchOptionsCreator makeSearchOptionCreator(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        if (filters.containsKey(PageItem.ATTRIBUTE_IS_HIDDEN)) {
            if (!hasShownIsHiddenLog && LOGGER.isWarnEnabled()) {
                LOGGER.warn(
                        "Parameter \"isHidden\" for page search is deprecated and will be removed in a future release");
                hasShownIsHiddenLog = true;
            }
            filters.remove(PageItem.ATTRIBUTE_IS_HIDDEN);
        }
        final SearchOptionsCreator searchOptionsCreator = new SearchOptionsCreator(page, resultsByPage, search,
                new Sorts(orders,
                        getSearchDescriptorConverter()),
                new Filters(filters,
                        new PageFilterCreator(getSearchDescriptorConverter())));
        final SearchOptionsBuilder builder = searchOptionsCreator.getBuilder();

        if (filters.containsKey(PageItem.FILTER_CONTENT_TYPE)
                && "processPage".equalsIgnoreCase(filters.get(PageSearchDescriptor.CONTENT_TYPE))) {
            builder.leftParenthesis().filter(PageSearchDescriptor.CONTENT_TYPE, "form")
                    .or().filter(PageSearchDescriptor.CONTENT_TYPE, "page")
                    .rightParenthesis();
        } else {
            addStringFilterToSearchBuilder(filters, builder, PageItem.FILTER_CONTENT_TYPE,
                    PageSearchDescriptor.CONTENT_TYPE);
        }
        return searchOptionsCreator;
    }

    /**
     * @param creator
     * @return
     * @throws SearchException
     */
    protected SearchResult<Page> runSearch(final SearchOptionsCreator creator)
            throws SearchException {
        return pageAPI.searchPages(creator.create());
    }

    @Override
    public PageItem update(final APIID id, final Map<String, String> attributes) {
        String filename = null;
        File zipFile = null;
        try {
            Long pageId = id.toLong();
            Page page = pageAPI.getPage(pageId);
            PageItem updatedPage = null;
            if (attributes.containsKey(PageDatastore.UNMAPPED_ATTRIBUTE_ZIP_FILE)) {
                final String zipFileAttribute = attributes.get(UNMAPPED_ATTRIBUTE_ZIP_FILE);
                if (zipFileAttribute != null && !zipFileAttribute.isEmpty()) {
                    final String[] filenames = zipFileAttribute.split(FileUploadServlet.RESPONSE_SEPARATOR);
                    filename = filenames[0];
                    String originalFileName = getOriginalFilename(filenames, filename, attributes);
                    final APISession engineSession = getEngineSession();
                    zipFile = tenantFolder.getTempFile(filename);
                    final File unzipPageTempFolder = unzipContentFile(zipFile);
                    pageContentValidator.validate(unzipPageTempFolder);
                    try {
                        updatePageContent(page, zipFile);
                        final PageUpdater pageUpdater = new PageUpdater();
                        pageUpdater.setContentName(originalFileName);
                        page = pageAPI.updatePage(pageId, pageUpdater);
                        updatedPage = convertEngineToConsoleItem(page);
                    } finally {
                        PageResourceProvider pageResourceProvider = customPageService.getPageResourceProvider(page);
                        customPageService.writePageToPageDirectory(page, pageResourceProvider, unzipPageTempFolder,
                                engineSession);
                        deleteTempDirectory(unzipPageTempFolder);
                    }
                }
            }
            return updatedPage;
        } catch (final Exception e) {
            throw new APIException(e);
        } finally {
            if (filename != null) {
                tenantFolder.removeUploadedTempContent(filename);
            }
            if (zipFile != null) {
                zipFile.delete();
            }
        }
    }

    protected String getOriginalFilename(final String[] filenames, final String tempFilename,
            final Map<String, String> attributes) {
        String originalFileName;
        if (filenames.length > 1) {
            originalFileName = filenames[1];
        } else {
            originalFileName = attributes.getOrDefault(PageItem.ATTRIBUTE_CONTENT_NAME, tempFilename);
        }
        return originalFileName;
    }

    protected void updatePageContent(final Page page, final File zipFile) throws IOException,
            CompilationFailedException, BonitaException {
        if (zipFile != null) {
            PageResourceProvider pageResourceProvider = customPageService.getPageResourceProvider(page);
            customPageService.ensurePageFolderIsUpToDate(getEngineSession(), pageResourceProvider);
            pageAPI.updatePageContent(page.getId(), FileUtils.readFileToByteArray(zipFile));
        }
        customPageService.removePageLocally(page);
    }

    @Override
    protected PageItem convertEngineToConsoleItem(final Page item) {
        if (item != null) {
            return new PageItemConverter().convert(item);
        }
        return null;
    }

}
