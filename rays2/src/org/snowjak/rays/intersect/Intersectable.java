package org.snowjak.rays.intersect;

import java.util.List;

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
	 * Determine where the given {@link Ray} (expressed in global coordinates)
	 * would intersect this object.
	 * 
	 * @param ray
	 * @return the list of {@link Intersection}s made by the given Ray on this
	 *         object, sorted by distance from the ray's origin
	 */
	public <S extends Intersectable> List<Intersection<S>> getIntersections(Ray ray);

	/**
	 * Determine where the given {@link Ray} (expressed in global coordinates)
	 * would intersect this object. Include even those intersection-points
	 * "behind" the Ray.
	 * 
	 * @param ray
	 * @return
	 */
	public <S extends Intersectable> List<Intersection<S>> getIntersectionsIncludingBehind(Ray ray);

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
}
