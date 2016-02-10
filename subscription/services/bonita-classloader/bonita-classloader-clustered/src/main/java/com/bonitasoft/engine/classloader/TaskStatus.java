/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.classloader;

import java.io.Serializable;

/**
 * 
 * @author Baptiste Mesta
 * 
 */
public class TaskStatus implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Throwable throwable;
	private boolean error = false;
	private long completionTime;

	private String origin;

	private String node;

	private String type;

	private long id;

	private boolean isLocal;

	public TaskStatus(String origin, String node, Throwable throwable,
			boolean error, long completionTime, boolean isLocal, String type,
			long id) {
		super();
		this.origin = origin;
		this.node = node;
		this.throwable = throwable;
		this.error = error;
		this.completionTime = completionTime;
		this.isLocal = isLocal;
		this.type = type;
		this.id = id;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public boolean isError() {
		return error;
	}

	public long getCompletionTime() {
		return completionTime;
	}

	public String getOrigin() {
		return origin;
	}

	public String getNode() {
		return node;
	}

	public String getMessage() {
		StringBuilder stb = new StringBuilder();
		if (error) {
			stb.append("Error refreshing");
		} else {
			stb.append("Refreshed");
			
		}
        stb.append(" classloader on node ");
		stb.append(node);
		stb.append(" . The classloader is ");
		if (isLocal) {
			stb.append(type);
			stb.append(" with id ");
			stb.append(id);
		} else {
			stb.append("global");
		}
		stb.append(". It took ");
		stb.append(completionTime);
		stb.append(" ms");
		if (error) {
			stb.append(", the error is ");
			stb.append(throwable.getMessage());
		}
		return stb.toString();
	}

}
