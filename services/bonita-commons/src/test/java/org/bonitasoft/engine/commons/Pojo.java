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
package org.bonitasoft.engine.commons;

import java.util.Date;
import java.util.List;

public class Pojo {

    private boolean choice;

    private Boolean bigChoice;

    private List<Boolean> bigChoices;

    private Date date;

    private List<Long> longs;

    private Pojo child;

    public Pojo() {
        //
    }

    public boolean isChoice() {
        return choice;
    }

    public void setChoice(final boolean choice) {
        this.choice = choice;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public List<Long> getLongs() {
        return longs;
    }

    public void setLongs(final List<Long> longs) {
        this.longs = longs;
    }

    public Boolean getBigChoice() {
        return bigChoice;
    }

    public void setBigChoice(final Boolean bigChoice) {
        this.bigChoice = bigChoice;
    }

    public List<Boolean> getBigChoices() {
        return bigChoices;
    }

    public void setBigChoices(final List<Boolean> bigChoices) {
        this.bigChoices = bigChoices;
    }

    public String notAGetter() {
        return null;
    }

    public String twoParamMethod(final String a, final Integer i) {
        return String.format("%s*%d", a, i);
    }

    public Pojo getChild() {
        return child;
    }

    public void setChild(Pojo child) {
        this.child = child;
    }
}
