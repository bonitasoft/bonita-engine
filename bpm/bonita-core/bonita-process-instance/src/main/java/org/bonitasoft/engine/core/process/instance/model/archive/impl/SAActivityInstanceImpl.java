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
package org.bonitasoft.engine.core.process.instance.model.archive.impl;

import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;

/**
 * @author Elias Ricken de Medeiros
 * @author Bole Zhang
 */
public abstract class SAActivityInstanceImpl extends SAFlowNodeInstanceImpl implements SAActivityInstance {

    private static final long serialVersionUID = -6796085066522281027L;

    public SAActivityInstanceImpl() {
        super();
    }

    public SAActivityInstanceImpl(final SActivityInstance activityInstance) {
        super(activityInstance);
    }

}
