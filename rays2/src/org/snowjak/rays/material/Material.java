package org.snowjak.rays.material;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Function;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Prototype;
import org.snowjak.rays.color.ColorScheme;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.function.Functions;
import org.snowjak.rays.transform.Transformable;
import org.snowjak.rays.transform.Transformer;

import javafx.scene.paint.Color;

/**
 * A Material defines an object's visible properties, like a {@link ColorScheme}
 * -- but unlike that, a Material has depth, defining an object's internal as
 * well as surface properties.
 * 
 * @author snowjak88
 *
 */
public class Material implements Transformable, Prototype<Material> {

	private Function<Vector3D, RawColor> color;

	private Function<Vector3D, Double> reflecvitiy, density, refractiveIndex;

	private Deque<Transformer> transformers = new LinkedList<>();

	/**
	 * Predefined Material: totally transparent, with a refractive index of 1.0
	 */
	public static final Material AIR = new Material(Functions.constant(Color.WHITE), Functions.constant(0d),
			Functions.constant(0d), Functions.constant(1d));

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

		return new Material(Functions.lerp(material1.getColor(point1), point1, material2.getColor(point2), point2),
				Functions.lerp(material1.getReflectivity(point1), point1, material2.getReflectivity(point2), point2),
				Functions.lerp(material1.getDensity(point1), point1, material2.getDensity(point2), point2),
				Functions.lerp(material1.getRefractiveIndex(point1), point1, material2.getRefractiveIndex(point2),
						point2));
	}

	/**
	 * Create a new Material.
	 */
	public Material() {
		this(Functions.constant(Color.HOTPINK), Functions.constant(0d), Functions.constant(0d), Functions.constant(1d));
	}

	/**
	 * Create a new Material with the specified visual properties.
	 * 
	 * @param surfaceColor
	 * @param reflectivity
	 * @param density
	 * @param refractiveIndex
	 */
	public Material(Function<Vector3D, RawColor> surfaceColor, Function<Vector3D, Double> reflectivity,
			Function<Vector3D, Double> density, Function<Vector3D, Double> refractiveIndex) {

		this.color = surfaceColor;
		this.reflecvitiy = reflectivity;
		this.density = density;
		this.refractiveIndex = refractiveIndex;
	}

	@Override
	public Deque<Transformer> getTransformers() {

		return transformers;
	}

	/**
	 * Set this Material's surface-color function
	 * 
	 * @param surfaceColor
	 */
	public void setSurfaceColor(Function<Vector3D, RawColor> surfaceColor) {

		this.color = surfaceColor;
	}

	/**
	 * Set this Material's surface-color function to a constant color
	 * 
	 * @param surfaceColor
	 */
	public void setSurfaceColor(RawColor surfaceColor) {

		this.color = Functions.constant(surfaceColor);
	}

	/**
	 * Set this Material's reflectivity-fraction function
	 * 
	 * @param reflectivity
	 */
	public void setReflectivity(Function<Vector3D, Double> reflectivity) {

		this.reflecvitiy = reflectivity;
	}

	/**
	 * Set this Material's reflectivity-fraction function to a constant value
	 * 
	 * @param reflectivity
	 */
	public void setReflectivity(double reflectivity) {

		this.reflecvitiy = Functions.constant(reflectivity);
	}

	/**
	 * Set this Material's density-fraction function
	 * 
	 * @param density
	 */
	public void setDensity(Function<Vector3D, Double> density) {

		this.density = density;
	}

	/**
	 * Set this Material's density-fraction function to a constant value
	 * 
	 * @param density
	 */
	public void setDensity(double density) {

		this.density = Functions.constant(density);
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
	 * @return this Material's color function
	 */
	public Function<Vector3D, RawColor> getSurfaceColor() {

		return color;
	}

	/**
	 * @param localPoint
	 * @return this Material's color at the given point
	 */
	public RawColor getColor(Vector3D localPoint) {

		return color.apply(localPoint);
	}

	/**
	 * 
	 * @return this Material's reflectivity-fraction function
	 */
	public Function<Vector3D, Double> getReflectivity() {

		return reflecvitiy;
	}

	/**
	 * @param localPoint
	 * @return this Material's reflectivity-fraction at the given point
	 */
	public double getReflectivity(Vector3D localPoint) {

		return reflecvitiy.apply(localPoint);
	}

	/**
	 * 
	 * @return this Material's density-fraction function
	 */
	public Function<Vector3D, Double> getDensity() {

		return density;
	}

	/**
	 * @param localPoint
	 * @return this Material's density-fraction at the given point
	 */
	public double getDensity(Vector3D localPoint) {

		return density.apply(localPoint);
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

		Material copy = new Material(color, reflecvitiy, density, refractiveIndex);
		copy.getTransformers().addAll(getTransformers());
		return copy;
	}

}
