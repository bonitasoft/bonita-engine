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
package org.bonitasoft.engine.page.impl;

import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.SPageBuilder;

/**
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public class SPageBuilderImpl implements SPageBuilder {

    private final SPage page;

    public SPageBuilderImpl(final SPage page) {
        super();
        this.page = page;
    }

    @Override
    public SPageBuilder setDescription(final String description) {
        page.setDescription(description);
        return this;
    }

    @Override
    public SPageBuilder setContentType(String contentType) {
        page.setContentType(contentType);
        return this;
    }

    @Override
    public SPageBuilder setProcessDefinitionId(Long processDefinitionId) {
        if (processDefinitionId == null) {
            page.setProcessDefinitionId(0);
        } else {
            page.setProcessDefinitionId(processDefinitionId);
        }
        return this;
    }

    @Override
    public SPage done() {
        return page;
    }

}
