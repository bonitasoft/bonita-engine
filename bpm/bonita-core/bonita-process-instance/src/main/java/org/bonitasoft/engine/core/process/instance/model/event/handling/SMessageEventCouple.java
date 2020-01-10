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
package org.bonitasoft.engine.core.process.instance.model.event.handling;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Elias Ricken de Medeiros
 */
@Data
@NoArgsConstructor
public class SMessageEventCouple implements PersistentObject {

    private long waitingMessageId;
    private SBPMEventType waitingMessageEventType;
    private long messageInstanceId;

    public SMessageEventCouple(final long waitingMessageId, final SBPMEventType waitingMessageEventType,
            final long messageInstanceId) {
        this.waitingMessageId = waitingMessageId;
        this.waitingMessageEventType = waitingMessageEventType;
        this.messageInstanceId = messageInstanceId;
    }

    @Override
    public long getId() {
        return -1;
    }

    @Override
    public void setId(final long id) {
        throw new IllegalArgumentException();
    }

    @Override
    public void setTenantId(final long id) {
        throw new IllegalArgumentException();
    }

}
