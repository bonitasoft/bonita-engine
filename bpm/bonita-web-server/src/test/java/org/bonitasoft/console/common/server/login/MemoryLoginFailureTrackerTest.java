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
package org.bonitasoft.console.common.server.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.getField;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.bonitasoft.web.server.login.MemoryLoginFailureTracker;
import org.junit.Test;

public class MemoryLoginFailureTrackerTest {

    private final AtomicLong clock = new AtomicLong(System.currentTimeMillis());

    private MemoryLoginFailureTracker createTracker(boolean enabled, int maxAttempts, int lockoutDurationSeconds) {
        return new MemoryLoginFailureTracker(enabled, maxAttempts, lockoutDurationSeconds) {

            @Override
            protected long currentTimeMillis() {
                return clock.get();
            }
        };
    }

    @Test
    public void isLockedOut_should_return_false_with_no_failures() {
        var tracker = createTracker(true, 5, 600);

        assertThat(tracker.isLockedOut("user1")).isFalse();
    }

    @Test
    public void isLockedOut_should_return_false_after_fewer_than_max_failures() {
        var tracker = createTracker(true, 5, 600);

        for (int i = 0; i < 4; i++) {
            tracker.recordFailure("user1");
        }

        assertThat(tracker.isLockedOut("user1")).isFalse();
    }

    @Test
    public void isLockedOut_should_return_true_after_max_failures() {
        var tracker = createTracker(true, 5, 600);

        for (int i = 0; i < 5; i++) {
            tracker.recordFailure("user1");
        }

        assertThat(tracker.isLockedOut("user1")).isTrue();
    }

    @Test
    public void isLockedOut_should_return_false_after_lockout_expires() {
        var tracker = createTracker(true, 5, 60);

        for (int i = 0; i < 5; i++) {
            tracker.recordFailure("user1");
        }
        assertThat(tracker.isLockedOut("user1")).isTrue();

        clock.addAndGet(60_000L);

        assertThat(tracker.isLockedOut("user1")).isFalse();
    }

    @Test
    public void resetFailures_should_clear_failure_count() {
        var tracker = createTracker(true, 5, 600);

        for (int i = 0; i < 5; i++) {
            tracker.recordFailure("user1");
        }
        assertThat(tracker.isLockedOut("user1")).isTrue();

        tracker.resetFailures("user1");

        assertThat(tracker.isLockedOut("user1")).isFalse();
    }

    @Test
    public void isLockedOut_should_return_false_when_disabled() {
        var tracker = createTracker(false, 5, 600);

        for (int i = 0; i < 10; i++) {
            tracker.recordFailure("user1");
        }

        assertThat(tracker.isLockedOut("user1")).isFalse();
    }

    @Test
    public void recordFailure_should_start_fresh_after_window_expires() {
        var tracker = createTracker(true, 5, 60);

        for (int i = 0; i < 4; i++) {
            tracker.recordFailure("user1");
        }

        clock.addAndGet(60_000L);

        // After window expires, counter should reset
        tracker.recordFailure("user1");

        assertThat(tracker.isLockedOut("user1")).isFalse();
    }

    @Test
    public void isLockedOut_should_track_different_users_independently() {
        var tracker = createTracker(true, 5, 600);

        for (int i = 0; i < 5; i++) {
            tracker.recordFailure("user1");
        }
        for (int i = 0; i < 3; i++) {
            tracker.recordFailure("user2");
        }

        assertThat(tracker.isLockedOut("user1")).isTrue();
        assertThat(tracker.isLockedOut("user2")).isFalse();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void eviction_should_cap_entries_at_max_and_evict_oldest_first() {
        var tracker = createTracker(true, 5, 600);

        // Access internal state to pre-populate beyond MAX_ENTRIES without 10k+ method calls
        var failures = (ConcurrentHashMap<String, Object>) getField(tracker, "failures");
        var operationCount = (AtomicInteger) getField(tracker, "operationCount");

        // Insert MAX_ENTRIES + 5 entries with staggered timestamps so we know which are oldest
        long baseTime = clock.get();
        int totalEntries = 10_000 + 5;
        for (int i = 0; i < totalEntries; i++) {
            // Use MemoryLoginFailureTracker.FailureRecord via reflection — it's package-private
            // Entries get timestamps from baseTime + i, so user_0..user_4 are the oldest
            failures.put("user_" + i, createFailureRecord(1, baseTime + i));
        }
        assertThat(failures).hasSize(totalEntries);

        // Set operationCount to 99 so the next recordFailure() call triggers eviction
        operationCount.set(99);

        // This 100th operation triggers evictExpiredIfNeeded()
        tracker.recordFailure("trigger_user");

        // Map should be capped at MAX_ENTRIES (the trigger_user entry is part of it)
        assertThat(failures.size()).isLessThanOrEqualTo(10_000);

        // The 5 oldest entries (user_0 through user_4) should have been evicted
        for (int i = 0; i < 5; i++) {
            assertThat(failures.containsKey("user_" + i))
                    .as("user_%d (oldest) should be evicted", i)
                    .isFalse();
        }

        // A newer entry should still be present
        assertThat(failures.containsKey("user_" + (totalEntries - 1))).isTrue();
    }

    /**
     * Creates a FailureRecord instance via reflection since the class is package-private.
     */
    private Object createFailureRecord(int count, long windowStartTimestamp) {
        try {
            Class<?> recordClass = Class
                    .forName("org.bonitasoft.web.server.login.MemoryLoginFailureTracker$FailureRecord");
            var constructor = recordClass.getDeclaredConstructor(int.class, long.class);
            constructor.setAccessible(true);
            return constructor.newInstance(count, windowStartTimestamp);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create FailureRecord", e);
        }
    }
}
