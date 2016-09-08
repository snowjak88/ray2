package org.snowjak.rays;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;

/**
 * Represents a "ray" -- a vector + an origin point, or position + direction.
 * 
 * @author snowjak88
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
		this.recursiveLevel = recursiveLevel;
		
		if (this.vector.getNorm() != 0d)
			this.vector = this.vector.normalize();
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

	/**
	 * @return this Ray's recursion-level
	 */
	public int getRecursiveLevel() {

		return recursiveLevel;
	}

	@Override
	public String toString() {

		return recursiveLevel + "/" + point.toString() + "->" + vector.toString();
	}

	/**
	 * Determine the minimum distance between this Ray (treated as an infinite
	 * line) and a point.
	 * <p>
	 * Calculated by:
	 * 
	 * <pre>
	 *   P = point
	 *   O = ray origin
	 *   V = ray vector (normalized)
	 *   
	 *   L = P - O
	 *   t_ca = V . L
	 *   d^2 = |L|^2 - t_ca^2
	 *   
	 *   distance = sqrt(d^2)
	 * </pre>
	 * </p>
	 * 
	 * @param point
	 * @return the minimum distance between this Ray and a point
	 */
	public double getClosestApproachDistance(Vector3D point) {

		Vector3D L = point.subtract(getOrigin());
		double t_ca = getVector().dotProduct(L);
		double d2 = L.getNormSq() - FastMath.pow(t_ca, 2d);
		return FastMath.sqrt(d2);
	}

}
