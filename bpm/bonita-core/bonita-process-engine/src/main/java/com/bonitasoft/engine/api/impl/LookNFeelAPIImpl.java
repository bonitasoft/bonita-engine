/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.api.LookNFeelAPI;
import com.bonitasoft.engine.looknfeel.LookNFeel;
import com.bonitasoft.engine.looknfeel.LookNFeelCreator;
import com.bonitasoft.engine.looknfeel.LookNFeelUpdater;
import com.bonitasoft.engine.looknfeel.exception.LookNFeelNotFoundException;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;

/**
 * @author Celine Souchet
 */
public class LookNFeelAPIImpl implements LookNFeelAPI {

    public TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    public LookNFeel createLookNFeel(final LookNFeelCreator creator) throws CreationException, AlreadyExistsException {
        // TODO Auto-generated method stub
        return null;
        // If field IS_DEFAULT is given, check in database, if exists. If yes, throw exception.

        // Map<LookNFeelField, Serializable> fields = creator.getFields();
        // final String name = (String) fields.get(LookNFeelField.NAME);
        // if (name == null || name.isEmpty()) {
        // throw new CreationException("Name is mandatory.");
        // }
        // final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        // final LookNFeelService LookNFeelService = tenantAccessor.getLookNFeelService();
        // try {
        // LookNFeelService.getLookNFeelByName(name);
        // throw new AlreadyExistsException("A LookNFeel with name \"" + name + "\" already exists");
        // } catch (final SLookNFeelNotFoundException sLookNFeelNotFoundException) {
        // try {
        // final SLookNFeel LookNFeel = LookNFeelService.createLookNFeel(SPModelConvertor.constructSLookNFeel(creator, false,
        // SessionInfos.getUserIdFromSession()));
        // return SPModelConvertor.toLookNFeel(LookNFeel);
        // } catch (final SLookNFeelCreationException e) {
        // throw new CreationException(e);
        // }
        // }
    }

    @Override
    public LookNFeel updateLookNFeel(final long id, final LookNFeelUpdater updateDescriptor) throws UpdateException, AlreadyExistsException {
        // TODO Auto-generated method stub
        return null;
        // if (updateDescriptor == null || updateDescriptor.getFields().isEmpty()) {
        // throw new UpdateException("The update descriptor does not contain field updates");
        // }
        // Serializable updatedName = updateDescriptor.getFields().get(LookNFeelUpdater.LookNFeelField.NAME);
        // if (updatedName != null) {
        // SearchResult<LookNFeel> searchLookNFeels;
        // try {
        // searchLookNFeels = searchLookNFeels(new SearchOptionsBuilder(0, 1).differentFrom(LookNFeelSearchDescriptor.ID, id)
        // .filter(LookNFeelSearchDescriptor.NAME, updatedName).done());
        // if (searchLookNFeels.getCount() > 0) {
        // throw new AlreadyExistsException("A LookNFeel with the name '" + updatedName + "' already exists");
        // }
        // } catch (SearchException e) {
        // throw new UpdateException("Cannot check if a LookNFeel with the name '" + updatedName + "' already exists", e);
        // }
        // }
        // final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        // final LookNFeelService LookNFeelService = tenantAccessor.getLookNFeelService();
        //
        // final UpdateLookNFeel updateLookNFeel = new UpdateLookNFeel(LookNFeelService, id,
        // updateDescriptor, SessionInfos.getUserIdFromSession());
        // try {
        // updateLookNFeel.execute();
        // return SPModelConvertor.toLookNFeel(updateLookNFeel.getResult());
        // } catch (final SBonitaException e) {
        // throw new UpdateException(e);
        // }
    }

    @Override
    public LookNFeel getCurrentLookNFeel() throws LookNFeelNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LookNFeel getDefaultLookNFeel() throws LookNFeelNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

}
