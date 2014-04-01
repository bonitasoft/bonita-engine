/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.bdm.validator;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * @author Romain Bioteau
 *
 */
public class SQLNameValidator {

	private int maxLength;
	static Set<String> sqlKeywords;
	static{
		sqlKeywords = new HashSet<String>();
	}

	public SQLNameValidator(){
		this(255);
	}

	public SQLNameValidator(int maxLength){
		this.maxLength = maxLength;
		if(sqlKeywords.isEmpty()){
			initializeSQLKeywords();
		}
	}

	private void initializeSQLKeywords() {
		InputStream resourceAsStream = SQLNameValidator.class.getResourceAsStream("/sql_keywords");
		Scanner scanner = new Scanner(resourceAsStream);
		while (scanner.hasNext()) {
			String word = (String) scanner.nextLine();
			sqlKeywords.add(word.trim());
		}
		try {
			resourceAsStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isValid(String name){
		return name.matches("[a-zA-Z][\\d\\w#@]{0,"+maxLength+"}$") && !isSQLKeyword(name);
	}

	public boolean isSQLKeyword(String name){
		return sqlKeywords.contains(name.toUpperCase());
	}


}
