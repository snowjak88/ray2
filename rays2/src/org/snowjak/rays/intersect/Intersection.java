package org.snowjak.rays.intersect;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.color.ColorScheme;
import org.snowjak.rays.color.HasColorScheme;

/**
 * Represents a point of contact between an object (i.e., an
 * {@link Intersectable}) and a Ray.
 * 
 * @author rr247200
 * @param <S>
 *            an Intersectable type
 *
 */
public class Intersection<S extends Intersectable> implements HasColorScheme {

	private Vector3D point, normal;

	private double distanceFromRayOrigin;

	private Ray ray;

	private S intersected;

	private ColorScheme ambientColorScheme, diffuseColorScheme, specularColorScheme, emissiveColorScheme;

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

		this(point, normal, ray, intersected, point.distance(ray.getOrigin()));
	}

	public Intersection(Vector3D point, Vector3D normal, Ray ray, S intersected, double distanceFromRayOrigin) {
		this.point = point;
		this.normal = normal;
		this.distanceFromRayOrigin = distanceFromRayOrigin;
		this.ray = ray;
		this.intersected = intersected;
	}

	public Intersection(Vector3D point, Vector3D normal, Ray ray, S intersected, ColorScheme ambientColorScheme,
			ColorScheme diffuseColorScheme, ColorScheme specularColorScheme, ColorScheme emissiveColorScheme) {
		this.point = point;
		this.normal = normal;
		this.distanceFromRayOrigin = point.distance(ray.getOrigin());
		this.ray = ray;
		this.intersected = intersected;
		this.ambientColorScheme = ambientColorScheme;
		this.diffuseColorScheme = diffuseColorScheme;
		this.specularColorScheme = specularColorScheme;
		this.emissiveColorScheme = emissiveColorScheme;
	}

	public Intersection(Vector3D point, Vector3D normal, Ray ray, S intersected, double distanceFromRayOrigin,
			ColorScheme ambientColorScheme, ColorScheme diffuseColorScheme, ColorScheme specularColorScheme,
			ColorScheme emissiveColorScheme) {
		this.point = point;
		this.normal = normal;
		this.distanceFromRayOrigin = distanceFromRayOrigin;
		this.ray = ray;
		this.intersected = intersected;
		this.ambientColorScheme = ambientColorScheme;
		this.diffuseColorScheme = diffuseColorScheme;
		this.specularColorScheme = specularColorScheme;
		this.emissiveColorScheme = emissiveColorScheme;
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

	@Override
	public ColorScheme getDiffuseColorScheme() {

		return diffuseColorScheme;
	}

	@Override
	public void setDiffuseColorScheme(ColorScheme diffuseColorScheme) {

		this.diffuseColorScheme = diffuseColorScheme;
	}

	@Override
	public ColorScheme getAmbientColorScheme() {

		return ambientColorScheme;
	}

	@Override
	public void setAmbientColorScheme(ColorScheme ambientColorScheme) {

		this.ambientColorScheme = ambientColorScheme;
	}

	@Override
	public ColorScheme getSpecularColorScheme() {

		return specularColorScheme;
	}

	@Override
	public void setSpecularColorScheme(ColorScheme specularColorScheme) {

		this.specularColorScheme = specularColorScheme;
	}

	@Override
	public ColorScheme getEmissiveColorScheme() {

		return emissiveColorScheme;
	}

	@Override
	public void setEmissiveColorScheme(ColorScheme emissiveColorScheme) {

		this.emissiveColorScheme = emissiveColorScheme;
	}
}
