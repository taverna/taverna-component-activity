/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.component.profile;

import java.util.ArrayList;
import java.util.List;

import uk.org.taverna.ns._2012.component.profile.Port;
import uk.org.taverna.ns._2012.component.profile.SemanticAnnotation;

/**
 * Specifies the semantic annotations that a port must have.
 * 
 * @author David Withers
 */
public class PortProfile {
	private final ComponentProfile componentProfile;
	private final Port port;

	public PortProfile(ComponentProfile componentProfile, Port port) {
		this.componentProfile = componentProfile;
		this.port = port;
	}

	public List<SemanticAnnotationProfile> getSemanticAnnotations() {
		List<SemanticAnnotationProfile> saProfiles = new ArrayList<SemanticAnnotationProfile>();
		for (SemanticAnnotation annotation : port.getSemanticAnnotation())
			saProfiles.add(new SemanticAnnotationProfile(componentProfile,
					annotation));
		return saProfiles;
	}

	@Override
	public String toString() {
		return "PortProfile \n  SemanticAnnotations : "
				+ getSemanticAnnotations();
	}
}
