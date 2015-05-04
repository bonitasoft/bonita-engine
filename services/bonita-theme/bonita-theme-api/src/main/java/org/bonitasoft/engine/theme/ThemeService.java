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
package org.bonitasoft.engine.theme;

import java.util.List;

import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.theme.exception.SRestoreThemeException;
import org.bonitasoft.engine.theme.exception.SThemeCreationException;
import org.bonitasoft.engine.theme.exception.SThemeDeletionException;
import org.bonitasoft.engine.theme.exception.SThemeNotFoundException;
import org.bonitasoft.engine.theme.exception.SThemeReadException;
import org.bonitasoft.engine.theme.exception.SThemeUpdateException;
import org.bonitasoft.engine.theme.model.STheme;
import org.bonitasoft.engine.theme.model.SThemeType;

/**
 * @author Celine Souchet
 */
public interface ThemeService extends TenantLifecycleService {

    String THEME = "THEME";

    /**
     * Add a new theme
     * 
     * @param theme
     *            The theme to create
     * @return The created theme
     * @throws SThemeCreationException
     *             If an exception is thrown during sTheme creation
     * @since 6.2
     */
    STheme createTheme(STheme theme) throws SThemeCreationException;

    /**
     * Update a theme by the given theme and the new content
     * 
     * @param theme
     *            The theme to update
     * @param descriptor
     *            All attributes to update
     * @return The updated theme
     * @throws SThemeUpdateException
     *             If an exception is thrown during sTheme update
     * @since 6.2
     */
    STheme updateTheme(STheme theme, EntityUpdateDescriptor descriptor) throws SThemeUpdateException;

    /**
     * Delete theme by the given theme
     * 
     * @param theme
     *            The theme to delete
     * @throws SThemeNotFoundException
     *             If the identifier does not refer to an existing sTheme
     * @throws SThemeDeletionException
     *             If an exception is thrown during sTheme deletion
     * @since 6.2
     */
    void deleteTheme(STheme theme) throws SThemeNotFoundException, SThemeDeletionException;

    /**
     * Delete theme by its id
     * 
     * @param id
     *            The identifier of the theme to delete
     * @throws SThemeNotFoundException
     *             If the identifier does not refer to an existing sTheme
     * @throws SThemeDeletionException
     *             If an exception is thrown during sTheme deletion
     * @since 6.2
     */
    void deleteTheme(long id) throws SThemeNotFoundException, SThemeDeletionException;

    /**
     * Restore default theme by the given type
     * 
     * @param type
     *            The type of the theme to restore
     * @throws SRestoreThemeException
     *             If an exception is thrown when the default theme is restored
     * @since 6.2
     */
    void restoreDefaultTheme(SThemeType type) throws SRestoreThemeException;

    /**
     * Get theme by its id
     * 
     * @param id
     *            The identifier of the theme
     * @return The theme
     * @throws SThemeNotFoundException
     *             If the identifier does not refer to an existing sTheme
     * @throws SThemeReadException
     * @since 6.2
     */
    STheme getTheme(long id) throws SThemeNotFoundException, SThemeReadException;

    /**
     * Get the default or current theme for the specific type.
     * 
     * @param type
     *            The type of the theme
     * @param isDefault
     *            Theme is default or not
     * @return The theme
     * @throws SThemeNotFoundException
     *             If the type does not refer to an existing sTheme
     * @throws SThemeReadException
     * @since 6.2
     */
    STheme getTheme(SThemeType type, boolean isDefault) throws SThemeNotFoundException, SThemeReadException;

    /**
     * Get the last modified theme for the specific type.
     * 
     * @param type
     *            The type of the theme
     * @return The theme
     * @throws SThemeNotFoundException
     *             If the type does not refer to an existing sTheme
     * @throws SThemeReadException
     * @since 6.2
     */
    STheme getLastModifiedTheme(SThemeType type) throws SThemeNotFoundException, SThemeReadException;

    /**
     * Get the number of the themes corresponding to criteria
     * 
     * @param queryOptions
     *            A map of specific parameters of a query
     * @return A list of STheme objects
     */
    long getNumberOfThemes(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Search the themes corresponding to criteria
     * 
     * @param queryOptions
     *            A map of specific parameters of a query
     * @return A list of STheme objects
     */
    List<STheme> searchThemes(QueryOptions queryOptions) throws SBonitaReadException;

}
