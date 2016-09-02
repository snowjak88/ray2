package org.snowjak.rays.shape;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.intersect.Intersection;

/**
 * Represents a plane, by default oriented normal to the Y-axis.
 * 
 * @author rr247200
 *
 */
public class Plane extends Shape {

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersections(Ray ray) {

		Ray transformedRay = worldToLocal(ray);

		LinkedList<Intersection<Shape>> results = new LinkedList<>();

		//
		//
		// Because this plane will always be oriented normal to the Y-axis,
		// and its displacement will always = 0,
		// a number of simplifications apply to the intersection calculations.
		//
		// Remember that a ray at any given extent t is defined by P + V*t
		//
		// We want to find t such that P + V*t intersects the plane -- i.e.,
		// so that P.y + V.y*t = 0
		//
		//
		// First: check for the trivial case: where the vector is parallel to
		// the plane.
		if (Double.compare(FastMath.abs(transformedRay.getVector().getY()), World.DOUBLE_ERROR) <= 0) {
			//
			// This ray is entirely parallel to the plane.
			// In the past, we tried giving an intersection at 0 distance from
			// the ray origin if
			// the ray was entirely within the plane -- but that led to all
			// kinds of problems with trying to normalize zero-length vectors.
			// It's easier to say that, if a ray is entirely parallel to the
			// plane, then it's treated across the board as non-intersecting.
			return Collections.emptyList();

		} else {
			//
			// Not parallel.
			//
			// OK -- find t such that P.y + V.y*t = 0
			//
			// P.y + V.y*t = 0
			// V.y*t = 0 - P.y
			// t = -P.y/V.y
			//
			// If t < 0, then this ray is heading the wrong way! and will not
			// intersect.
			//
			double t = -transformedRay.getOrigin().getY() / transformedRay.getVector().getY();
			if (Double.compare(t, World.DOUBLE_ERROR) >= 0) {

				Vector3D intersectionPoint = transformedRay.getOrigin()
						.add(transformedRay.getVector().normalize().scalarMultiply(t));
				Vector3D normal = Vector3D.PLUS_J.scalarMultiply(
						FastMath.signum(transformedRay.getVector().negate().dotProduct(Vector3D.PLUS_J)));

				results.add(localToWorld(new Intersection<Shape>(intersectionPoint, normal, transformedRay, this)));
			}
		}

		return results;
	}

	/**
	 * @return this Plane's normal vector, in global coordinates
	 */
	public Vector3D getNormal() {

		return localToWorld(new Ray(Vector3D.ZERO, Vector3D.PLUS_J)).getVector();
	}

}
