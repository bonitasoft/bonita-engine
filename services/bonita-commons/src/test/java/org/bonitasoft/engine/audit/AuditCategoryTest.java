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
package org.bonitasoft.engine.audit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuditCategoryTest {

    @Test
    void parent_logger_name_should_stay_the_one_routed_by_the_distribution() {
        // The distribution routes org.bonitasoft.runtime.audit (log4j2-appenders.xml): under another name, the
        // audit events would be written with the regular logs, and not to the audit log
        assertThat(AuditCategory.PARENT_LOGGER_NAME).isEqualTo("org.bonitasoft.runtime.audit");
    }

    @Test
    void authentication_events_should_go_to_a_child_logger_named_after_their_event_category() {
        assertThat(AuditCategory.AUTHENTICATION.getValue()).isEqualTo("authentication");
        assertThat(AuditCategory.AUTHENTICATION.getLoggerName())
                .isEqualTo("org.bonitasoft.runtime.audit.authentication");
    }

    @Test
    void category_values_should_be_ecs_words_usable_as_a_single_logger_name_segment() {
        // A dot would create a grandchild logger, and is not an ECS event.category value
        assertThat(AuditCategory.values())
                .allSatisfy(category -> assertThat(category.getValue()).matches("[a-z_]+"));
    }
}
