/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package org.mozartspaces.examples.flughafen;

import java.io.Serializable;

public final class LandebahnData implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int landebahnNr;
	private final String status;
	private final String flugNr;

	public LandebahnData(final int landebahnNr, final String status, final String flugNr) {
		this.landebahnNr = landebahnNr;
		this.status = status;
		this.flugNr = flugNr;
	}

	public int getLandebahnNr() {
		return landebahnNr;
	}

	public String getStatus() {
		return status;
	}

	public String getFlugNr() {
		return flugNr;
	}

}
