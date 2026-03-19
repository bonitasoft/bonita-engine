/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.web.server.login;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Memory implementation of {@link LoginFailureTracker}
 * Note: this implementation is not distributed and will not work across multiple server instances.
 * <p>
 * Uses a fixed-window counting strategy: the failure count resets when the window expires.
 * This means an attacker could make up to {@code maxAttempts - 1} attempts per window indefinitely
 * without triggering a lockout. A sliding-window approach would be stricter, but the fixed window
 * is simpler and sufficient for the intended threat model (automated brute-force attacks).
 */
public class MemoryLoginFailureTracker implements LoginFailureTracker {

    private static final int EVICTION_INTERVAL = 100;

    /** Safety cap to prevent unbounded memory growth under sustained attack from many distinct usernames. */
    static final int MAX_ENTRIES = 10_000;

    private final boolean enabled;
    private final int maxAttempts;
    private final long lockoutDurationMillis;
    private final ConcurrentHashMap<String, FailureRecord> failures = new ConcurrentHashMap<>();
    private final AtomicInteger operationCount = new AtomicInteger();

    public MemoryLoginFailureTracker(boolean enabled, int maxAttempts, int lockoutDurationSeconds) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be positive, got: " + maxAttempts);
        }
        if (lockoutDurationSeconds <= 0) {
            throw new IllegalArgumentException(
                    "lockoutDurationSeconds must be positive, got: " + lockoutDurationSeconds);
        }
        this.enabled = enabled;
        this.maxAttempts = maxAttempts;
        this.lockoutDurationMillis = lockoutDurationSeconds * 1000L;
    }

    @Override
    public boolean isLockedOut(String username) {
        if (!enabled) {
            return false;
        }
        FailureRecord record = failures.get(username);
        if (record == null) {
            return false;
        }
        if (isExpired(record)) {
            failures.remove(username);
            return false;
        }
        return record.count >= maxAttempts;
    }

    @Override
    public void recordFailure(String username) {
        if (!enabled) {
            return;
        }
        failures.compute(username, (key, existing) -> {
            long now = currentTimeMillis();
            if (existing == null || isExpired(existing)) {
                return new FailureRecord(1, now);
            }
            return new FailureRecord(existing.count + 1, existing.windowStartTimestamp);
        });
        evictExpiredIfNeeded();
    }

    @Override
    public long getLockoutDurationSeconds() {
        return lockoutDurationMillis / 1000;
    }

    @Override
    public void resetFailures(String username) {
        if (!enabled) {
            return;
        }
        failures.remove(username);
    }

    private void evictExpiredIfNeeded() {
        if (operationCount.incrementAndGet() % EVICTION_INTERVAL == 0) {
            failures.entrySet().removeIf(entry -> isExpired(entry.getValue()));
            // If still over the safety cap after expiry eviction, evict oldest entries
            int excess = failures.size() - MAX_ENTRIES;
            if (excess > 0) {
                failures.entrySet().stream()
                        .sorted((a, b) -> Long.compare(a.getValue().windowStartTimestamp,
                                b.getValue().windowStartTimestamp))
                        .limit(excess)
                        .forEach(entry -> failures.remove(entry.getKey()));
            }
        }
    }

    private boolean isExpired(FailureRecord record) {
        return currentTimeMillis() - record.windowStartTimestamp >= lockoutDurationMillis;
    }

    protected long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    static class FailureRecord {

        final int count;
        final long windowStartTimestamp;

        FailureRecord(int count, long windowStartTimestamp) {
            this.count = count;
            this.windowStartTimestamp = windowStartTimestamp;
        }
    }
}
