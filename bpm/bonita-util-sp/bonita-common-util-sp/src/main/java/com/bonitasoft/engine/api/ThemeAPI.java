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
import org.bonitasoft.engine.exception.UpdateException;

import com.bonitasoft.engine.looknfeel.Theme;
import com.bonitasoft.engine.looknfeel.ThemeCreator;
import com.bonitasoft.engine.looknfeel.ThemeUpdater;
import com.bonitasoft.engine.looknfeel.exception.ThemeNotFoundException;

/**
 * Manage mobile and portal theme
 * 
 * @author Celine Souchet
 */
public interface ThemeAPI {

    /**
     * 
     * @param creator
     * @return
     * @throws AlreadyExistsException
     * @throws CreationException
     * 
     * @since 6.2
     */
    Theme createTheme(ThemeCreator creator) throws AlreadyExistsException, CreationException;

    /**
     * 
     * @param id
     * @param updater
     * @return
     * @throws UpdateException
     * @throws AlreadyExistsException
     * @since 6.2
     */
    Theme updateTheme(long id, ThemeUpdater updater) throws UpdateException, AlreadyExistsException;

    /**
     * 
     * @return
     * @throws ThemeNotFoundException
     * @since 6.2
     */
    Theme getCurrentTheme() throws ThemeNotFoundException;

    /**
     * 
     * @return
     * @throws ThemeNotFoundException
     * @since 6.2
     */
    Theme getDefaultTheme() throws ThemeNotFoundException;

    /**
     * 
     * @return
     * @throws ThemeNotFoundException
     * @since 6.2
     */
    Date getLastUpdatedDate() throws ThemeNotFoundException;

}
