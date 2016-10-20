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

import javafx.scene.paint.Color;

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
 * <strong>Default values</strong> are as follows:
 * <ul>
 * <li>Ambient radiance: 0</li>
 * <li>Diffuse radiance: 1 (i.e., white)</li>
 * <li>Specular radiance: 1 (i.e., white)</li>
 * <li>Intensity: 1 (constant)</li>
 * <li>Falloff: {@link #DEFAULT_FALLOFF_FUNCTION()}</li>
 * <li>Radius: none (i.e., this is a point light-source)</li>
 * </ul>
 * </p>
 * 
 * @author snowjak88
 *
 */
@Deprecated
public abstract class Light implements Transformable, Locatable {

	/**
	 * Defines Light's default ambient radiance (R/G/B = 0/0/0)
	 */
	public static final RawColor DEFAULT_AMBIENT = new RawColor(Color.BLACK);

	/**
	 * Defines Light's default diffuse radiance (R/G/B = 1/1/1)
	 */
	public static final RawColor DEFAULT_DIFFUSE = new RawColor(Color.WHITE);

	/**
	 * Defines Light's default specular radiance (R/G/B = 1/1/1)
	 */
	public static final RawColor DEFAULT_SPECULAR = new RawColor(Color.WHITE);

	/**
	 * Defines Light's default intensity-function (constant 1.0)
	 */
	public static final Function<Vector3D, Double> DEFAULT_INTENSITY = Functions.constant(1d);

	/**
	 * Define's Light's default falloff function.
	 * 
	 * @see #DEFAULT_FALLOFF_FUNCTION()
	 */
	public static final BiFunction<Light, Vector3D, Double> DEFAULT_FALLOFF = DEFAULT_FALLOFF_FUNCTION();

	/**
	 * Define's Light's default radius (i.e., none)
	 */
	public static final Optional<Double> DEFAULT_RADIUS = Optional.empty();

	private Deque<Transformer> transformers = new LinkedList<>();

	private RawColor ambientColor = DEFAULT_AMBIENT, diffuseColor = DEFAULT_DIFFUSE, specularColor = DEFAULT_SPECULAR;

	private Function<Vector3D, Double> intensityFunction = DEFAULT_INTENSITY;

	private final BiFunction<Light, Intersection<Shape>, Double> exposureFunction = DEFAULT_EXPOSURE_FUNCTION();

	private BiFunction<Light, Vector3D, Double> falloffFunction = DEFAULT_FALLOFF;

	private Optional<Double> radius = DEFAULT_RADIUS;

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

		return exposureFunction.apply(this, intersection);
	}

	/**
	 * @see #getExposure(Intersection)
	 * @param point
	 * @param normal
	 * @return the "exposure" this light is able to provide to the given point.
	 */
	public double getExposure(Vector3D point, Vector3D normal) {

		return getExposure(new Intersection<Shape>(point, normal, new Ray(point, getLocation().subtract(point)), null));
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
	 * Set this light's falloff function. See
	 * {@link #DEFAULT_FALLOFF_FUNCTION()}
	 * 
	 * @param falloffFunction
	 */
	public void setFalloffFunction(BiFunction<Light, Vector3D, Double> falloffFunction) {

		this.falloffFunction = falloffFunction;
	}

	/**
	 * Set this light's radius.
	 * 
	 * @param radius
	 */
	public void setRadius(Optional<Double> radius) {

		this.radius = (Double.compare(radius.orElse(0d), 0d) <= 0) ? Optional.empty() : radius;
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
	 * (l, i) -> (vector of l -> i.point) . i.normal
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
