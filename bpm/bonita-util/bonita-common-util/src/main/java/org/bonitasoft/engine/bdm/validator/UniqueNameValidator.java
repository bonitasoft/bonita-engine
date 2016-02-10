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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.bonitasoft.engine.bdm.model.NamedElement;

/**
 * @author Colin PUY
 */
public class UniqueNameValidator {

    public ValidationStatus validate(Collection<? extends NamedElement> namedElements,  String namedElementTypePluralForm) {
        ValidationStatus status = new ValidationStatus();
        Set<String> duplicateNames = findDuplicateNames(namedElements);
        for (String name : duplicateNames) {
            status.addError("There are at least two " + namedElementTypePluralForm + " with the same name : " + name);
        }
        return status;
    }
    
    private Set<String> findDuplicateNames(Collection<? extends NamedElement> list) {
        Set<String> duplicates = new LinkedHashSet<String>();
        Set<String> uniqueNames = new HashSet<String>();

        for (NamedElement t : list) {
            if (!uniqueNames.add(t.getName())) {
                duplicates.add(t.getName());
            }
        }

        return duplicates;
    }
}
