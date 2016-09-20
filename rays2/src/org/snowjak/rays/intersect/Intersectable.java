package org.snowjak.rays.intersect;

import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;

/**
 * Indicates that this object can be checked for {@link Intersection}s with a
 * {@link Ray}.
 * 
 * @author snowjak88
 *
 */
public interface Intersectable {

	/**
	 * Determine the closest Intersection where the given {@link Ray} (expressed
	 * in global coordinates) would intersect this object.
	 * 
	 * @param ray
	 * @return the closest Intersection made by the Ray on this object
	 */
	public <S extends Intersectable> Optional<Intersection<S>> getIntersection(Ray ray);

	/**
	 * Determine where the given {@link Ray} (expressed in global coordinates)
	 * would intersect this object.
	 * 
	 * @param ray
	 * @return the list of {@link Intersection}s made by the given Ray on this
	 *         object
	 */
	public <S extends Intersectable> List<Intersection<S>> getIntersections(Ray ray);

	/**
	 * Determine where the given {@link Ray} (expressed in global coordinates)
	 * would intersect this object. Include even those intersection-points
	 * "behind" the Ray.
	 * 
	 * @param ray
	 * @param includeBehindRayOrigin TODO
	 * @return the list of {@link Intersection}s made by the given Ray on this
	 *         object, including those behind the Ray's origin
	 */
	public <S extends Intersectable> List<Intersection<S>> getIntersections(Ray ray, boolean includeBehindRayOrigin);

	/**
	 * Tests to see if the given point (in global coordinates) is contained
	 * within this object.
	 * 
	 * <p>
	 * Specifically, starts from the point and works directly away from
	 * {@link #getLocation()}, and tests for an intersection with this object.
	 * </p>
	 * 
	 * @param point
	 * @return <code>true</code> if the given point is contained within this
	 *         object
	 */
	public boolean isInside(Vector3D point);

	/**
	 * Given a point {@code localPoint} in object-local coordinates, determine
	 * the surface-normal at the point on the object's surface nearest to
	 * {@code localPoint}.
	 * 
	 * @param localPoint
	 * @return the calculated surface-normal
	 */
	public Vector3D getNormalRelativeTo(Vector3D localPoint);
}
