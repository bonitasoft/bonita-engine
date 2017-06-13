/**
 * Copyright (C) 2017 Bonitasoft S.A.
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

package org.bonitasoft.engine.commons.time;

import java.time.Instant;
import java.time.temporal.TemporalUnit;

/**
 * This is a fixed clock with some methods to change this fixed time
 *
 * Only used for testing purpose
 *
 * @author Baptiste Mesta.
 */
public class FixedEngineClock implements EngineClock {

    private Instant now;

    public FixedEngineClock(Instant now) {
        this.now = now;
    }

    @Override
    public Instant now() {
        return now;
    }

    public void setNow(Instant now) {
        this.now = now;
    }

    public void addTime(long amountToAdd, TemporalUnit unit) {
        now = now.plus(amountToAdd, unit);
    }
}
