package org.snowjak.rays.light;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Locatable;
import org.snowjak.rays.Ray;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.transform.Transformable;
import org.snowjak.rays.transform.Transformer;

/**
 * Represents a light of some kind.
 * 
 * <p>
 * Any light will provide several kinds of light:
 * <dl>
 * <dt>Ambient</dt>
 * <dd>The amount of light visible everywhere. Prevents shadows from being
 * completely unlit and invisible. A rough approximation of indirect scattering.
 * Is <em>not</em> subject to falloff over distance.</dd>
 * <dt>Diffuse</dt>
 * <dd>Ordinary lighting. Provides light to see diffuse colors on objects.
 * Subject to modification by exposure.</dd>
 * </dl>
 * </p>
 * <p>
 * <strong>"Exposure"</strong> refers to the amount of light actually reflected
 * off of a surface. Ordinarily, this is calculated by the following:
 * 
 * <pre>
 * exposure = cos(theta)
 * or
 * exposure = N . L   [i.e., dot-product]
 * </pre>
 * 
 * where {@code theta} is the angle between two vectors:
 * <ol>
 * <li>N := surface normal at a particular point</li>
 * <li>L := direction from that point to this light</li>
 * </ol>
 * </p>
 * 
 * @author rr247200
 *
 */
public class Light implements Transformable, Locatable {

	private Deque<Transformer> transformers = new LinkedList<>();

	private Function<Ray, RawColor> ambientIntensityFunction, diffuseIntensityFunction, specularIntensityFunction;

	private BiFunction<Light, Intersection<Shape>, Double> exposureFunction;

	/**
	 * Create a new Light with given light intensities, and the normal exposure
	 * function (i.e., cosine of angle between light and surface-normal
	 * vectors).
	 * 
	 * @param ambientIntensity
	 * @param diffuseIntensity
	 * @param specularIntensity
	 */
	public Light(RawColor ambientIntensity, RawColor diffuseIntensity, RawColor specularIntensity) {
		this(CONSTANT_COLOR(ambientIntensity), CONSTANT_COLOR(diffuseIntensity), CONSTANT_COLOR(specularIntensity),
				DEFAULT_EXPOSURE_FUNCTION());
	}

	/**
	 * Create a new Light with given light intensities, and a custom
	 * lighting-exposure function.
	 * 
	 * @param ambientIntensity
	 * @param diffuseIntensity
	 * @param specularIntensity
	 * @param exposureFunction
	 */
	public Light(RawColor ambientIntensity, RawColor diffuseIntensity, RawColor specularIntensity,
			BiFunction<Light, Intersection<Shape>, Double> exposureFunction) {
		this(CONSTANT_COLOR(ambientIntensity), CONSTANT_COLOR(diffuseIntensity), CONSTANT_COLOR(specularIntensity),
				exposureFunction);
	}

	/**
	 * Create a new Light with the given intensity-functions for lighting, and
	 * the normal exposure-function.
	 * <p>
	 * Each intensity-function is of the form:
	 * 
	 * <pre>
	 * intensity = f(ray)
	 * </pre>
	 * 
	 * where {@code ray} = the incoming ray in <em>light-local</em> terms.
	 * </p>
	 * 
	 * @param ambientIntensityFunction
	 * @param diffuseIntensityFunction
	 * @param specularIntensityFunction
	 */
	public Light(Function<Ray, RawColor> ambientIntensityFunction, Function<Ray, RawColor> diffuseIntensityFunction,
			Function<Ray, RawColor> specularIntensityFunction) {
		this(ambientIntensityFunction, diffuseIntensityFunction, specularIntensityFunction,
				DEFAULT_EXPOSURE_FUNCTION());
	}

	/**
	 * Create a new Light with the given intensity-functions for lighting, and a
	 * custom exposure function.
	 * <p>
	 * The intensity functions are of the form:
	 * 
	 * <pre>
	 * intensity = f(ray)
	 * </pre>
	 * 
	 * @param ambientIntensityFunction
	 * @param diffuseIntensityFunction
	 * @param specularIntensityFunction
	 * @param exposureFunction
	 */
	public Light(Function<Ray, RawColor> ambientIntensityFunction, Function<Ray, RawColor> diffuseIntensityFunction,
			Function<Ray, RawColor> specularIntensityFunction,
			BiFunction<Light, Intersection<Shape>, Double> exposureFunction) {
		this.ambientIntensityFunction = ambientIntensityFunction;
		this.diffuseIntensityFunction = diffuseIntensityFunction;
		this.specularIntensityFunction = specularIntensityFunction;
		this.exposureFunction = exposureFunction;
	}

	/**
	 * Determine the intensity of ambient lighting provided by this Light to the
	 * given Ray (expressed in the <em>global</em> reference-frame).
	 * 
	 * @param ray
	 * @return the amount of ambient light provided by this Light to the given
	 *         Ray
	 */
	public RawColor getAmbientIntensity(Ray ray) {

		Ray localRay = worldToLocal(ray);
		return ambientIntensityFunction.apply(localRay);
	}

	/**
	 * Determine the intensity of diffuse lighting provided by this Light to the
	 * given Ray (expressed in the <em>global</em> reference-frame).
	 * 
	 * @param ray
	 * @return the amount of diffuse light provided by this Light to the given
	 *         Ray
	 */
	public RawColor getDiffuseIntensity(Ray ray) {

		Ray localRay = worldToLocal(ray);
		return diffuseIntensityFunction.apply(localRay);
	}

	/**
	 * Determine the intensity of specular lighting provided by this Light to
	 * the given Ray (expressed in the <em>global</em> reference-frame).
	 * 
	 * @param ray
	 * @return the amount of specular light provided by this Light to the given
	 *         Ray
	 */
	public RawColor getSpecularIntensity(Ray ray) {

		Ray localRay = worldToLocal(ray);
		return specularIntensityFunction.apply(localRay);
	}

	/**
	 * Determine the "exposure" this light is able to provide to the given
	 * {@link Intersection}. "Exposure" is usually a function of the light's
	 * position and the intersected surface's normal.
	 * 
	 * @see #DEFAULT_EXPOSURE_FUNCTION()
	 * 
	 * @param intersection
	 * @return the "exposure" this light is able to provide to the given
	 *         Intersection
	 */
	public double getExposure(Intersection<Shape> intersection) {
		
		Intersection<Shape> localIntersection = worldToLocal(intersection);
		return exposureFunction.apply(this, localIntersection);
	}

	@Override
	public Deque<Transformer> getTransformers() {

		return transformers;
	}

	@Override
	public Vector3D getLocation() {

		return localToWorld(Vector3D.ZERO);
	}

	/**
	 * @param color
	 * @return A function which returns the given color with no regard to its
	 *         input
	 */
	public static Function<Ray, RawColor> CONSTANT_COLOR(RawColor color) {

		return (ray) -> color;
	}

	/**
	 * An implementation of the ordinary exposure function:
	 * {@code exposure = cos(theta) } where {@code theta} is the angle between
	 * the light-direction and the surface-normal
	 * <p>
	 * 
	 * <pre>
	 * (l, i) -> l.getLocation()
	 *                   .subtract(i.getPoint())
	 *                   .normalize()
	 *            .dotProduct(i.getNormal()
	 *                         .normalize());
	 * </pre>
	 * </p>
	 * 
	 * @return the ordinary exposure function
	 */
	public static BiFunction<Light, Intersection<Shape>, Double> DEFAULT_EXPOSURE_FUNCTION() {

		return (l, i) -> l.getLocation().subtract(i.getPoint()).normalize().dotProduct(i.getNormal().normalize());
	}

}
