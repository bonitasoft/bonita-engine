/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.looknfeel.Theme;
import org.bonitasoft.engine.session.InvalidSessionException;

import com.bonitasoft.engine.looknfeel.ThemeCreator;
import com.bonitasoft.engine.looknfeel.ThemeUpdater;

/**
 * Manage mobile and portal theme
 * 
 * @author Celine Souchet
 */
public interface ThemeAPI extends org.bonitasoft.engine.api.ThemeAPI {

    /**
     * Create a new theme
     * 
     * @param creator
     *            The attributes to initialize
     * @return The new created theme
     * @throws CreationException
     *             If can't create the new theme
     * @throws AlreadyExistsException
     *             If the theme already exists
     * @throws InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.2
     */
    Theme createTheme(ThemeCreator creator) throws CreationException, AlreadyExistsException;

    /**
     * Update a theme.
     * 
     * @param id
     *            The identifier of the theme to update
     * @param themeUpdater
     *            The attributes to update
     * @return
     * @throws UpdateException
     *             If can't update theme
     * @throws AlreadyExistsException
     *             If the theme already exists
     * @throws InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.2
     */
    Theme updateTheme(long id, ThemeUpdater themeUpdater) throws UpdateException, AlreadyExistsException;

}
