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

/**
 * Represents a Sphere, centered on (0,0,0) (absent any transformations you may
 * apply).
 * 
 * @author rr247200
 *
 */
public class Sphere extends Shape {

	private double radius;

	/**
	 * Create a Sphere of radius {@code 1.0}
	 */
	public Sphere() {
		this(1d);
	}

	/**
	 * Create a Sphere of a given radius.
	 * 
	 * @param radius
	 */
	public Sphere(double radius) {
		super();
		this.radius = radius;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersectionsIncludingBehind(Ray ray) {

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
		double t_ca = transformedRay.getVector().normalize().dotProduct(L);
		//
		// d = shortest distance from center of sphere to ray
		//
		// d^2 = |L|^2 - t_ca^2
		//
		double d2 = pow(L.getNorm(), 2d) - pow(t_ca, 2d);
		//
		// r = sphere's radius
		//
		// Now -- if d > r, then this ray does *not* intersect this sphere!
		double r2 = pow(radius, 2d);
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

		List<Intersection<Shape>> results = new LinkedList<>();

		if (Double.compare(FastMath.abs(intersectionDistance1), World.DOUBLE_ERROR) >= 0) {
			Vector3D intersectPointOnSphere1 = transformedRay.getVector()
					.normalize()
					.scalarMultiply(intersectionDistance1)
					.add(transformedRay.getOrigin());

			Vector3D normal1 = intersectPointOnSphere1.normalize();
			results.add(localToWorld(new Intersection<Shape>(intersectPointOnSphere1, normal1, transformedRay, this,
					this.getAmbientColorScheme(), this.getDiffuseColorScheme(), this.getSpecularColorScheme(),
					this.getEmissiveColorScheme())));
		}

		if (Double.compare(FastMath.abs(intersectionDistance2), World.DOUBLE_ERROR) >= 0) {
			Vector3D intersectPointOnSphere2 = transformedRay.getVector()
					.normalize()
					.scalarMultiply(intersectionDistance2)
					.add(transformedRay.getOrigin());

			Vector3D normal2 = intersectPointOnSphere2.normalize();
			results.add(localToWorld(new Intersection<Shape>(intersectPointOnSphere2, normal2, transformedRay, this,
					this.getAmbientColorScheme(), this.getDiffuseColorScheme(), this.getSpecularColorScheme(),
					this.getEmissiveColorScheme())));
		}

		return results;
	}

}
