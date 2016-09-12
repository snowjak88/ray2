package org.snowjak.rays.material;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Function;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
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
public class Material implements Transformable {

	private Function<Vector3D, RawColor> surfaceColor, internalColor, reflectiveColor;

	private Function<Vector3D, Double> transparency, reflectivity, refractiveIndex;

	private Deque<Transformer> transformers = new LinkedList<>();

	/**
	 * Predefined Material: totally transparent, with a refractive index of 1.0
	 */
	public static final Material AIR = new Material(Functions.constant(Color.WHITE), Functions.constant(Color.WHITE),
			Functions.constant(Color.WHITE), Functions.constant(1d), Functions.constant(0d), Functions.constant(1d));

	/**
	 * Create a new Material.
	 */
	public Material() {
		this(Functions.constant(Color.HOTPINK), Functions.constant(Color.WHITE), Functions.constant(Color.WHITE),
				Functions.constant(0d), Functions.constant(0d), Functions.constant(1d));
	}

	/**
	 * Create a new Material with the specified visual properties.
	 * 
	 * @param surfaceColor
	 * @param internalColor
	 * @param reflectiveColor
	 * @param transparency
	 * @param reflectivity
	 * @param refractiveIndex
	 */
	public Material(Function<Vector3D, RawColor> surfaceColor, Function<Vector3D, RawColor> internalColor,
			Function<Vector3D, RawColor> reflectiveColor, Function<Vector3D, Double> transparency,
			Function<Vector3D, Double> reflectivity, Function<Vector3D, Double> refractiveIndex) {

		this.surfaceColor = surfaceColor;
		this.internalColor = internalColor;
		this.reflectiveColor = reflectiveColor;
		this.transparency = transparency;
		this.reflectivity = reflectivity;
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

		this.surfaceColor = surfaceColor;
	}

	/**
	 * Set this Material's surface-color function to a constant color
	 * 
	 * @param surfaceColor
	 */
	public void setSurfaceColor(RawColor surfaceColor) {

		this.surfaceColor = Functions.constant(surfaceColor);
	}

	/**
	 * Set this Material's internal-color function
	 * 
	 * @param internalColor
	 */
	public void setInternalColor(Function<Vector3D, RawColor> internalColor) {

		this.internalColor = internalColor;
	}

	/**
	 * Set this Material's internal-color function to a constant color
	 * 
	 * @param internalColor
	 */
	public void setInternalColor(RawColor internalColor) {

		this.internalColor = Functions.constant(internalColor);
	}

	/**
	 * Set this Material's reflective-color function
	 * 
	 * @param reflectiveColor
	 */
	public void setReflectiveColor(Function<Vector3D, RawColor> reflectiveColor) {

		this.reflectiveColor = reflectiveColor;
	}

	/**
	 * Set this Material's reflective-color function to a constant color
	 * 
	 * @param reflectiveColor
	 */
	public void setReflectiveColor(RawColor reflectiveColor) {

		this.reflectiveColor = Functions.constant(reflectiveColor);
	}

	/**
	 * Set this Material's transparency-fraction function
	 * 
	 * @param transparency
	 */
	public void setTransparency(Function<Vector3D, Double> transparency) {

		this.transparency = transparency;
	}

	/**
	 * Set this Material's transparency-fraction function to a constant value
	 * 
	 * @param transparency
	 */
	public void setTransparency(double transparency) {

		this.transparency = Functions.constant(transparency);
	}

	/**
	 * Set this Material's reflectivity-fraction function
	 * 
	 * @param reflectivity
	 */
	public void setReflectivity(Function<Vector3D, Double> reflectivity) {

		this.reflectivity = reflectivity;
	}

	/**
	 * Set this Material's reflectivity-fraction function to a constant value
	 * 
	 * @param reflectivity
	 */
	public void setReflectivity(double reflectivity) {

		this.reflectivity = Functions.constant(reflectivity);
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
	 * @return this Material's surface-color function
	 */
	public Function<Vector3D, RawColor> getSurfaceColor() {

		return surfaceColor;
	}

	/**
	 * @param localPoint
	 * @return this Material's surface-color at the given point
	 */
	public RawColor getSurfaceColor(Vector3D localPoint) {

		return surfaceColor.apply(localPoint);
	}

	/**
	 * @return this Material's internal-color function
	 */
	public Function<Vector3D, RawColor> getInternalColor() {

		return internalColor;
	}

	/**
	 * @param localPoint
	 * @return this Material's internal-color at the given point
	 */
	public RawColor getInternalColor(Vector3D localPoint) {

		return internalColor.apply(localPoint);
	}

	/**
	 * @return this Material's reflective-color function
	 */
	public Function<Vector3D, RawColor> getReflectiveColor() {

		return reflectiveColor;
	}

	/**
	 * @param localPoint
	 * @return this Material's reflective-color at the given point
	 */
	public RawColor getReflectiveColor(Vector3D localPoint) {

		return reflectiveColor.apply(localPoint);
	}

	/**
	 * @return this Material's transparency-fraction function
	 */
	public Function<Vector3D, Double> getTransparency() {

		return transparency;
	}

	/**
	 * @param localPoint
	 * @return this Material's transparency-fraction at the given point
	 */
	public double getTransparency(Vector3D localPoint) {

		return transparency.apply(localPoint);
	}

	/**
	 * @return this Material's reflectivity-fraction function
	 */
	public Function<Vector3D, Double> getReflectivity() {

		return reflectivity;
	}

	/**
	 * @param localPoint
	 * @return this Material's reflectivity-fraction at the given point
	 */
	public double getReflectivity(Vector3D localPoint) {

		return reflectivity.apply(localPoint);
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

}
