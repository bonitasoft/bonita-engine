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
package org.bonitasoft.engine.scheduler.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Baptiste Mesta
 */
public class IncrementItselfJob extends GroupJob {

    private static final long serialVersionUID = 3707724945060118636L;

    private static int value = 0;

    private static List<Date> executionDates = new ArrayList<Date>();

    @Override
    public void execute() {
        value++;
        addToExecutionDates(new Date(System.currentTimeMillis()));
    }

    public static int getValue() {
        return value;
    }

    public static void reset() {
        value = 0;
        executionDates = new ArrayList<Date>();
    }

    public static synchronized List<Date> getExecutionDates() {
        return new ArrayList<Date>(executionDates);
    }
    synchronized void addToExecutionDates(Date date) {
        executionDates.add(date);
    }

    @Override
    public String getDescription() {
        return "Increment itself ";
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) {
        super.setAttributes(attributes);
    }

}
