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
package org.bonitasoft.engine.sequence;

import java.util.Collections;
import java.util.Set;

/**
 * @author Charles Souillard
 */
public class SequenceMapping {

    private final Set<String> classNames;
    private final long sequenceId;
    private final int rangeSize;

    public SequenceMapping(String className, long sequenceId, final int rangeSize) {
        this(Collections.singleton(className), sequenceId, rangeSize);
    }

    public SequenceMapping(Set<String> classNames, long sequenceId, final int rangeSize) {
        this.classNames = classNames;
        this.sequenceId = sequenceId;
        this.rangeSize = rangeSize;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public Set<String> getClassNames() {
        return classNames;
    }

    public int getRangeSize() {
        return rangeSize;
    }

    @Override
    public String toString() {
        return "SequenceMapping{" +
                "sequenceId=" + sequenceId +
                ", classNames='" + classNames + '\'' +
                ", rangeSize=" + rangeSize +
                '}';
    }
}
