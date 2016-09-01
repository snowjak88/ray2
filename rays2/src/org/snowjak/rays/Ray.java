package org.snowjak.rays;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Represents a "ray" -- a vector + an origin point, or position + direction.
 * 
 * @author rr247200
 *
 */
public class Ray {

	private Vector3D point, vector;

	private int recursiveLevel;

	/**
	 * Create a new Ray with the given position and direction.
	 * 
	 * @param origin
	 * @param vector
	 */
	public Ray(Vector3D origin, Vector3D vector) {
		this(origin, vector, 1);
	}

	/**
	 * Create a new Ray with the given position and direction.
	 * 
	 * @param origin
	 * @param vector
	 * @param recursiveLevel
	 */
	public Ray(Vector3D origin, Vector3D vector, int recursiveLevel) {
		this.point = origin;
		this.vector = vector;
	}

	/**
	 * @return this Ray's position
	 */
	public Vector3D getOrigin() {

		return point;
	}

	/**
	 * @return this Ray's direction
	 */
	public Vector3D getVector() {

		return vector;
	}

	public int getRecursiveLevel() {

		return recursiveLevel;
	}

	@Override
	public String toString() {

		return recursiveLevel + "/" + point.toString() + "->" + vector.toString();
	}

}
