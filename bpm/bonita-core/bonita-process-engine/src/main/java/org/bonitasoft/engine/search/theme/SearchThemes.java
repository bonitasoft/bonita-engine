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
package org.bonitasoft.engine.search.theme;

import java.util.List;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchThemeDescriptor;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.theme.Theme;
import org.bonitasoft.engine.theme.ThemeService;
import org.bonitasoft.engine.theme.model.STheme;

/**
 * @author Celine Souchet
 */
public class SearchThemes extends AbstractSearchEntity<Theme, STheme> {

    private final ThemeService themeService;

    public SearchThemes(final ThemeService themeService, final SearchThemeDescriptor searchThemeDescriptor, final SearchOptions options) {
        super(searchThemeDescriptor, options);
        this.themeService = themeService;
    }

    @Override
    public long executeCount(final QueryOptions queryOptions) throws SBonitaReadException {
        return themeService.getNumberOfThemes(queryOptions);
    }

    @Override
    public List<STheme> executeSearch(final QueryOptions queryOptions) throws SBonitaReadException {
        return themeService.searchThemes(queryOptions);
    }

    @Override
    public List<Theme> convertToClientObjects(final List<STheme> serverObjects) {
        return ModelConvertor.toThemes(serverObjects);
    }

}
