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

package org.bonitasoft.engine.business.data.proxy;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.bdm.lazy.LazyLoaded;

/**
 * @author Romain Bioteau
 * @author Laurent Leseigneur
 */
public class PersonEntity implements Entity {

    public PersonEntity() {

    }

    /*
     * (non-Javadoc)
     * @see com.bonitasoft.engine.bdm.Entity#getPersistenceId()
     */
    @Override
    public Long getPersistenceId() {
        return 1L;
    }

    /*
     * (non-Javadoc)
     * @see com.bonitasoft.engine.bdm.Entity#getPersistenceVersion()
     */
    @Override
    public Long getPersistenceVersion() {
        return null;
    }

    @LazyLoaded
    public String getWithLazyLoadedAnnotation() {
        return "getWithLazyLoadedAnnotation";
    }

    public String getWithoutLazyLoadedAnnotation() {
        return "getWithoutLazyLoadedAnnotation";
    }
}
