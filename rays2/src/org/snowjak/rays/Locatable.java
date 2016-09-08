package org.snowjak.rays;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Indicates that something can be said to have a definite location in 3D space.
 * 
 * @author snowjak88
 *
 */
public interface Locatable {

	/**
	 * Get this object's location in 3D space (expressed in global coordinates).
	 * 
	 * @return this object's location
	 */
	public Vector3D getLocation();
}
