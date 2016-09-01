package org.snowjak.rays.light;

import java.util.function.Function;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.color.RawColor;

public final class DirectionalLight extends Light {

	private Vector3D direction;

	public DirectionalLight(Vector3D direction, Function<Ray, RawColor> ambientIntensityFunction,
			Function<Ray, RawColor> diffuseIntensityFunction, Function<Ray, RawColor> specularIntensityFunction) {
		super(ambientIntensityFunction, diffuseIntensityFunction, specularIntensityFunction);
		this.direction = direction;
	}

	public DirectionalLight(Vector3D direction, RawColor ambientIntensity, RawColor diffuseIntensity,
			RawColor specularIntensity) {
		this(direction, CONSTANT_COLOR(ambientIntensity), CONSTANT_COLOR(diffuseIntensity),
				CONSTANT_COLOR(specularIntensity));
	}

	@Override
	public Vector3D getLocation() {

		return direction.negate().normalize().scalarMultiply(World.WORLD_BOUND);
	}

}
