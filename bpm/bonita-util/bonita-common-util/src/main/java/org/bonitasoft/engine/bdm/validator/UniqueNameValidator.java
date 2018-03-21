/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.bdm.validator;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.api.result.StatusCode;
import org.bonitasoft.engine.api.result.StatusContext;
import org.bonitasoft.engine.bdm.model.NamedElement;

/**
 * @author Colin PUY
 */
public class UniqueNameValidator {

    public ValidationStatus validate(Collection<? extends NamedElement> namedElements,  String namedElementTypePluralForm) {
        ValidationStatus status = new ValidationStatus();
        Set<String> duplicateNames = findDuplicateNames(namedElements);
        Map<String, Serializable> context = new HashMap<>();
        context.put(StatusContext.BDM_ARTIFACT_KEY, namedElementTypePluralForm);
        for (String name : duplicateNames) {
            context.put(StatusContext.BDM_ARTIFACT_NAME_KEY, name);
            status.addError(StatusCode.DUPLICATE_CONSTRAINT_OR_INDEX_NAME,
                    String.format("There are at least two %s with the same name : %s", namedElementTypePluralForm, name),
                    context);
        }
        return status;
    }

    private Set<String> findDuplicateNames(Collection<? extends NamedElement> list) {
        Set<String> duplicates = new LinkedHashSet<>();
        Set<String> uniqueNames = new HashSet<>();

        for (NamedElement t : list) {
            if (!uniqueNames.add(t.getName())) {
                duplicates.add(t.getName());
            }
        }

        return duplicates;
    }
}
