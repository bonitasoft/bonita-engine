/**
 * Copyright (C) 2013-2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
