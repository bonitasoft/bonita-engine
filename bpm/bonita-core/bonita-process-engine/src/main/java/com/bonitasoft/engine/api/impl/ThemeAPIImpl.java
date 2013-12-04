/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.util.Date;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.api.ThemeAPI;
import com.bonitasoft.engine.looknfeel.Theme;
import com.bonitasoft.engine.looknfeel.ThemeCreator;
import com.bonitasoft.engine.looknfeel.ThemeType;
import com.bonitasoft.engine.looknfeel.ThemeUpdater;
import com.bonitasoft.engine.looknfeel.exception.ThemeNotFoundException;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;

/**
 * @author Celine Souchet
 */
public class ThemeAPIImpl implements ThemeAPI {

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
    public Theme createTheme(final ThemeCreator creator) throws CreationException, AlreadyExistsException {
        // TODO Auto-generated method stub
        return null;
        // If field IS_DEFAULT is given, check in database, if exists. If yes, throw exception.

        // Map<ThemeField, Serializable> fields = creator.getFields();
        // final String name = (String) fields.get(ThemeField.NAME);
        // if (name == null || name.isEmpty()) {
        // throw new CreationException("Name is mandatory.");
        // }
        // final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        // final ThemeService ThemeService = tenantAccessor.getThemeService();
        // try {
        // ThemeService.getThemeByName(name);
        // throw new AlreadyExistsException("A Theme with name \"" + name + "\" already exists");
        // } catch (final SThemeNotFoundException sThemeNotFoundException) {
        // try {
        // final STheme Theme = ThemeService.createTheme(SPModelConvertor.constructSTheme(creator, false,
        // SessionInfos.getUserIdFromSession()));
        // return SPModelConvertor.toTheme(Theme);
        // } catch (final SThemeCreationException e) {
        // throw new CreationException(e);
        // }
        // }
    }

    @Override
    public Theme updateTheme(final long id, final ThemeUpdater updateDescriptor) throws UpdateException, AlreadyExistsException {
        // TODO Auto-generated method stub
        return null;
        // if (updateDescriptor == null || updateDescriptor.getFields().isEmpty()) {
        // throw new UpdateException("The update descriptor does not contain field updates");
        // }
        // Serializable updatedName = updateDescriptor.getFields().get(ThemeUpdater.ThemeField.NAME);
        // if (updatedName != null) {
        // SearchResult<Theme> searchThemes;
        // try {
        // searchThemes = searchThemes(new SearchOptionsBuilder(0, 1).differentFrom(ThemeSearchDescriptor.ID, id)
        // .filter(ThemeSearchDescriptor.NAME, updatedName).done());
        // if (searchThemes.getCount() > 0) {
        // throw new AlreadyExistsException("A Theme with the name '" + updatedName + "' already exists");
        // }
        // } catch (SearchException e) {
        // throw new UpdateException("Cannot check if a Theme with the name '" + updatedName + "' already exists", e);
        // }
        // }
        // final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        // final ThemeService ThemeService = tenantAccessor.getThemeService();
        //
        // final UpdateTheme updateTheme = new UpdateTheme(ThemeService, id,
        // updateDescriptor, SessionInfos.getUserIdFromSession());
        // try {
        // updateTheme.execute();
        // return SPModelConvertor.toTheme(updateTheme.getResult());
        // } catch (final SBonitaException e) {
        // throw new UpdateException(e);
        // }
    }

    @Override
    public Theme getCurrentTheme(ThemeType type) throws ThemeNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Theme getDefaultTheme(ThemeType type) throws ThemeNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getLastUpdatedDate(ThemeType type) throws ThemeNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

}
