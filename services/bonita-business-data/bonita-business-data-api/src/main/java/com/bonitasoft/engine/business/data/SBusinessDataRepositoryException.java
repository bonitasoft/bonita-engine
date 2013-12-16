package com.bonitasoft.engine.business.data;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

public class SBusinessDataRepositoryException extends SBonitaException {

	private static final long serialVersionUID = 1L;

	public SBusinessDataRepositoryException(final String message) {
		super(message);
	}

	public SBusinessDataRepositoryException(final Throwable cause) {
		super(cause);
	}

	public SBusinessDataRepositoryException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
