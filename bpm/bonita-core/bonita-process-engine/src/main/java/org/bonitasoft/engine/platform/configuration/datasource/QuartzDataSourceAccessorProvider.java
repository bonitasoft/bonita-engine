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
package org.bonitasoft.engine.platform.configuration.datasource;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * This is a hack to let Quartz access datasource beans from SprigContext
 * Quartz support custom connection providers but not non primitive parameters for them
 */
@Component
public class QuartzDataSourceAccessorProvider {

    private static QuartzDataSourceAccessor INSTANCE;

    public QuartzDataSourceAccessorProvider(@Qualifier("bonitaDataSource") DataSource bonitaDataSource,
            @Qualifier("bonitaNonXaDataSource") DataSource bonitaNonXaDataSource) {
        INSTANCE = new QuartzDataSourceAccessor(bonitaDataSource, bonitaNonXaDataSource);
    }

    public static QuartzDataSourceAccessor getInstance() {
        return INSTANCE;
    }

}
