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

import org.bonitasoft.engine.page.SPageBuilder;
import org.bonitasoft.engine.page.SPageBuilderFactory;

/**
 * @author Baptiste Mesta
 */
public class SPageBuilderFactoryImpl implements SPageBuilderFactory {

    @Override
    public SPageBuilder createNewInstance(final String name, final String description, final String displayName, final long installationDate,
            final long installedBy, final boolean provided, final String contentName) {
        return new SPageBuilderImpl(new SPageImpl(name, description, displayName, installationDate, installedBy, provided, installationDate, installedBy,
                contentName));
    }

    @Override
    public SPageBuilder createNewInstance(final String name, final long installationDate, final int installedBy, final boolean provided,
            final String contentName) {
        return new SPageBuilderImpl(new SPageImpl(name, installationDate, installedBy, provided, contentName));
    }

    @Override
    public String getNameKey() {
        return "name";
    }

    @Override
    public String getDescriptionKey() {
        return "description";
    }

    @Override
    public String getInstallationDateKey() {
        return "installationDate";
    }

    @Override
    public String getInstalledByKey() {
        return "installedBy";
    }

    @Override
    public String getContentTypeKey() {
        return "contentType";
    }

    @Override
    public String getProcessDefinitionIdKey() {
        return "processDefinitionId";
    }

    @Override
    public String getIdKey() {
        return "id";
    }

    @Override
    public String getProvidedKey() {
        return "provided";
    }

    @Override
    public String getDisplayNameKey() {
        return "displayName";
    }

    @Override
    public String getLastModificationDateKey() {
        return "lastModificationDate";
    }

    @Override
    public String getLastUpdatedByKey() {
        return "lastUpdateBy";
    }

}
