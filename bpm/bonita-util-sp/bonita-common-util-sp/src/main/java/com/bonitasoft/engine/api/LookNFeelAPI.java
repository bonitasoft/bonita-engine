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

import com.bonitasoft.engine.looknfeel.LookNFeel;
import com.bonitasoft.engine.looknfeel.LookNFeelCreator;
import com.bonitasoft.engine.looknfeel.LookNFeelUpdater;
import com.bonitasoft.engine.looknfeel.exception.LookNFeelNotFoundException;

/**
 * Manage mobile and portal look'n'feel
 * 
 * @author Celine Souchet
 */
public interface LookNFeelAPI {

    /**
     * 
     * @param creator
     * @return
     * @throws AlreadyExistsException
     * @throws CreationException
     * 
     * @since 6.2
     */
    LookNFeel createLookNFeel(LookNFeelCreator creator) throws AlreadyExistsException, CreationException;

    /**
     * 
     * @param id
     * @param updater
     * @return
     * @throws UpdateException
     * @throws AlreadyExistsException
     * @since 6.2
     */
    LookNFeel updateLookNFeel(long id, LookNFeelUpdater updater) throws UpdateException, AlreadyExistsException;

    /**
     * 
     * @return
     * @throws LookNFeelNotFoundException
     * @since 6.2
     */
    LookNFeel getCurrentLookNFeel() throws LookNFeelNotFoundException;

    /**
     * 
     * @return
     * @throws LookNFeelNotFoundException
     * @since 6.2
     */
    LookNFeel getDefaultLookNFeel() throws LookNFeelNotFoundException;

    /**
     * 
     * @return
     * @throws LookNFeelNotFoundException
     * @since 6.2
     */
    Date getLastUpdatedDate() throws LookNFeelNotFoundException;

}
