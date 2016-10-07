package org.snowjak.rays.light;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Locatable;
import org.snowjak.rays.Ray;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.function.Functions;
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
 * @author snowjak88
 *
 */
public class Light implements Transformable, Locatable {

	private Deque<Transformer> transformers = new LinkedList<>();

	private RawColor ambientColor, diffuseColor, specularColor;

	private Function<Vector3D, Double> intensityFunction;

	private BiFunction<Light, Intersection<Shape>, Double> exposureFunction;

	private BiFunction<Light, Vector3D, Double> falloffFunction;

	private Optional<Double> radius;

	/**
	 * Create a new Light with the given lighting intensity, and the normal
	 * exposure-function.
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
	 * @param ambientColor
	 * @param diffuseColor
	 * @param specularColor
	 * @param intensity
	 * @param intensityFunction
	 */
	public Light(RawColor ambientColor, RawColor diffuseColor, RawColor specularColor, double intensity) {
		this(ambientColor, diffuseColor, specularColor, DEFAULT_EXPOSURE_FUNCTION(), Functions.constant(intensity),
				DEFAULT_FALLOFF_FUNCTION(), Optional.empty());
	}

	/**
	 * Create a new Light with the given lighting intensity, the normal
	 * exposure-function, and the given radius.
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
	 * @param ambientColor
	 * @param diffuseColor
	 * @param specularColor
	 * @param intensity
	 * @param radius
	 */
	public Light(RawColor ambientColor, RawColor diffuseColor, RawColor specularColor, double intensity,
			double radius) {
		this(ambientColor, diffuseColor, specularColor, DEFAULT_EXPOSURE_FUNCTION(), Functions.constant(intensity),
				DEFAULT_FALLOFF_FUNCTION(), Optional.of(radius));
	}

	/**
	 * Create a new Light with the given lighting-intensity function, and the
	 * normal exposure-function.
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
	 * @param ambientColor
	 * @param diffuseColor
	 * @param specularColor
	 * @param intensityFunction
	 */
	public Light(RawColor ambientColor, RawColor diffuseColor, RawColor specularColor,
			Function<Vector3D, Double> intensityFunction) {
		this(ambientColor, diffuseColor, specularColor, DEFAULT_EXPOSURE_FUNCTION(), intensityFunction,
				DEFAULT_FALLOFF_FUNCTION(), Optional.empty());
	}

	/**
	 * Create a new Light with the given lighting intensity-function, and a
	 * custom exposure function.
	 * 
	 * @param ambientColor
	 * @param diffuseColor
	 * @param specularColor
	 * @param exposureFunction
	 * @param intensityFunction
	 * @param falloffFunction
	 */
	public Light(RawColor ambientColor, RawColor diffuseColor, RawColor specularColor,
			BiFunction<Light, Intersection<Shape>, Double> exposureFunction,
			Function<Vector3D, Double> intensityFunction, BiFunction<Light, Vector3D, Double> falloffFunction) {
		this(ambientColor, diffuseColor, specularColor, exposureFunction, intensityFunction, falloffFunction,
				Optional.empty());
	}

	/**
	 * Create a new Light with the given lighting intensity-function, a custom
	 * exposure function, and the given radius.
	 * 
	 * @param ambientColor
	 * @param diffuseColor
	 * @param specularColor
	 * @param exposureFunction
	 * @param intensityFunction
	 * @param falloffFunction
	 * @param radius
	 */
	public Light(RawColor ambientColor, RawColor diffuseColor, RawColor specularColor,
			BiFunction<Light, Intersection<Shape>, Double> exposureFunction,
			Function<Vector3D, Double> intensityFunction, BiFunction<Light, Vector3D, Double> falloffFunction,
			Optional<Double> radius) {
		this.ambientColor = ambientColor;
		this.diffuseColor = diffuseColor;
		this.specularColor = specularColor;
		this.exposureFunction = exposureFunction;
		this.intensityFunction = intensityFunction;
		this.falloffFunction = falloffFunction;
		this.radius = (Double.compare(radius.orElse(0d), 0d) == 0 ? Optional.empty() : radius);
	}

	/**
	 * Determine the intensity of ambient lighting provided by this Light to the
	 * given Ray (expressed in the <em>global</em> reference-frame).
	 * 
	 * @return the amount of ambient light provided by this Light to the given
	 *         Ray
	 */
	public RawColor getAmbientColor() {

		return ambientColor;
	}

	/**
	 * Determine the intensity of diffuse lighting provided by this Light to the
	 * given Ray (expressed in the <em>global</em> reference-frame).
	 * 
	 * @return the amount of diffuse light provided by this Light to the given
	 *         Ray
	 */
	public RawColor getDiffuseColor() {

		return diffuseColor;
	}

	/**
	 * Determine the intensity of specular lighting provided by this Light to
	 * the given Ray (expressed in the <em>global</em> reference-frame).
	 * 
	 * @return the amount of specular light provided by this Light to the given
	 *         Ray
	 */
	public RawColor getSpecularColor() {

		return specularColor;
	}

	/**
	 * Determine this light's overall intensity-multiplier available to the
	 * given point.
	 * 
	 * @param v
	 * @return this light's overall intensity
	 */
	public double getIntensity(Vector3D v) {

		return intensityFunction.apply(v);
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

	/**
	 * @see #getExposure(Intersection)
	 * @param point
	 * @param normal
	 * @return the "exposure" this light is able to provide to the given point.
	 */
	public double getExposure(Vector3D point, Vector3D normal) {

		return getExposure(new Intersection<Shape>(point, normal, new Ray(point, normal), null));
	}

	/**
	 * Calculate this light's falloff-fraction as seen by the given point.
	 * 
	 * @param point
	 * @return this light's falloff
	 */
	public double getFalloff(Vector3D point) {

		return falloffFunction.apply(this, point);
	}

	/**
	 * @return this light's radius (if it has one)
	 */
	public Optional<Double> getRadius() {

		return radius;
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
	 * Set this Light's ambient-lighting function color
	 * 
	 * @param ambientColor
	 */
	public void setAmbientColor(RawColor ambientColor) {

		this.ambientColor = ambientColor;
	}

	/**
	 * Set this Light's diffuse-lighting color
	 * 
	 * @param diffuseColor
	 */
	public void setDiffuseColor(RawColor diffuseColor) {

		this.diffuseColor = diffuseColor;
	}

	/**
	 * Set this Light's specular-lighting color
	 * 
	 * @param specularColor
	 */
	public void setSpecularColor(RawColor specularColor) {

		this.specularColor = specularColor;
	}

	/**
	 * Set this light's overall-intensity function
	 * 
	 * @param intensityFunction
	 */
	public void setIntensityFunction(Function<Vector3D, Double> intensityFunction) {

		this.intensityFunction = intensityFunction;
	}

	/**
	 * Set this light's exposure function. See
	 * {@link #DEFAULT_EXPOSURE_FUNCTION()}
	 * 
	 * @param exposureFunction
	 */
	public void setExposureFunction(BiFunction<Light, Intersection<Shape>, Double> exposureFunction) {

		this.exposureFunction = exposureFunction;
	}

	/**
	 * Set this light's falloff function. See
	 * {@link #DEFAULT_FALLOFF_FUNCTION()}
	 * 
	 * @param falloffFunction
	 */
	public void setFalloffFunction(BiFunction<Light, Vector3D, Double> falloffFunction) {

		this.falloffFunction = falloffFunction;
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
	 * (l, i) -> (vector of l -> i.point) . i.normal * intensity * falloff
	 * </pre>
	 * </p>
	 * 
	 * @return the ordinary exposure function
	 */
	public static BiFunction<Light, Intersection<Shape>, Double> DEFAULT_EXPOSURE_FUNCTION() {

		return (l, i) -> l.getLocation().subtract(i.getPoint()).normalize().dotProduct(i.getNormal());
	}

	/**
	 * Create a {@link BiFunction} which implements the area-rule falloff
	 * function:
	 * 
	 * <pre>
	 *                1
	 * falloff = ------------
	 *            4 * pi * d
	 * </pre>
	 * 
	 * where {@code d} = the distance between the light's location and
	 * {@code point}
	 * 
	 * @return the area-rule falloff function
	 */
	public static BiFunction<Light, Vector3D, Double> DEFAULT_FALLOFF_FUNCTION() {

		return (l, v) -> 1d / (4d * FastMath.PI * l.getLocation().distance(v));
	}

}
