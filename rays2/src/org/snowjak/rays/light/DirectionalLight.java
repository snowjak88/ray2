package org.snowjak.rays.light;

import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.World;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.function.Functions;
import org.snowjak.rays.transform.Transformer;

/**
 * Implements a simple directional light -- a {@link Light} which shines its
 * light-rays into the world at the same angle. A directional light is a simple
 * method of simulating a light-source that is extremely far away -- so far that
 * the received light-rays are practically parallel.
 * <p>
 * One consequence of this is that a DirectionalLight's position is immaterial:
 * it gives the same light to the entire world regardless of any
 * {@link Transformer}s applied to it.
 * </p>
 * 
 * @author snowjak88
 *
 */
public final class DirectionalLight extends Light {

	private Vector3D direction;

	/**
	 * Create a new {@link DirectionalLight}, with its light-rays pointing in
	 * the given direction (expressed as a vector in global coordinates).
	 * 
	 * @param direction
	 * @param ambientColor
	 * @param diffuseColor
	 * @param specularColor
	 * @param intensityFunction
	 */
	public DirectionalLight(Vector3D direction, RawColor ambientColor, RawColor diffuseColor, RawColor specularColor) {
		this(direction, ambientColor, diffuseColor, specularColor, 1d);
	}

	/**
	 * Create a new {@link DirectionalLight}, with its light-rays pointing in
	 * the given direction (expressed as a vector in global coordinates).
	 * 
	 * @param direction
	 * @param ambientColor
	 * @param diffuseColor
	 * @param specularColor
	 * @param intensity
	 */
	public DirectionalLight(Vector3D direction, RawColor ambientColor, RawColor diffuseColor, RawColor specularColor,
			double intensity) {
		super(ambientColor, diffuseColor, specularColor, Light.DEFAULT_EXPOSURE_FUNCTION(),
				Functions.constant(intensity), (l, v) -> 1d, Optional.empty());
		this.direction = direction.normalize();
	}

	@Override
	public Vector3D getLocation() {

		return direction.negate().normalize().scalarMultiply(World.WORLD_BOUND);
	}

}
