package org.snowjak.rays.intersect;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.color.ColorScheme;
import org.snowjak.rays.color.HasColorScheme;
import org.snowjak.rays.material.Material;

/**
 * Represents a point of contact between an object (i.e., an
 * {@link Intersectable}) and a Ray.
 * 
 * @author snowjak88
 * @param <S>
 *            an Intersectable type
 *
 */
public class Intersection<S extends Intersectable> implements HasColorScheme {

	private Vector3D point, normal;

	private double distanceFromRayOrigin;

	private Ray ray;

	private S intersected;

	private ColorScheme diffuseColorScheme, specularColorScheme, emissiveColorScheme;

	private Material leavingMaterial, enteringMaterial;

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

	/**
	 * Construct a new Intersection, explicitly specifying the distance between
	 * the intersection-point and the ray-origin.
	 * 
	 * @param point
	 * @param normal
	 * @param ray
	 * @param intersected
	 * @param distanceFromRayOrigin
	 */
	public Intersection(Vector3D point, Vector3D normal, Ray ray, S intersected, double distanceFromRayOrigin) {
		this.point = point;
		this.normal = normal;
		this.distanceFromRayOrigin = distanceFromRayOrigin;
		this.ray = ray;
		this.intersected = intersected;
	}

	/**
	 * Construct a new Intersection, explicitly specifying the various
	 * ColorSchemes associated with it.
	 * 
	 * @param point
	 * @param normal
	 * @param ray
	 * @param intersected
	 * @param ambientColorScheme
	 * @param diffuseColorScheme
	 * @param specularColorScheme
	 * @param emissiveColorScheme
	 * @param leavingMaterial
	 * @param enteringMaterial
	 */
	public Intersection(Vector3D point, Vector3D normal, Ray ray, S intersected, ColorScheme diffuseColorScheme,
			ColorScheme specularColorScheme, ColorScheme emissiveColorScheme, Material leavingMaterial,
			Material enteringMaterial) {
		this.point = point;
		this.normal = normal;
		this.distanceFromRayOrigin = point.distance(ray.getOrigin());
		this.ray = ray;
		this.intersected = intersected;
		this.diffuseColorScheme = diffuseColorScheme;
		this.specularColorScheme = specularColorScheme;
		this.emissiveColorScheme = emissiveColorScheme;
		this.leavingMaterial = leavingMaterial;
		this.enteringMaterial = enteringMaterial;
	}

	/**
	 * Create a new Intersection, explicitly specifying the various ColorSchemes
	 * associated with it as well as the distance between the intersection-point
	 * and the ray-origin.
	 * 
	 * @param point
	 * @param normal
	 * @param ray
	 * @param intersected
	 * @param distanceFromRayOrigin
	 * @param ambientColorScheme
	 * @param diffuseColorScheme
	 * @param specularColorScheme
	 * @param emissiveColorScheme
	 * @param leavingMaterial
	 * @param enteringMaterial
	 */
	public Intersection(Vector3D point, Vector3D normal, Ray ray, S intersected, double distanceFromRayOrigin,
			ColorScheme diffuseColorScheme, ColorScheme specularColorScheme, ColorScheme emissiveColorScheme,
			Material leavingMaterial, Material enteringMaterial) {
		this.point = point;
		this.normal = normal;
		this.distanceFromRayOrigin = distanceFromRayOrigin;
		this.ray = ray;
		this.intersected = intersected;
		this.diffuseColorScheme = diffuseColorScheme;
		this.specularColorScheme = specularColorScheme;
		this.emissiveColorScheme = emissiveColorScheme;
		this.leavingMaterial = leavingMaterial;
		this.enteringMaterial = enteringMaterial;
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

	/**
	 * @return the Material that this Intersection is leaving (proceeding along
	 *         the Ray from its origin)
	 */
	public Material getLeavingMaterial() {

		return leavingMaterial;
	}

	/**
	 * @return the Material that this Intersection is entering (proceeding along
	 *         the Ray from its origin)
	 */
	public Material getEnteringMaterial() {

		return enteringMaterial;
	}
}
