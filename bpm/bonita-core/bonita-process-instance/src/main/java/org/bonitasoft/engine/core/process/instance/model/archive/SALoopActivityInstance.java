/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.core.process.instance.model.archive;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.model.SLoopActivityInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Baptiste Mesta
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("loop")
public class SALoopActivityInstance extends SAActivityInstance {

    @Column(name = "loop_counter")
    private int loopCounter;
    @Column(name = "loop_max")
    private int loopMax;

    public SALoopActivityInstance(final SLoopActivityInstance sLoopActivityInstance) {
        super(sLoopActivityInstance);
        loopMax = sLoopActivityInstance.getLoopMax();
        loopCounter = sLoopActivityInstance.getLoopCounter();
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.LOOP_ACTIVITY;
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SLoopActivityInstance.class;
    }

}
