/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssertionsUtils {

    private static final Logger logger = LoggerFactory.getLogger(AssertionsUtils.class);

    @FunctionalInterface
    public interface Assertion {

        void assertThat() throws Exception;

    }

    @FunctionalInterface
    public interface ExceptionHandler {

        void handle(Exception e) throws Exception;

    }

    public static void assertNoErrorAfterXAttemps(int attempts, Assertion assertThat, ExceptionHandler onError)
            throws Exception {
        for (int attempt = 1; attempt <= attempts; attempt++) {
            logger.info("Attempt {} on {}", attempt, attempts);
            try {
                assertThat.assertThat();
            } catch (Exception e) {
                logger.error("Attempt {} on {} failed", attempt, attempts);
                onError.handle(e);
                throw e;
            }
            logger.info("Completed attempt {} on {}", attempt, attempts);
        }
    }
}
