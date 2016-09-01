package org.snowjak.rays.intersect;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;

/**
 * Represents a point of contact between an object (i.e., an
 * {@link Intersectable}) and a Ray.
 * 
 * @author rr247200
 * @param <S>
 *            an Intersectable type
 *
 */
public class Intersection<S extends Intersectable> {

	private Vector3D point, normal;

	private double distanceFromRayOrigin;

	private Ray ray;

	private S intersected;

	/**
	 * Create a new Intersection using (expressed in global coordinates) the
	 * given intersection-point, normal, ray, and object intersected.
	 * 
	 * @param point
	 * @param normal
	 * @param ray
	 * @param intersected
	 */
	public Intersection(Vector3D point, Vector3D normal, Ray ray, S intersected) {

		this.point = point;
		this.normal = normal;
		this.distanceFromRayOrigin = point.distance(ray.getOrigin());
		this.ray = ray;
		this.intersected = intersected;
	}

	/**
	 * @return this Intersection's location (in global terms)
	 */
	public Vector3D getPoint() {

		return point;
	}

	/**
	 * @return the object's normal-vector (in global terms) at this Intersection
	 */
	public Vector3D getNormal() {

		return normal;
	}

	/**
	 * @return the distance between this Intersection and the Ray's point of
	 *         origin
	 */
	public double getDistanceFromRayOrigin() {

		return distanceFromRayOrigin;
	}

	/**
	 * @return the {@link Ray} which originated this Intersection
	 */
	public Ray getRay() {

		return ray;
	}

	/**
	 * @return the object intersected by the Ray
	 */
	public S getIntersected() {

		return intersected;
	}
}
