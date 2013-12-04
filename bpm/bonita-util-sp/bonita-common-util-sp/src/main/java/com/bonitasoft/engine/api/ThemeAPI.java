/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import java.util.Date;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.session.InvalidSessionException;

import com.bonitasoft.engine.looknfeel.Theme;
import com.bonitasoft.engine.looknfeel.ThemeCreator;
import com.bonitasoft.engine.looknfeel.ThemeType;
import com.bonitasoft.engine.looknfeel.ThemeUpdater;
import com.bonitasoft.engine.looknfeel.exception.ThemeNotFoundException;

/**
 * Manage mobile and portal theme
 * 
 * @author Celine Souchet
 */
public interface ThemeAPI {

    /**
     * Create a new theme
     * 
     * @param creator
     *            the fields to initialize
     * @return The new created theme
     * @throws CreationException
     *             error thrown if can't create the new theme
     * @throws AlreadyExistsException
     *             error thrown if the theme already exists
     * @throws InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.2
     */
    Theme createTheme(ThemeCreator creator) throws CreationException, AlreadyExistsException;

    /**
     * Update a theme.
     * 
     * @param id
     *            the theme identifier to update
     * @param updater
     *            including new value of all attributes adaptable
     * @return The updated theme.
     * @throws UpdateException
     *             error thrown if can't update theme
     * @throws AlreadyExistsException
     *             error thrown if the theme already exists
     * @throws InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.2
     */
    Theme updateTheme(long id, ThemeUpdater updater) throws UpdateException, AlreadyExistsException;

    /**
     * Get the current theme for the specific type.
     * 
     * @param type
     *            the type of theme
     * @return the searched theme
     * @throws ThemeNotFoundException
     *             error thrown if can't find a theme corresponding to criteria
     * @throws RetrieveException
     *             If an exception occurs during the theme retrieving
     * @throws InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.2
     */
    Theme getCurrentTheme(ThemeType type) throws ThemeNotFoundException;

    /**
     * Get the default theme for the specific type.
     * 
     * @param type
     *            the type of theme
     * @return the searched theme
     * @throws ThemeNotFoundException
     *             error thrown if can't find a theme corresponding to criteria
     * @throws RetrieveException
     *             If an exception occurs during the theme retrieving
     * @throws InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.2
     */
    Theme getDefaultTheme(ThemeType type) throws ThemeNotFoundException;

    /**
     * Get the last updated date of the current theme for the specific type.
     * 
     * @param type
     *            the type of theme
     * @return the last updated date of the searched theme
     * @throws ThemeNotFoundException
     * @throws InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.2
     */
    Date getLastUpdatedDate(ThemeType type) throws ThemeNotFoundException;

}
