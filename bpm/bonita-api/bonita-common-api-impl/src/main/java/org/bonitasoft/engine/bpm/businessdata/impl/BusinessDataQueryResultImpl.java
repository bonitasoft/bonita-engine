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
package org.bonitasoft.engine.bpm.businessdata.impl;

import java.io.Serializable;
import java.util.List;

import org.bonitasoft.engine.bpm.businessdata.BusinessDataQueryResult;

/**
 * @author Laurent Leseigneur
 */
public class BusinessDataQueryResultImpl implements BusinessDataQueryResult {

    private final Serializable jsonResults;
    private final BusinessDataQueryMetadataImpl businessDataQueryMetadata;

    public BusinessDataQueryResultImpl(){
        this(null,null);
    };

    public BusinessDataQueryResultImpl(Serializable jsonResults, BusinessDataQueryMetadataImpl businessDataQueryMetadata) {
        this.jsonResults = jsonResults;
        this.businessDataQueryMetadata = businessDataQueryMetadata;
    }

    public Serializable getJsonResults() {
        return jsonResults;
    }

    public BusinessDataQueryMetadataImpl getBusinessDataQueryMetadata() {
        return businessDataQueryMetadata;
    }

    @Override
    public long getCount() {
        return 0;
    }

    @Override
    public List getResult() {
        return null;
    }
}
