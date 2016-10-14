package org.snowjak.rays.light;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.World;
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

	/**
	 * The direction which this DirectionalLight will point at by default.
	 */
	public static final Vector3D DEFAULT_DIRECTION = Vector3D.MINUS_J;

	private Vector3D direction = DEFAULT_DIRECTION;

	/**
	 * Create a new DirectionalLight with the default pointing-to direction
	 * (i.e., {@link #DEFAULT_DIRECTION}).
	 * 
	 * @param direction
	 */
	public DirectionalLight() {
		super();
		this.setFalloffFunction((l, v) -> 1d);
	}

	/**
	 * @return the direction that this {@link DirectionalLight} is pointing
	 *         <em>to</em>.
	 */
	public Vector3D getDirection() {

		return direction;
	}

	/**
	 * Set the direction that this {@link DirectionalLight} should point
	 * <em>to</em>
	 * 
	 * @param direction
	 */
	public void setDirection(Vector3D direction) {

		this.direction = direction.normalize();
	}

	@Override
	public Vector3D getLocation() {

		return direction.negate().normalize().scalarMultiply(World.WORLD_BOUND);
	}

}
