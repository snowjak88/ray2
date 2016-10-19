package org.snowjak.rays.shape;

import static org.apache.commons.math3.util.FastMath.pow;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Locatable;
import org.snowjak.rays.Prototype;
import org.snowjak.rays.Ray;
import org.snowjak.rays.color.ColorScheme;
import org.snowjak.rays.color.HasColorScheme;
import org.snowjak.rays.color.SimpleColorScheme;
import org.snowjak.rays.function.Functions;
import org.snowjak.rays.intersect.Intersectable;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.material.HasMaterial;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.transform.Transformable;
import org.snowjak.rays.transform.Transformer;
import org.snowjak.rays.world.World;

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

	/**
	 * By default, this Shape will use this Material.
	 */
	public static final Material DEFAULT_MATERIAL = new Material(Functions.constant(0d), Functions.constant(0d), Functions.constant(1d));

	private final Deque<Transformer> transformers = new LinkedList<>();

	private ColorScheme diffuseColorScheme = DEFAULT_COLOR_SCHEME, specularColorScheme = DEFAULT_SPECULAR_COLOR_SCHEME,
			emissiveColorScheme = DEFAULT_EMISSIVE_COLOR_SCHEME;

	private Material material = DEFAULT_MATERIAL;

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

		return getIntersections(ray, false, true).stream().findFirst();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersections(Ray ray) {

		return getIntersections(ray, false, false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersections(Ray ray, boolean includeBehindRayOrigin) {

		return getIntersections(ray, includeBehindRayOrigin, false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public abstract List<Intersection<Shape>> getIntersections(Ray ray, boolean includeBehindRayOrigin,
			boolean onlyReturnClosest);

	@Override
	public boolean isInside(Vector3D point) {

		Vector3D centerToPoint = point.subtract(getLocation());
		//
		// If the given point is "close enough" to the center, then this test is
		// trivially true.
		if (Double.compare(centerToPoint.getNorm(), World.NEARLY_ZERO) <= 0)
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
	 * Perform a quick check to see if this (object-local) {@link Ray}
	 * (interpreted as a line of infinite length) will intersect a
	 * bounding-sphere of a given radius, located at (0,0,0). Can be used as a
	 * quick-and-dirty Ray-rejection test for more complex Shapes.
	 * 
	 * @param localRay
	 * @param radius_squared
	 * @return
	 */
	protected boolean isIntersectWithBoundingSphere(Ray localRay, double radius_squared) {

		//
		// O = sphere origin
		// P = ray origin
		//
		//
		// L = O - P
		// but O is (0,0,0)
		// and negating P is irrelevant.
		// so L = P
		//
		Vector3D L = localRay.getOrigin();
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

	/**
	 * Tests if the given {@link Ray} (interpreted as a line of infinite length)
	 * intersects an Axis-Aligned Bounding Box centered on (0,0,0), and
	 * possessed of the given side-lengths.
	 * 
	 * @param localRay
	 * @param xLength
	 * @param yLength
	 * @param zLength
	 * @return
	 */
	protected boolean isIntersectsWithAABB(Ray localRay, double xLength, double yLength, double zLength) {

		double halfXLength = xLength / 2d, halfYLength = yLength / 2d, halfZLength = zLength / 2d;

		boolean ignoreX = false, ignoreY = false, ignoreZ = false;
		double px = localRay.getOrigin().getX(), py = localRay.getOrigin().getY(), pz = localRay.getOrigin().getZ();
		double vx = localRay.getVector().getX(), vy = localRay.getVector().getY(), vz = localRay.getVector().getZ();

		if (Double.compare(vx, 0d) == 0)
			ignoreX = true;
		if (Double.compare(vy, 0d) == 0)
			ignoreY = true;
		if (Double.compare(vz, 0d) == 0)
			ignoreZ = true;

		double t_x0 = (ignoreX ? -1 : solveForT(px, vx, -halfXLength)),
				t_y0 = (ignoreY ? -1 : solveForT(py, vy, -halfYLength)),
				t_z0 = (ignoreZ ? -1 : solveForT(pz, vz, -halfZLength));
		double t_x1 = (ignoreX ? Double.MAX_VALUE : solveForT(px, vx, +halfXLength)),
				t_y1 = (ignoreY ? Double.MAX_VALUE : solveForT(py, vy, +halfYLength)),
				t_z1 = (ignoreZ ? Double.MAX_VALUE : solveForT(pz, vz, +halfZLength));

		if (t_x0 > t_x1) {
			double temp = t_x0;
			t_x0 = t_x1;
			t_x1 = temp;
		}
		if (t_y0 > t_y1) {
			double temp = t_y0;
			t_y0 = t_y1;
			t_y1 = temp;
		}
		if (t_z0 > t_z1) {
			double temp = t_z0;
			t_z0 = t_z1;
			t_z1 = temp;
		}

		double t0 = FastMath.max(FastMath.max(t_x0, t_y0), t_z0);
		double t1 = FastMath.max(FastMath.max(t_x1, t_y1), t_z1);

		Vector3D intersectionPoint = localRay.getOrigin().add(localRay.getVector().scalarMultiply(t0));
		if (Double.compare(FastMath.abs(intersectionPoint.getX()) - 1d, World.NEARLY_ZERO) <= 0
				&& Double.compare(FastMath.abs(intersectionPoint.getY()) - 1d, World.NEARLY_ZERO) <= 0
				&& Double.compare(FastMath.abs(intersectionPoint.getZ()) - 1d, World.NEARLY_ZERO) <= 0)
			return true;

		intersectionPoint = localRay.getOrigin().add(localRay.getVector().scalarMultiply(t1));
		if (Double.compare(FastMath.abs(intersectionPoint.getX()) - 1d, World.NEARLY_ZERO) <= 0
				&& Double.compare(FastMath.abs(intersectionPoint.getY()) - 1d, World.NEARLY_ZERO) <= 0
				&& Double.compare(FastMath.abs(intersectionPoint.getZ()) - 1d, World.NEARLY_ZERO) <= 0)
			return true;

		return false;
	}

	/**
	 * <p>
	 * P + Vt = solution
	 * </p>
	 * <p>
	 * therefore:
	 * </p>
	 * <p>
	 * t = (solution - P) / V
	 * </p>
	 * 
	 * @param p
	 * @param v
	 * @param sol
	 * @return
	 */
	private double solveForT(double p, double v, double sol) {

		return (sol - p) / v;
	}

}
