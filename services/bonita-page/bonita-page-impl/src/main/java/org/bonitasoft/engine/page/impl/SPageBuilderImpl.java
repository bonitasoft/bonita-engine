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
package org.bonitasoft.engine.page.impl;

import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.SPageBuilder;

/**
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public class SPageBuilderImpl implements SPageBuilder {

    private final SPageImpl page;

    public SPageBuilderImpl(final SPageImpl page) {
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
        page.setProcessDefinitionId(processDefinitionId);
        return this;
    }

    @Override
    public SPage done() {
        return page;
    }

}
