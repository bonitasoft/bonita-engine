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
package org.bonitasoft.engine.core.process.instance.model.builder.event.handling.impl;

import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SMessageInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.impl.SMessageInstanceImpl;

/**
 * @author Elias Ricken de Medeiros
 */
public class SMessageInstanceBuilderImpl implements SMessageInstanceBuilder {

    private final SMessageInstanceImpl entity;
    
    public SMessageInstanceBuilderImpl(final SMessageInstanceImpl entity) {
        super();
        this.entity = entity;
    }

    @Override
    public SMessageInstanceBuilder setCorrelation(final int index, final String correlation) {
        switch (index) {
            case 1:
                entity.setCorrelation1(correlation);
                break;
            case 2:
                entity.setCorrelation2(correlation);
                break;
            case 3:
                entity.setCorrelation3(correlation);
                break;
            case 4:
                entity.setCorrelation4(correlation);
                break;
            case 5:
                entity.setCorrelation5(correlation);
                break;
            default:
                break;
        }
        return this;
    }

    @Override
    public SMessageInstance done() {
        return entity;
    }

}
