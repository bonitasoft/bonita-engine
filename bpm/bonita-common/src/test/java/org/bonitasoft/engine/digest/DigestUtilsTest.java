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
package org.bonitasoft.engine.digest;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.bonitasoft.engine.digest.DigestUtils.*;

import org.junit.Test;

public class DigestUtilsTest {

    @Test
    public void should_encode_base64_when_source_with_accents() {
        assertThat(encodeBase64AsUtf8String(bytes("I love Bonita héhé"))).isEqualTo("SSBsb3ZlIEJvbml0YSBow6low6k=");
    }

    @Test
    public void should_generate_md5_when_source_with_accents() {
        assertThat(md5("I lôve Bonita hïhà"))
                .isEqualTo(new byte[] { 32, 69, -29, -53, 5, 12, -97, 109, 27, -99, 16, -27, 35, 71, 92, -46 });
    }

    @Test
    public void should_generate_md5_fail_when_source_is_null() {
        assertThatThrownBy(() -> md5(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_generate_sha1_when_source_with_accents() {
        assertThat(sha1("You Lik~e Bonita ù?"))
                .isEqualTo(new byte[] { 87, 92, -26, 118, -34, -105, -48, 1, 124, 85, 62, 10, -24, -87, -11, -22, 112,
                        69, -51, 63 });
    }

    @Test
    public void should_generate_sha1_fail_when_source_is_null() {
        assertThatThrownBy(() -> sha1(null)).isInstanceOf(NullPointerException.class);
    }

    private static byte[] bytes(String string) {
        return string.getBytes(UTF_8);
    }

}
