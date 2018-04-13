/**
 * Copyright (C) 2018 Bonitasoft S.A.
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

package org.bonitasoft.engine.business.data.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.business.data.SchemaManager;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.hibernate.HibernateException;

/**
 * @author Pablo Alonso de Linaje García
 */
public class SchemaManagerReadOnly implements SchemaManager {
    private final TechnicalLoggerService loggerService;

    public SchemaManagerReadOnly(final TechnicalLoggerService loggerService) throws HibernateException {
        this.loggerService = loggerService;
        this.loggerService.log(SchemaManagerReadOnly.class, TechnicalLogSeverity.WARNING, "No update of BDM DB Schema will be performed. Please ensure that this update is done before using the new BDM");
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Exception> drop(Set<String> managedClasses) {
        this.loggerService.log(SchemaManagerReadOnly.class, TechnicalLogSeverity.WARNING, "No drop of BDM DB Schema will be performed. Please ensure that this update is done before using the new BDM");
        return new ArrayList<Exception>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Exception> update(Set<String> managedClasses) {
        this.loggerService.log(SchemaManagerReadOnly.class, TechnicalLogSeverity.WARNING, "No update of BDM DB Schema will be performed. Please ensure that this update is done before using the new BDM");
        return new ArrayList<Exception>();
    }
}
