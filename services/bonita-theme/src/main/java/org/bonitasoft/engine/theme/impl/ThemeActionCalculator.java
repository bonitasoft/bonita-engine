/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.engine.theme.impl;

import java.security.NoSuchAlgorithmException;

import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.theme.model.STheme;

/**
 * @author Elias Ricken de Medeiros
 */
public class ThemeActionCalculator {

    private final TechnicalLoggerService loggerService;

    public ThemeActionCalculator(TechnicalLoggerService loggerService) {
        this.loggerService = loggerService;
    }

    public enum ThemeAction {

        CREATE,

        UPDATE,

        NONE

    }

    public ThemeAction calculateAction(STheme currentTheme, byte[] newContent) {
        if(!hasContent(newContent)) {
            return ThemeAction.NONE;
        }
        if (currentTheme == null) {
            return ThemeAction.CREATE;
        }
        if (currentTheme.isDefault() && hasContentChanged(currentTheme, newContent)) {
            return ThemeAction.UPDATE;
        }
        return ThemeAction.NONE;
    }

    private boolean hasContentChanged(final STheme currentTheme, final byte[] newContent) {
        boolean changed = false;
        try {
            changed = !md5(currentTheme.getContent()).equals(md5(newContent));
        } catch (NoSuchAlgorithmException e) {
            if (loggerService.isLoggable(getClass(), TechnicalLogSeverity.ERROR)) {
                loggerService.log(getClass(), TechnicalLogSeverity.ERROR,
                        "Unable to verify if default theme '" + currentTheme.getType() + "' has changed. It will not be updated.", e);
            }
        }
        return changed;
    }

    //package private for testing purpose
    String md5(byte[] content) throws NoSuchAlgorithmException {
        return IOUtil.md5(content);
    }

    private boolean hasContent(final byte[] newContent) {
        return newContent != null && newContent.length > 0;
    }

}
