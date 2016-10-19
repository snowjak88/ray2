package org.snowjak.rays.material;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Function;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Prototype;
import org.snowjak.rays.color.ColorScheme;
import org.snowjak.rays.function.Functions;
import org.snowjak.rays.transform.Transformable;
import org.snowjak.rays.transform.Transformer;

/**
 * A Material defines an object's visible properties, like a {@link ColorScheme}
 * -- but unlike that, a Material has depth, defining an object's internal as
 * well as surface properties.
 * 
 * @author snowjak88
 *
 */
public class Material implements Transformable, Prototype<Material> {

	private Function<Vector3D, Double> surfaceTransparency, refractiveIndex, albedo;

	private final Deque<Transformer> transformers = new LinkedList<>();

	/**
	 * Predefined Material: totally transparent, with a refractive index of 1.0
	 */
	public static final Material AIR = new Material(Functions.constant(1d), Functions.constant(0d),
			Functions.constant(1d));

	/**
	 * Create a new Material by linearly-interpolating all visual properties of
	 * one Material (at {@code point1}) to another (at {@code point2}).
	 * 
	 * @param material1
	 * @param point1
	 * @param material2
	 * @param point2
	 * @return a new Material defined as a linear interpolation of two other
	 *         Materials
	 */
	public static Material blend(Material material1, Vector3D point1, Material material2, Vector3D point2) {

		return new Material(
				Functions.lerp(material1.getSurfaceTransparency(point1), point1,
						material2.getSurfaceTransparency(point2), point2),
				Functions.constant(0d), Functions.lerp(material1.getRefractiveIndex(point1), point1,
						material2.getRefractiveIndex(point2), point2));
	}

	/**
	 * Create a new Material -- totally opaque, with a refractive index of 1.0
	 */
	public Material() {
		this(Functions.constant(0d), Functions.constant(0d), Functions.constant(1d));
	}

	/**
	 * Create a new Material with the specified visual properties.
	 * 
	 * @param surfaceTransparency
	 * @param albedo
	 * @param refractiveIndex
	 */
	public Material(Function<Vector3D, Double> surfaceTransparency, Function<Vector3D, Double> albedo,
			Function<Vector3D, Double> refractiveIndex) {

		this.surfaceTransparency = surfaceTransparency;
		this.albedo = albedo;
		this.refractiveIndex = refractiveIndex;
	}

	@Override
	public Deque<Transformer> getTransformers() {

		return transformers;
	}

	/**
	 * Set this Material's surface-transparency-fraction function
	 * 
	 * @param surfaceTransparency
	 */
	public void setSurfaceTransparency(Function<Vector3D, Double> surfaceTransparency) {

		this.surfaceTransparency = surfaceTransparency;
	}

	/**
	 * Set this Material's surface-transparency-fraction function to a constant
	 * value
	 * 
	 * @param surfaceTransparency
	 */
	public void setSurfaceTransparency(double surfaceTransparency) {

		this.surfaceTransparency = Functions.constant(surfaceTransparency);
	}

	/**
	 * Set this Material's albedo function
	 * 
	 * @param albedo
	 */
	public void setAlbedo(Function<Vector3D, Double> albedo) {

		this.albedo = albedo;
	}

	/**
	 * Set this Material's albedo function to a constant value
	 * 
	 * @param albedo
	 */
	public void setAlbedo(double albedo) {

		this.albedo = Functions.constant(albedo);
	}

	/**
	 * Set this Material's refractive-index function
	 * 
	 * @param refractiveIndex
	 */
	public void setRefractiveIndex(Function<Vector3D, Double> refractiveIndex) {

		this.refractiveIndex = refractiveIndex;
	}

	/**
	 * Set this Material's refractive-index function to a constant value
	 * 
	 * @param refractiveIndex
	 */
	public void setRefractiveIndex(double refractiveIndex) {

		this.refractiveIndex = Functions.constant(refractiveIndex);
	}

	/**
	 * 
	 * @return this Material's surface-transparency-fraction function
	 */
	public Function<Vector3D, Double> getSurfaceTransparency() {

		return surfaceTransparency;
	}

	/**
	 * @param localPoint
	 * @return this Material's surface-transparency-fraction at the given point
	 */
	public double getSurfaceTransparency(Vector3D localPoint) {

		return surfaceTransparency.apply(localPoint);
	}

	/**
	 * @return this Material's albedo function
	 */
	public Function<Vector3D, Double> getAlbedo() {

		return albedo;
	}

	/**
	 * @param localPoint
	 * @return this Material's albedo at the given point
	 */
	public double getAlbedo(Vector3D localPoint) {

		return albedo.apply(localPoint);
	}

	/**
	 * @return this Material's refractive-index function
	 */
	public Function<Vector3D, Double> getRefractiveIndex() {

		return refractiveIndex;
	}

	/**
	 * @param localPoint
	 * @return this Material's index of refraction at the given point
	 */
	public double getRefractiveIndex(Vector3D localPoint) {

		return refractiveIndex.apply(localPoint);
	}

	@Override
	public Material copy() {

		Material copy = new Material(surfaceTransparency, albedo, refractiveIndex);
		copy.getTransformers().addAll(getTransformers());
		return copy;
	}

}
