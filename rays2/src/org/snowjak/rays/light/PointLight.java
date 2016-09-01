package org.snowjak.rays.light;

import java.util.function.Function;

import org.snowjak.rays.Ray;
import org.snowjak.rays.color.RawColor;

public class PointLight extends Light {

	public PointLight(RawColor ambientIntensity, RawColor diffuseIntensity, RawColor specularIntensity) {
		super(ambientIntensity, diffuseIntensity, specularIntensity);
	}

	public PointLight(Function<Ray, RawColor> ambientIntensityFunction,
			Function<Ray, RawColor> diffuseIntensityFunction, Function<Ray, RawColor> specularIntensityFunction) {
		super(ambientIntensityFunction, diffuseIntensityFunction, specularIntensityFunction);
	}

}
