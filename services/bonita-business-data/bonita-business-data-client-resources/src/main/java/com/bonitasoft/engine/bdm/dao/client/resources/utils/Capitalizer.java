package com.bonitasoft.engine.bdm.dao.client.resources.utils;

public class Capitalizer {

	public static String capitalize(final String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}
}
