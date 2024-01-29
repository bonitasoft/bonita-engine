/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.application.importer;

import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.business.application.model.SApplicationWithIcon;

/**
 * @author Elias Ricken de Medeiros
 */
public class ImportResult {

    private final SApplicationWithIcon application;
    private final ImportStatus importStatus;

    public ImportResult(SApplicationWithIcon application, ImportStatus importStatus) {
        this.application = application;
        this.importStatus = importStatus;
    }

    public SApplicationWithIcon getApplication() {
        return application;
    }

    public ImportStatus getImportStatus() {
        return importStatus;
    }
}
