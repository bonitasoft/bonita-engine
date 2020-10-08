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
package org.bonitasoft.engine.platform.configuration.monitoring;

import java.util.Arrays;
import java.util.concurrent.atomic.DoubleAdder;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.step.StepCounter;

/**
 * Add the total of the counter as a Measurement to the original StepCounter
 */
public class CumulativeAndStepCounter extends StepCounter {

    private final DoubleAdder count;

    CumulativeAndStepCounter(Id id, Clock clock, long stepMillis) {
        super(id, clock, stepMillis);
        count = new DoubleAdder();
    }

    @Override
    public void increment(double amount) {
        super.increment(amount);
        count.add(amount);
    }

    private double total() {
        return count.sum();
    }

    @Override
    public Iterable<Measurement> measure() {
        return Arrays.asList(new Measurement(this::count, Statistic.COUNT),
                new Measurement(this::total, Statistic.TOTAL));
    }

}
