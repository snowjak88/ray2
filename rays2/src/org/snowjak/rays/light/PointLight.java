package org.snowjak.rays.light;

import java.util.function.Function;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.color.RawColor;

/**
 * <p>
 * PointLight represents light that comes from a particular, nearby location in
 * space.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class PointLight extends Light {

	/**
	 * <p>
	 * PointLight represents light that comes from a particular, nearby location
	 * in space.
	 * </p>
	 * See {@link Light#Light(RawColor, RawColor, RawColor)}
	 * 
	 * @param ambientIntensity
	 * @param diffuseIntensity
	 * @param specularIntensity
	 * @param intensity
	 */
	public PointLight(RawColor ambientIntensity, RawColor diffuseIntensity, RawColor specularIntensity,
			double intensity) {
		super(ambientIntensity, diffuseIntensity, specularIntensity, intensity);
	}

	/**
	 * <p>
	 * PointLight represents light that comes from a particular, nearby location
	 * in space.
	 * </p>
	 * See {@link Light#Light(Function, Function, Function)}
	 * 
	 * @param ambientIntensityFunction
	 * @param diffuseIntensityFunction
	 * @param specularIntensityFunction
	 * @param intensityFunction
	 */
	public PointLight(Function<Ray, RawColor> ambientIntensityFunction,
			Function<Ray, RawColor> diffuseIntensityFunction, Function<Ray, RawColor> specularIntensityFunction,
			Function<Vector3D, Double> intensityFunction) {
		super(ambientIntensityFunction, diffuseIntensityFunction, specularIntensityFunction, intensityFunction);
	}

}
