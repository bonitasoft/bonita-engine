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
package org.bonitasoft.web.rest.server.api.page.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.io.FileContent;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.web.rest.model.portal.page.PageItem;
import org.bonitasoft.web.rest.server.datastore.page.PageDatastore;

/**
 * @author Fabio Lombardi
 */
public class PageItemBuilder {

    protected long id = 1L;

    protected String urlToken = "custompage_aName";

    protected String displayName = "aDisplayName";

    protected String description = "aDescription";

    protected boolean isProvided = false;

    protected Date creation_date = new Date(1);

    protected long createdBy = 1L;

    protected Date last_update_date = new Date(1);

    protected String contentName = "page.zip";

    protected long updatedBy = 1L;

    public static PageItemBuilder aPageItem() {
        return new PageItemBuilder();
    }

    public PageItem build() throws IOException, URISyntaxException, ServerAPIException, BonitaHomeNotSetException,
            UnknownAPITypeException {
        final PageItem item = new PageItem();
        item.setId(id);
        item.setUrlToken(urlToken);
        item.setDisplayName(displayName);
        item.setDescription(description);
        item.setIsProvided(isProvided);
        item.setCreationDate(creation_date);
        item.setCreatedByUserId(createdBy);
        item.setLastUpdateDate(last_update_date);
        item.setUpdatedByUserId(updatedBy);
        item.setContentName(contentName);

        String zipFileName = "/page.zip";
        final URL zipFileUrl = getClass().getResource(zipFileName);
        final File zipFile = new File(zipFileUrl.toURI());
        //store page zip into database
        String pageZipKey = PlatformAPIAccessor.getTemporaryContentAPI()
                .storeTempFile(new FileContent(zipFile.getName(), new FileInputStream(zipFile), "application/zip"));

        item.setAttribute(PageDatastore.UNMAPPED_ATTRIBUTE_ZIP_FILE, pageZipKey);
        return item;
    }

    public PageItemBuilder fromEngineItem(final Page page) {
        id = page.getId();
        urlToken = page.getName();
        displayName = page.getDisplayName();
        description = page.getDescription();
        isProvided = page.isProvided();
        creation_date = page.getInstallationDate();
        createdBy = page.getInstalledBy();
        last_update_date = page.getLastModificationDate();
        updatedBy = page.getLastUpdatedBy();
        contentName = page.getContentName();

        return this;
    }

}
