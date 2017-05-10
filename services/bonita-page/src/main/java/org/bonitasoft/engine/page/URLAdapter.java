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
 */

package org.bonitasoft.engine.page;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SExecutionException;

/**
 * @author Baptiste Mesta
 */
public interface URLAdapter {

    /**
     * adapt an url based on a context
     *
     * @param url the base url
     * @param key the url key
     * @param context the provided context
     * @return the new url
     * @throws SExecutionException when the URL rewriting fails
     */
    String adapt(String url, String key, Map<String, Serializable> context) throws SExecutionException;

    /**
     * @return the identifier for this url adapter
     */
    String getId();

}
