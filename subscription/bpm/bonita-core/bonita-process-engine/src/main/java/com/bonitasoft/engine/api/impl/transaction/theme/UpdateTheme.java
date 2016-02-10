/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.theme;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.theme.ThemeService;
import org.bonitasoft.engine.theme.ThemeType;
import org.bonitasoft.engine.theme.builder.SThemeUpdateBuilder;
import org.bonitasoft.engine.theme.builder.SThemeUpdateBuilderFactory;
import org.bonitasoft.engine.theme.exception.SThemeUpdateException;
import org.bonitasoft.engine.theme.model.STheme;
import org.bonitasoft.engine.theme.model.SThemeType;

import com.bonitasoft.engine.theme.ThemeUpdater;
import com.bonitasoft.engine.theme.ThemeUpdater.ThemeField;

/**
 * @author Celine Souchet
 */
public class UpdateTheme implements TransactionContentWithResult<STheme> {

    private final ThemeService themeService;

    private final Long themeId;

    private final ThemeUpdater updateDescriptor;

    private STheme sTheme = null;

    public UpdateTheme(final ThemeService themeService, final Long themeId, final ThemeUpdater updateDescriptor) {
        super();
        this.themeService = themeService;
        this.themeId = themeId;
        this.updateDescriptor = updateDescriptor;
    }

    @Override
    public void execute() throws SBonitaException {
        sTheme = themeService.getTheme(themeId);
        if (!sTheme.isDefault()) {
            sTheme = themeService.updateTheme(sTheme, getThemeUpdateDescriptor());
        } else {
            throw new SThemeUpdateException("Can't update a default theme. Theme id = <" + themeId + ">, type = <" + sTheme.getType() + ">");
        }
    }

    @Override
    public STheme getResult() {
        return sTheme;
    }

    private EntityUpdateDescriptor getThemeUpdateDescriptor() {
        final SThemeUpdateBuilder updateBuilder = BuilderFactory.get(SThemeUpdateBuilderFactory.class).createNewInstance();
        final Map<ThemeField, Serializable> fields = updateDescriptor.getFields();
        for (final Entry<ThemeField, Serializable> field : fields.entrySet()) {
            switch (field.getKey()) {
                case CONTENT:
                    updateBuilder.setContent((byte[]) field.getValue());
                    break;
                case CSS_CONTENT:
                    updateBuilder.setCSSContent((byte[]) field.getValue());
                    break;
                case TYPE:
                    updateBuilder.setType(SThemeType.valueOf(((ThemeType) field.getValue()).name()));
                    break;
            }
        }
        updateBuilder.setLastUpdateDate(System.currentTimeMillis());
        return updateBuilder.done();
    }

}
