package org.snowjak.rays.shape;

import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.material.Material;

/**
 * Represents a Sphere, centered on (0,0,0) (absent any transformations you may
 * apply).
 * 
 * @author snowjak88
 *
 */
public class Sphere extends Shape {

	/**
	 * Create a Sphere of radius {@code 1.0}
	 */
	public Sphere() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersections(Ray ray, boolean includeBehindRayOrigin,
			boolean onlyIncludeClosest) {

		Ray transformedRay = worldToLocal(ray);
		//
		// O = sphere origin
		// P = ray origin
		//
		//
		// L = O - P
		//
		Vector3D L = worldToLocal(getLocation()).subtract(transformedRay.getOrigin());
		//
		// v = ray vector (normalized)
		//
		// t_ca = v dot-product L
		//
		// t_ca = v . L
		double t_ca = transformedRay.getVector().dotProduct(L);
		//
		// d = shortest distance from center of sphere to ray
		//
		// d^2 = |L|^2 - t_ca^2
		//
		double d2 = L.getNormSq() - pow(t_ca, 2d);
		//
		// r = sphere's radius
		//
		// Now -- if d > r, then this ray does *not* intersect this sphere!
		double r2 = pow(1d, 2d);
		if (Double.compare(d2, r2) > 0)
			return Collections.emptyList();
		//
		// r and d describe two sides of a triangle, delimited by:
		// A) the ray's path as it crosses the sphere
		// B) the segment of length r between O and the ray's intersection with
		// the sphere's border
		// C) the segment of length d between O and the ray's closest approach
		// to O
		//
		// Specifically, r = B and d = C
		// t_hc = A<-->C
		//
		// t_hc = sqrt ( r^2 - d^2 )
		//
		double t_hc = sqrt(r2 - d2);
		//
		//
		// Now: t_ca = distance from P to (C)
		// and t_hc = distance from (C) to intersection
		//
		// So t -- the distance from P to the ray's intersection with the sphere
		// --
		// t = t_ca +/- t_hc
		//
		// Remember that a ray that intersects a sphere will intersect the
		// sphere at 2 points:
		// one intersection close to P, one far from P
		//
		// So the two intersection-points are located at:
		// v * (t_ca - t_hc) + P
		// v * (t_ca + t_hc) + P
		//
		double intersectionDistance1 = t_ca - t_hc, intersectionDistance2 = t_ca + t_hc;
		boolean isIntersectionDistance1Smaller = Double.compare(intersectionDistance1, intersectionDistance2) < 0;

		boolean useIntersection1 = (includeBehindRayOrigin
				|| Double.compare(intersectionDistance1, World.NEARLY_ZERO) >= 0);
		boolean useIntersection2 = (includeBehindRayOrigin
				|| Double.compare(intersectionDistance2, World.NEARLY_ZERO) >= 0);

		List<Intersection<Shape>> results = new LinkedList<>();

		if (useIntersection1 && Double.compare(FastMath.abs(intersectionDistance1), World.NEARLY_ZERO) >= 0) {
			Vector3D intersectPointOnSphere1 = transformedRay.getVector()
					.scalarMultiply(intersectionDistance1)
					.add(transformedRay.getOrigin());

			Vector3D normal1 = intersectPointOnSphere1.normalize();
			double normalSign = FastMath.signum(transformedRay.getVector().negate().dotProduct(normal1));
			normal1 = normal1.scalarMultiply(normalSign).normalize();

			Material leaving = Material.AIR, entering = Material.AIR;
			if (isIntersectionDistance1Smaller)
				entering = getMaterial();
			else
				leaving = getMaterial();

			results.add(localToWorld(new Intersection<Shape>(intersectPointOnSphere1, normal1, transformedRay, this,
					this.getDiffuseColorScheme(), this.getSpecularColorScheme(), this.getEmissiveColorScheme(), leaving,
					entering)));
		}

		if (!(onlyIncludeClosest && results.size() > 0))
			if (useIntersection2 && Double.compare(FastMath.abs(intersectionDistance2), World.NEARLY_ZERO) >= 0) {
				Vector3D intersectPointOnSphere2 = transformedRay.getVector()
						.scalarMultiply(intersectionDistance2)
						.add(transformedRay.getOrigin());

				Vector3D normal2 = intersectPointOnSphere2.normalize();
				double normalSign = FastMath.signum(transformedRay.getVector().negate().dotProduct(normal2));
				normal2 = normal2.scalarMultiply(normalSign).normalize();

				Material leaving = Material.AIR, entering = Material.AIR;
				if (isIntersectionDistance1Smaller)
					leaving = getMaterial();
				else
					entering = getMaterial();

				results.add(localToWorld(new Intersection<Shape>(intersectPointOnSphere2, normal2, transformedRay, this,
						this.getDiffuseColorScheme(), this.getSpecularColorScheme(), this.getEmissiveColorScheme(),
						leaving, entering)));
			}

		return results;
	}

	@Override
	public Sphere copy() {

		Sphere sphere = new Sphere();
		sphere = configureCopy(sphere);
		return sphere;
	}

	@Override
	public Vector3D getNormalRelativeTo(Vector3D localPoint) {

		return localPoint.normalize();
	}

}
