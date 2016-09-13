package org.snowjak.rays.shape;

import static org.apache.commons.math3.util.FastMath.pow;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Locatable;
import org.snowjak.rays.Prototype;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.color.ColorScheme;
import org.snowjak.rays.color.HasColorScheme;
import org.snowjak.rays.color.SimpleColorScheme;
import org.snowjak.rays.intersect.Intersectable;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.material.HasMaterial;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.transform.Transformable;
import org.snowjak.rays.transform.Transformer;

import javafx.scene.paint.Color;

/**
 * Represents an "object" in a 3D space. Something susceptible of being placed,
 * transformed, and intersected with {@link Ray}s.
 * 
 * @author snowjak88
 *
 */
public abstract class Shape
		implements Transformable, Locatable, Intersectable, HasColorScheme, HasMaterial, Prototype<Shape> {

	protected static final double ROOT_2 = FastMath.sqrt(2d);
	
	/**
	 * By default, the ambient and diffuse color-schemes will take this value.
	 */
	public static final ColorScheme DEFAULT_COLOR_SCHEME = new SimpleColorScheme(Color.HOTPINK);

	/**
	 * By default, the specular color-scheme will take this value.
	 */
	public static final ColorScheme DEFAULT_SPECULAR_COLOR_SCHEME = new SimpleColorScheme(Color.WHITE);

	/**
	 * By default, the emissive color-scheme will take this value.
	 */
	public static final ColorScheme DEFAULT_EMISSIVE_COLOR_SCHEME = new SimpleColorScheme(Color.BLACK);

	private Deque<Transformer> transformers = new LinkedList<>();

	private ColorScheme diffuseColorScheme = DEFAULT_COLOR_SCHEME, specularColorScheme = DEFAULT_SPECULAR_COLOR_SCHEME,
			emissiveColorScheme = DEFAULT_EMISSIVE_COLOR_SCHEME;

	private Material material = new Material();

	/**
	 * Default, no-action constructor.
	 */
	public Shape() {

	}

	@Override
	public Deque<Transformer> getTransformers() {

		return transformers;
	}

	@Override
	public Vector3D getLocation() {

		return localToWorld(Vector3D.ZERO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<Intersection<Shape>> getIntersection(Ray ray) {

		return getIntersections(ray).stream()
				.sorted((i1, i2) -> Double.compare(i1.getDistanceFromRayOrigin(), i2.getDistanceFromRayOrigin()))
				.findFirst();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersections(Ray ray) {

		List<Intersection<Shape>> intersectionsIncludingBehind = getIntersectionsIncludingBehind(ray);

		return intersectionsIncludingBehind.parallelStream().filter(i -> {
			Vector3D rayToIntersection = i.getPoint().subtract(ray.getOrigin());
			return (Double.compare(rayToIntersection.getNorm(), World.DOUBLE_ERROR) <= 0
					|| rayToIntersection.normalize().dotProduct(ray.getVector()) >= 0d);
		}).collect(Collectors.toCollection(LinkedList::new));
	}

	@SuppressWarnings("unchecked")
	@Override
	public abstract List<Intersection<Shape>> getIntersectionsIncludingBehind(Ray ray);

	@Override
	public boolean isInside(Vector3D point) {

		Vector3D centerToPoint = point.subtract(getLocation());
		//
		// If the given point is "close enough" to the center, then this test is
		// trivially true.
		if (Double.compare(centerToPoint.getNorm(), World.DOUBLE_ERROR) <= 0)
			return true;
		//
		// Else, construct a Ray from the point away from the center, and look
		// for any intersections with this object.
		return !(getIntersections(new Ray(point, centerToPoint.normalize())).isEmpty());
	}

	public ColorScheme getDiffuseColorScheme() {

		return diffuseColorScheme;
	}

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

	@Override
	public Material getMaterial() {

		return material;
	}

	@Override
	public void setMaterial(Material material) {

		this.material = material;
	}

	/**
	 * As part of copying -- once you've created a new Shape instance, call this
	 * method to ensure all configuration is copied.
	 * 
	 * @param copy
	 *            the newly-created Shape instance, currently being configured
	 * @return the same Shape instance, with additional configuration copied
	 *         over from this instance
	 */
	protected <T extends Shape> T configureCopy(T copy) {

		copy.setDiffuseColorScheme(this.getDiffuseColorScheme().copy());
		copy.setSpecularColorScheme(this.getSpecularColorScheme().copy());
		copy.setEmissiveColorScheme(this.getEmissiveColorScheme().copy());
		copy.setMaterial(this.getMaterial().copy());
		copy.getTransformers().addAll(this.getTransformers());
		return copy;
	}

	/**
	 * {@inheritDoc Prototype#copy()}
	 * <p>
	 * For {@link Shape} instances, you will want to call
	 * {@link Shape#configureCopy(Shape)} as part of your implementation of this
	 * method. {@code configureCopy(Shape)} will copy all those fields declared
	 * on the Shape type.
	 * </p>
	 */
	@Override
	public abstract Shape copy();

	/**
	 * Perform a quick check to see if this (object-local) {@link Ray} will
	 * intersect a bounding-sphere of a given radius, located at (0,0,0). Can be
	 * used as a quick-and-dirty Ray-rejection test for more complex Shapes.
	 * 
	 * @param localRay
	 * @param radius_squared
	 * @return
	 */
	protected boolean isWithinBoundingSphere(Ray localRay, double radius_squared) {

		//
		// O = sphere origin
		// P = ray origin
		//
		//
		// L = O - P
		//
		Vector3D L = localRay.getOrigin().negate();
		//
		// v = ray vector (normalized)
		//
		// t_ca = v dot-product L
		//
		// t_ca = v . L
		double t_ca = localRay.getVector().dotProduct(L);
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
		return Double.compare(d2, radius_squared) <= 0;
	}

}
