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
package org.bonitasoft.engine.business.application.importer.validator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.business.application.ApplicationMenuCreator;
import org.bonitasoft.engine.business.application.ApplicationMenuField;
import org.springframework.stereotype.Component;

/**
 * @author Elias Ricken de Medeiros
 */
@Component
public class ApplicationMenuCreatorValidator {

    public List<String> isValid(final ApplicationMenuCreator creator) {
        List<String> problems = new ArrayList<>();
        final Map<ApplicationMenuField, Serializable> fields = creator.getFields();
        if (fields.get(ApplicationMenuField.APPLICATION_ID) == null) {
            problems.add("The applicationId cannot be null");
        }
        return problems;
    }

}
