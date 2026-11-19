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

import static org.bonitasoft.engine.Profiles.NOT_IN_CLUSTER;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile(NOT_IN_CLUSTER)
public class LoginFailureTrackerConfiguration {

    // Inline @Value defaults mirror bonita-platform-community.properties — keep them in sync.
    @Value("${bonita.runtime.security.bruteforce.enabled:true}")
    private boolean bruteForceEnabled;

    @Value("${bonita.runtime.security.bruteforce.max.attempts:5}")
    private int bruteForceMaxAttempts;

    @Value("${bonita.runtime.security.bruteforce.lockout.duration.seconds:600}")
    private int bruteForceLockoutDurationSeconds;

    @Bean
    public LoginFailureTracker loginFailureTracker() {
        return new MemoryLoginFailureTracker(
                bruteForceEnabled,
                bruteForceMaxAttempts,
                bruteForceLockoutDurationSeconds);
    }
}
