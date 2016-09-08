package org.snowjak.rays.shape;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.transform.Translation;

/**
 * Represents an open-ended cylinder aligned with the Y-axis, centered on
 * (0,0,0), with a radius of 1, and an extension along the Y-axis from [-1] to
 * [1].
 * 
 * @author snowjak88
 *
 */
public class Cylinder extends Shape {

	private boolean isMinusYCapped, isPlusYCapped;

	private Plane minusYCap, plusYCap;

	/**
	 * Create a new Cylinder, with open (uncapped) ends.
	 */
	public Cylinder() {
		this(false);
	}

	/**
	 * Create a new Cylinder, specifying if both ends are to be capped or not.
	 * 
	 * @param isCapped
	 */
	public Cylinder(boolean isCapped) {
		this(isCapped, isCapped);
	}

	/**
	 * Create a new Cylinder, specifying if each end is to be capped or not.
	 * 
	 * @param isMinusYCapped
	 * @param isPlusYCapped
	 */
	public Cylinder(boolean isMinusYCapped, boolean isPlusYCapped) {
		this.isMinusYCapped = isMinusYCapped;
		this.isPlusYCapped = isPlusYCapped;

		this.minusYCap = new Plane();
		minusYCap.getTransformers().add(new Translation(0d, -1d, 0d));

		this.plusYCap = new Plane();
		plusYCap.getTransformers().add(new Translation(0d, 1d, 0d));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersectionsIncludingBehind(Ray ray) {

		Ray localRay = worldToLocal(ray);
		Vector3D localLocation = worldToLocal(getLocation());
		List<Intersection<Shape>> results = new LinkedList<>();
		//
		//
		// Here we take the sphere-intersection detection routine from our
		// Sphere primitive, and adapt it for 2 dimensions (X,Z).
		//
		// For explanation of this routine, see the comments in
		// org.snowjak.rays.shape.Sphere

		Vector2D rayOrigin = new Vector2D(localRay.getOrigin().getX(), localRay.getOrigin().getZ());
		Vector2D rayVector = new Vector2D(localRay.getVector().getX(), localRay.getVector().getZ()).normalize();
		Vector2D circleOrigin = new Vector2D(localLocation.getX(), localLocation.getZ());

		Vector2D L = circleOrigin.subtract(rayOrigin);
		double t_ca = rayVector.dotProduct(L);
		double d2 = L.getNormSq() - FastMath.pow(t_ca, 2d);

		double r2 = 1d;
		if (Double.compare(d2, r2) > 0)
			return Collections.emptyList();

		double t_hc = FastMath.sqrt(r2 - d2);

		boolean onlyOneSolution = false;
		if (Double.compare(t_hc, World.DOUBLE_ERROR) <= 0)
			onlyOneSolution = true;

		double t1_2d = t_ca - t_hc;
		double t2_2d = t_ca + t_hc;
		//
		// Now we can determine the intersection-point(s) in 2D space.
		Vector2D intersectionPoint1_2D = rayOrigin.add(rayVector.scalarMultiply(t1_2d));
		Vector2D intersectionPoint2_2D = rayOrigin.add(rayVector.scalarMultiply(t2_2d));
		//
		// And translate those 2D intersection-points into 3D equivalents.
		double t1 = (intersectionPoint1_2D.getX() - localRay.getOrigin().getX()) / localRay.getVector().getX();
		double t2 = (intersectionPoint2_2D.getX() - localRay.getOrigin().getX()) / localRay.getVector().getX();

		Vector3D intersectionPoint1 = localRay.getOrigin().add(localRay.getVector().scalarMultiply(t1));
		Vector3D intersectionPoint2 = localRay.getOrigin().add(localRay.getVector().scalarMultiply(t2));

		//
		//
		// Now -- are the ends of this cylinder capped?
		//
		// Test each of the capped ends for an intersection.
		// If any intersection exists, test if it lies within the circle of this
		// cylinder.
		//
		if (isMinusYCapped) {

			//
			// Remember that a circle is x^2 + y^2 = r^2
			// or, for points inside the circle:
			// x^2 + y^2 <= r^2
			results.addAll(minusYCap.getIntersectionsIncludingBehind(localRay)
					.parallelStream()
					.filter(i -> Double.compare(
							FastMath.pow(i.getPoint().getX(), 2d) + FastMath.pow(i.getPoint().getZ(), 2d), 1d) <= 0)
					.map(i -> new Intersection<Shape>(i.getPoint(), i.getNormal(), i.getRay(), this,
							getAmbientColorScheme(), getDiffuseColorScheme(), getSpecularColorScheme(),
							getEmissiveColorScheme()))
					.collect(Collectors.toCollection(LinkedList::new)));
		}

		if (isPlusYCapped) {
			results.addAll(plusYCap.getIntersectionsIncludingBehind(localRay)
					.parallelStream()
					.filter(i -> Double.compare(
							FastMath.pow(i.getPoint().getX(), 2d) + FastMath.pow(i.getPoint().getZ(), 2d), 1d) <= 0)
					.map(i -> new Intersection<Shape>(i.getPoint(), i.getNormal(), i.getRay(), this,
							getAmbientColorScheme(), getDiffuseColorScheme(), getSpecularColorScheme(),
							getEmissiveColorScheme()))
					.collect(Collectors.toCollection(LinkedList::new)));
		}

		//
		//
		// Now we've determined the intersection(s) to the cylinder in 3D-space.
		// Time to see if those intersections are within the bounds of this
		// cylinder.
		//
		if (Double.compare(intersectionPoint1.getY(), -1d) >= 0 && Double.compare(intersectionPoint1.getY(), 1d) <= 0) {

			// We need to ensure that the reported surface normal is facing
			// toward the intersecting ray.
			// After all, it is possible to see on both sides of the
			// cylinder's surface.
			Vector3D normal = new Vector3D(intersectionPoint1.getX(), 0d, intersectionPoint1.getZ()).normalize();
			double normalSign = FastMath.signum(localRay.getVector().negate().dotProduct(normal));
			normal = normal.scalarMultiply(normalSign);

			results.add(new Intersection<Shape>(intersectionPoint1, normal, localRay, this, getAmbientColorScheme(),
					getDiffuseColorScheme(), getSpecularColorScheme(), getEmissiveColorScheme()));
		}

		if (onlyOneSolution && results.size() == 1)
			return results;

		if (Double.compare(intersectionPoint2.getY(), -1d) >= 0 && Double.compare(intersectionPoint2.getY(), 1d) <= 0) {

			Vector3D normal = new Vector3D(intersectionPoint2.getX(), 0d, intersectionPoint2.getZ()).normalize();
			double normalSign = FastMath.signum(localRay.getVector().negate().dotProduct(normal));
			normal = normal.scalarMultiply(normalSign);

			results.add(new Intersection<Shape>(intersectionPoint2, normal, localRay, this, getAmbientColorScheme(),
					getDiffuseColorScheme(), getSpecularColorScheme(), getEmissiveColorScheme()));
		}

		return results.parallelStream().map(i -> localToWorld(i)).collect(Collectors.toCollection(LinkedList::new));
	}

	@Override
	public Cylinder copy() {

		Cylinder newCylinder = new Cylinder(this.isMinusYCapped, this.isPlusYCapped);
		newCylinder = configureCopy(newCylinder);

		return newCylinder;
	}

	@Override
	public Vector3D getNormalRelativeTo(Vector3D localPoint) {

		Vector3D normal = localPoint.normalize();
		return new Vector3D(normal.getX(), 0d, normal.getZ());
	}

}
