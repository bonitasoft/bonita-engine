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
package org.bonitasoft.engine.api.impl.converter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.page.PageCreator;
import org.bonitasoft.engine.page.PageUpdater;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.SPageBuilder;
import org.bonitasoft.engine.page.SPageBuilderFactory;
import org.bonitasoft.engine.page.impl.PageImpl;

/**
 * @author Elias Ricken de Medeiros
 */
public class PageModelConverter {

    public SPage constructSPage(final PageCreator pageCreator, final long creatorUserId) {
        final Map<PageCreator.PageField, Serializable> fields = pageCreator.getFields();
        final String name = (String) fields.get(PageCreator.PageField.NAME);
        final String description = (String) fields.get(PageCreator.PageField.DESCRIPTION);
        final String displayName = (String) fields.get(PageCreator.PageField.DISPLAY_NAME);
        final String contentName = (String) fields.get(PageCreator.PageField.CONTENT_NAME);
        final String contentType = (String) fields.get(PageCreator.PageField.CONTENT_TYPE);
        final Long processDefinitionId = (Long) fields.get(PageCreator.PageField.PROCESS_DEFINITION_ID);

        return buildSPage(creatorUserId, name, description, displayName, contentName, contentType, processDefinitionId);
    }

    public SPage constructSPage(final PageUpdater pageUpdater, final long creatorUserId) {
        final Map<PageUpdater.PageUpdateField, Serializable> fields = pageUpdater.getFields();
        final String name = (String) fields.get(PageUpdater.PageUpdateField.NAME);
        final String description = (String) fields.get(PageUpdater.PageUpdateField.DESCRIPTION);
        final String displayName = (String) fields.get(PageUpdater.PageUpdateField.DISPLAY_NAME);
        final String contentName = (String) fields.get(PageUpdater.PageUpdateField.CONTENT_NAME);
        final String contentType = (String) fields.get(PageUpdater.PageUpdateField.CONTENT_TYPE);
        final Long processDefinitionId = (Long) fields.get(PageUpdater.PageUpdateField.PROCESS_DEFINITION_ID);

        return buildSPage(creatorUserId, name, description, displayName, contentName, contentType, processDefinitionId);
    }

    private SPage buildSPage(long creatorUserId, String name, String description, String displayName, String contentName, String contentType,
            Long processDefinitionId) {
        final SPageBuilder newSPageBuilder = BuilderFactory.get(SPageBuilderFactory.class).createNewInstance(name, description, displayName,
                System.currentTimeMillis(), creatorUserId, false, contentName);
        newSPageBuilder.setContentType(contentType);
        newSPageBuilder.setProcessDefinitionId(processDefinitionId);
        return newSPageBuilder.done();
    }

    public Page toPage(final SPage sPage) {
        final PageImpl page = new PageImpl(sPage.getId(), sPage.getName(), sPage.getDisplayName(), sPage.isProvided(), sPage.getDescription(),
                sPage.getInstallationDate(), sPage.getInstalledBy(), sPage.getLastModificationDate(), sPage.getLastUpdatedBy(), sPage.getContentName(),
                sPage.getContentType(), sPage.getProcessDefinitionId());
        return page;
    }

    public List<Page> toPages(final List<SPage> sPages) {
        final List<Page> pages = new ArrayList<Page>(sPages.size());
        for (final SPage sPage : sPages) {
            pages.add(toPage(sPage));
        }
        return pages;
    }

}
