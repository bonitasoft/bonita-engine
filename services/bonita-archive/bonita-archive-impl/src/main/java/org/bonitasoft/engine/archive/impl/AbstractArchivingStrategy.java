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
package org.bonitasoft.engine.archive.impl;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.archive.ArchivingStrategy;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Celine Souchet
 * 
 */
public abstract class AbstractArchivingStrategy implements ArchivingStrategy {

    protected final Map<String, Boolean> archives;

    public AbstractArchivingStrategy() {
        archives = new HashMap<String, Boolean>();
    }

    public AbstractArchivingStrategy(final Map<String, Boolean> archives) {
        this.archives = archives;
    }

    @Override
    public boolean isArchivable(final Class<? extends PersistentObject> srcClass) {
        final Boolean isArchivable = archives.get(srcClass.getName());
        if (isArchivable == null) {
            throw new SBonitaRuntimeException("The class '" + srcClass.getName() + "' is not known as archivable");
        }
        return isArchivable;
    }

}
