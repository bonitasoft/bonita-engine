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
package org.bonitasoft.engine.scheduler.recorder;

import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.scheduler.model.SFailedJob;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;

/**
 * @author Celine Souchet
 */
public class SelectDescriptorBuilder {

    // FIXME put in a common model
    public static <T extends PersistentObject> SelectByIdDescriptor<T> getElementById(final Class<T> clazz, final String elementName, final long id) {
        return new SelectByIdDescriptor<T>("get" + elementName + "ById", clazz, id);
    }

    public static SelectListDescriptor<SFailedJob> getFailedJobs(final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.emptyMap();
        return new SelectListDescriptor<SFailedJob>("getFailedJobs", parameters, SJobDescriptor.class, queryOptions);
    }

}
