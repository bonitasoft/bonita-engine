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
package org.bonitasoft.engine.cache;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Emmanuel Duchastenier
 */
@Setter
@Getter
public class CacheConfiguration {

    /**
     * most implementation support LRU and LFU
     * some implementation (ehcache) support FIFO also
     * by default set to LRU
     */
    private String evictionPolicy = "LRU";

    /**
     * The time to live is the time elements from this cache will be kept.
     * After this time the element can be evicted
     */
    private long timeToLiveSeconds = 60 * 60;

    /**
     * the maximum number of elements the cache will keep in memory
     */
    private int maxElementsInMemory = 10000;

    /**
     * @param maxElementsInMemory
     *        the maxElementsInMemory to set. Zero is an invalid value (infinite).
     *        If value is set to 0 or less, the value will be reset to 1.
     */
    public void setMaxElementsInMemory(final int maxElementsInMemory) {
        this.maxElementsInMemory = maxElementsInMemory;
        if (this.maxElementsInMemory <= 0) {
            this.maxElementsInMemory = 1;
        }
    }

    /**
     * true if the elements are never evicted automatically
     */
    private boolean eternal = false;

    /**
     * Are the elements stored in the cache read more often than written ?
     */
    private boolean readIntensive = false;

    /**
     * Off-heap memory size in megabytes.
     * Zero means no off-heap storage (heap-only).
     * <p>
     * Off-heap storage provides overflow capacity for the heap tier without requiring disk I/O.
     * It uses native memory (outside JVM heap) which doesn't participate in garbage collection,
     * reducing GC pressure while maintaining good performance.
     * </p>
     * <p>
     * Example: 512 = 512MB of off-heap memory
     * </p>
     * <p>
     * Note: Off-heap storage requires that cached objects are serializable.
     * </p>
     */
    private int offHeapSizeMB = 0; // Default: no off-heap (heap-only)

    /**
     * Name of this cache configuration
     */
    private String name;

}
