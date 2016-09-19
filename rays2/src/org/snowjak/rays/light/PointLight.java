package org.snowjak.rays.light;

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

}
