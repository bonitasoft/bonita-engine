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
package org.bonitasoft.engine.cache;

/**
 * @author Emmanuel Duchastenier
 */
public class CacheConfiguration {

    private String evictionPolicy = "LRU";

    private long timeToLiveSeconds = 60 * 60;

    private int maxElementsInMemory = 10000;

    private int maxElementsOnDisk = 20000;

    private boolean inMemoryOnly = false;

    private boolean eternal = false;

    private boolean readIntensive = false;

    private boolean copyOnRead = false;

    private boolean copyOnWrite = false;

    private String name;

    /**
     * @return the evictionPolicy
     */
    public String getEvictionPolicy() {
        return evictionPolicy;
    }

    /**
     * most implementation support LRU and LFU
     * some implementation (ehcache) support FIFO also
     * by default set to LRU
     *
     * @param evictionPolicy
     *        the evictionPolicy to set
     */
    public void setEvictionPolicy(final String evictionPolicy) {
        this.evictionPolicy = evictionPolicy;
    }

    /**
     * true if the elements are never evicted automatically
     *
     * @return the eternal
     */
    public boolean isEternal() {
        return eternal;
    }

    /**
     * @param eternal
     *        the eternal to set
     */
    public void setEternal(final boolean eternal) {
        this.eternal = eternal;
    }

    /**
     * The time to live is the time elements from this cache will be kept.
     * After this time the element can be evicted
     *
     * @return the timeToLiveSeconds
     */
    public long getTimeToLiveSeconds() {
        return timeToLiveSeconds;
    }

    /**
     * the maximum number of elements the cache will keep in memory
     *
     * @return the maxElementsInMemory
     */
    public int getMaxElementsInMemory() {
        return maxElementsInMemory;
    }

    /**
     * the maximum number of element the cache will keep on disk after the limit of elements in memory is reached
     *
     * @return the maxElementsOnDisk
     */
    public int getMaxElementsOnDisk() {
        return maxElementsOnDisk;
    }

    /**
     * if true nothing is stored on disk
     *
     * @return the inMemoryOnly
     */
    public boolean isInMemoryOnly() {
        return inMemoryOnly;
    }

    /**
     * @param timeToLiveSeconds
     *        the timeToLiveSeconds to set
     */
    public void setTimeToLiveSeconds(final long timeToLiveSeconds) {
        this.timeToLiveSeconds = timeToLiveSeconds;
    }

    /**
     * @param maxElementsInMemory
     *        the maxElementsInMemory to set
     */
    public void setMaxElementsInMemory(final int maxElementsInMemory) {
        this.maxElementsInMemory = maxElementsInMemory;
    }

    /**
     * @param maxElementsOnDisk
     *        the maxElementsOnDisk to set
     */
    public void setMaxElementsOnDisk(final int maxElementsOnDisk) {
        this.maxElementsOnDisk = maxElementsOnDisk;
    }

    /**
     * @param inMemoryOnly
     *        the inMemoryOnly to set
     */
    public void setInMemoryOnly(final boolean inMemoryOnly) {
        this.inMemoryOnly = inMemoryOnly;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Are the elements stored in the cache read more often than written ?
     *
     * @return readIntensive
     */
    public boolean isReadIntensive() {
        return readIntensive;
    }

    /**
     * @param readIntensive
     *        the readIntensive to set
     */
    public void setReadIntensive(final boolean readIntensive) {
        this.readIntensive = readIntensive;
    }

    public boolean isCopyOnRead() {
        return copyOnRead;
    }

    public void setCopyOnRead(final boolean copyOnRead) {
        this.copyOnRead = copyOnRead;
    }

    public boolean isCopyOnWrite() {
        return copyOnWrite;
    }

    public void setCopyOnWrite(final boolean copyOnWrite) {
        this.copyOnWrite = copyOnWrite;
    }

}
