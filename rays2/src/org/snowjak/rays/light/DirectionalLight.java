package org.snowjak.rays.light;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.color.RawColor;

import javafx.scene.paint.Color;

/**
 * Implements a simple directional light -- a {@link Light} which shines its
 * light-rays into the world at the same angle. A directional light is a simple
 * method of simulating a light-source that is extremely far away -- so far that
 * the received light-rays are practically parallel.
 * 
 * @author snowjak88
 *
 */
public final class DirectionalLight {

	/**
	 * The direction which this DirectionalLight will point at by default.
	 */
	public static final Vector3D DEFAULT_DIRECTION = Vector3D.MINUS_J;

	private Vector3D direction = DEFAULT_DIRECTION;

	/**
	 * The default radiance that this DirectionalLight affords.
	 */
	public static final RawColor DEFAULT_RADIANCE = new RawColor(Color.WHITE);

	private RawColor radiance = DEFAULT_RADIANCE;

	/**
	 * Create a new DirectionalLight with the default pointing-to direction
	 * (i.e., {@link #DEFAULT_DIRECTION}).
	 * 
	 * @param direction
	 */
	public DirectionalLight() {
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

	/**
	 * @return the radiance that this {@link DirectionalLight} affords
	 */
	public RawColor getRadiance() {

		return radiance;
	}

	/**
	 * Set the radiance that this {@link DirectionalLight} should afford.
	 * 
	 * @param radiance
	 */
	public void setRadiance(RawColor radiance) {

		this.radiance = radiance;
	}
}
