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
package org.bonitasoft.engine.external.identity.mapping.impl;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMapping;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;

/**
 * @author Emmanuel Duchastenier
 */
public class SelectDescriptorBuilder {

    public static SelectByIdDescriptor<SExternalIdentityMapping> getExternalIdentityMappingWithoutDisplayNameById(final long mappingId) {
        return new SelectByIdDescriptor<SExternalIdentityMapping>("getExternalIdentityMappingWithoutDisplayNameById", SExternalIdentityMapping.class, mappingId);
    }

    public static SelectByIdDescriptor<SExternalIdentityMapping> getExternalIdentityMappingById(final long mappingId) {
        return new SelectByIdDescriptor<SExternalIdentityMapping>("getExternalIdentityMappingById", SExternalIdentityMapping.class, mappingId);
    }

    public static SelectByIdDescriptor<SExternalIdentityMapping> getExternalIdentityMappingById(final long mappingId, final String suffix) {
        return new SelectByIdDescriptor<SExternalIdentityMapping>("getExternalIdentityMappingById" + suffix, SExternalIdentityMapping.class, mappingId);
    }

    public static SelectOneDescriptor<Long> getNumberOfExternalIdentityMappings(final String externalId, final String kind) {
        final Map<String, Object> parameters = new HashMap<String, Object>(2);
        parameters.put("externalId", externalId);
        parameters.put("kind", kind);
        return new SelectOneDescriptor<Long>("getNumberOfExternalIdentityMappings", parameters, SExternalIdentityMapping.class);
    }

}
