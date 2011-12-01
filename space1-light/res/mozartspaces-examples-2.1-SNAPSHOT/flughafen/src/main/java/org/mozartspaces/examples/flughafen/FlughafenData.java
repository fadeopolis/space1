/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.flughafen;

import java.io.Serializable;

public final class FlughafenData implements Serializable {

	private static final long serialVersionUID = 1L;

	private int nLandebahnen;
    private String status;
    private String flughafenName;

    public FlughafenData(final String flughafenName) {
    	this.flughafenName = flughafenName;
	}

	public FlughafenData(final int landebahnen, final String status, final String flughafenName) {
		nLandebahnen = landebahnen;
		this.status = status;
		this.flughafenName = flughafenName;
	}

	public int getNLandebahnen() {
		return nLandebahnen;
	}

	public void setNLandebahnen(final int landebahnen) {
		nLandebahnen = landebahnen;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public String getFlughafenName() {
		return flughafenName;
	}

	public void setFlughafenName(final String flughafenName) {
		this.flughafenName = flughafenName;
	}
}
