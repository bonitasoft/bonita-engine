package com.bonitasoft.engine.bdm.dao.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CapitalizerTest {

	@Test
	public void should_capitalize_a_string() throws Exception {

		String capitalized = Capitalizer.capitalize("uncapitalized");

		assertThat(capitalized).isEqualTo("Uncapitalized");
	}

	@Test
	public void should_do_nothing_for_a_null_string() throws Exception {
		String capitalized = Capitalizer.capitalize(null);

		assertThat(capitalized).isNull();
	}

	@Test
	public void should_do_nothing_for_an_empty_string() throws Exception {
		String capitalized = Capitalizer.capitalize("");

		assertThat(capitalized).isEmpty();
	}
}
