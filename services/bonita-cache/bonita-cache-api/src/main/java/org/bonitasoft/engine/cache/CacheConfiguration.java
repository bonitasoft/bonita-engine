/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.cache;

/**
 * @author Emmanuel Duchastenier
 */
public class CacheConfiguration {

    protected long timeToLiveSeconds = 60 * 60;

    protected int maxElementsInMemory = 10000;

    protected int maxElementsOnDisk = 20000;

    protected boolean inMemoryOnly = false;

    /**
     * @param timeToLiveSeconds
     * @param maxElementsInMemory
     * @param maxElementsOnDisk
     * @param inMemoryOnly
     */
    public CacheConfiguration(final long timeToLiveSeconds, final int maxElementsInMemory, final int maxElementsOnDisk, final boolean inMemoryOnly) {
        super();
        this.timeToLiveSeconds = timeToLiveSeconds;
        this.maxElementsInMemory = maxElementsInMemory;
        this.maxElementsOnDisk = maxElementsOnDisk;
        this.inMemoryOnly = inMemoryOnly;
    }

    /**
     * @return the timeToLiveSeconds
     */
    public long getTimeToLiveSeconds() {
        return timeToLiveSeconds;
    }

    /**
     * @return the maxElementsInMemory
     */
    public int getMaxElementsInMemory() {
        return maxElementsInMemory;
    }

    /**
     * @return the maxElementsOnDisk
     */
    public int getMaxElementsOnDisk() {
        return maxElementsOnDisk;
    }

    /**
     * @return the inMemoryOnly
     */
    public boolean isInMemoryOnly() {
        return inMemoryOnly;
    }

}
