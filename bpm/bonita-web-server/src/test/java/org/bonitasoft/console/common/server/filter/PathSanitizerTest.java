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
package org.bonitasoft.console.common.server.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PathSanitizerTest {

    @Test
    public void should_return_null_for_null_input() {
        assertThat(PathSanitizer.stripPathParameters(null)).isNull();
    }

    @Test
    public void should_return_empty_for_empty_input() {
        assertThat(PathSanitizer.stripPathParameters("")).isEmpty();
    }

    @Test
    public void should_not_modify_path_without_semicolons() {
        assertThat(PathSanitizer.stripPathParameters("/API/bpm/process/12345"))
                .isEqualTo("/API/bpm/process/12345");
    }

    @Test
    public void should_strip_semicolons_from_dotdot_traversal() {
        assertThat(PathSanitizer.stripPathParameters("/API/..;/..;/serverAPI"))
                .isEqualTo("/API/../../serverAPI");
    }

    @Test
    public void should_strip_semicolons_with_arbitrary_content() {
        assertThat(PathSanitizer.stripPathParameters("/API/..;anything/..;foo/serverAPI"))
                .isEqualTo("/API/../../serverAPI");
    }

    @Test
    public void should_strip_trailing_semicolon() {
        assertThat(PathSanitizer.stripPathParameters("/API/resource;jsessionid=abc123"))
                .isEqualTo("/API/resource");
    }

    @Test
    public void should_strip_multiple_semicolons_per_segment() {
        assertThat(PathSanitizer.stripPathParameters("/API/system/session/..;/..;/..;/serverAPI/something"))
                .isEqualTo("/API/system/session/../../../serverAPI/something");
    }

    @Test
    public void should_handle_semicolon_at_start_of_path() {
        assertThat(PathSanitizer.stripPathParameters(";param/API/resource"))
                .isEqualTo("/API/resource");
    }

    @Test
    public void should_handle_encoded_dotdot_semicolon_pattern() {
        assertThat(PathSanitizer.stripPathParameters("/API/a/..;anything/..;/..;/WEB-INF/web.xml"))
                .isEqualTo("/API/a/../../../WEB-INF/web.xml");
    }

    @Test
    public void should_preserve_normal_path_segments() {
        assertThat(PathSanitizer.stripPathParameters("/apps/myapp/API/bpm/case/42"))
                .isEqualTo("/apps/myapp/API/bpm/case/42");
    }

    @Test
    public void should_handle_consecutive_semicolons() {
        // Double semicolons: first ';' starts skipping until '/' or end of string
        assertThat(PathSanitizer.stripPathParameters("/API/a;;b/next"))
                .isEqualTo("/API/a/next");
        assertThat(PathSanitizer.stripPathParameters("/API/..;;/serverAPI"))
                .isEqualTo("/API/../serverAPI");
    }

    @Test
    public void should_not_strip_double_encoded_semicolons() {
        // %253b is double-encoded: %25 -> %, so %253b -> %3b (literal text, not a semicolon)
        // PathSanitizer only strips literal ';', not percent-encoded forms.
        // URL-decoding is the caller's responsibility (handled in URLExcludePattern).
        assertThat(PathSanitizer.stripPathParameters("/API/..%253b/..%253b/serverAPI"))
                .isEqualTo("/API/..%253b/..%253b/serverAPI");
    }
}
