package org.snowjak.rays.light;

import java.util.function.Function;

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
	 */
	public PointLight(RawColor ambientIntensity, RawColor diffuseIntensity, RawColor specularIntensity) {
		super(ambientIntensity, diffuseIntensity, specularIntensity);
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
	 */
	public PointLight(Function<Ray, RawColor> ambientIntensityFunction,
			Function<Ray, RawColor> diffuseIntensityFunction, Function<Ray, RawColor> specularIntensityFunction) {
		super(ambientIntensityFunction, diffuseIntensityFunction, specularIntensityFunction);
	}

}
