package org.bonitasoft.engine.identity.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MD5CredentialsEncrypterTest {

    @Test
    public void hash_should_hash_password_using_md5() throws Exception {
        final MD5CredentialsEncrypter encrypter = new MD5CredentialsEncrypter();
        final String hash = encrypter.hash("123ñ");
        assertThat(hash).isEqualTo("jTIqcNwrbMbZJEf20AFxAQ==");
    }

    @Test
    public void check_should_compare_hash_password_with_a_clear_password() throws Exception {
        final MD5CredentialsEncrypter encrypter = new MD5CredentialsEncrypter();
        final boolean match = encrypter.check("123ñ", "jTIqcNwrbMbZJEf20AFxAQ==");
        assertThat(match).isTrue();
    }

}
