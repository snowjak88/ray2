package org.snowjak.rays.color;

import java.util.Deque;
import java.util.LinkedList;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Prototype;
import org.snowjak.rays.transform.Transformable;
import org.snowjak.rays.transform.Transformer;

/**
 * Represents an algorithm for coloring an object.
 * 
 * @author rr247200
 *
 */
public abstract class ColorScheme implements Transformable, Prototype<ColorScheme> {

	private Deque<Transformer> transformers = new LinkedList<>();

	private double shininess = 1e5, reflectivity = -1d;

	/**
	 * Determine which color to use for the object, given the provided
	 * object-local coordinates.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return the color to use for this object
	 */
	public abstract RawColor getColor(double x, double y, double z);

	/**
	 * Determine which color to use for the object, given the provided
	 * object-local coordinates.
	 * 
	 * @param coord
	 * @return the color to use for this object
	 */
	public RawColor getColor(Vector3D coord) {

		return getColor(coord.getX(), coord.getY(), coord.getZ());
	}

	/**
	 * Determine which color to use for the object, given the provided global
	 * coordinates.
	 * 
	 * @param worldX
	 * @param worldY
	 * @param worldZ
	 * @return the color to use for this object
	 */
	public RawColor getColorForWorld(double worldX, double worldY, double worldZ) {

		return getColorForWorld(new Vector3D(worldX, worldY, worldZ));
	}

	/**
	 * Determine which color to use for the object, given the provided global
	 * coordinates.
	 * 
	 * @param worldCoord
	 * @return the color to use for this object
	 */
	public RawColor getColorForWorld(Vector3D worldCoord) {

		Vector3D localCoord = worldToLocal(worldCoord);
		return getColor(localCoord.getX(), localCoord.getY(), localCoord.getZ());
	}

	/**
	 * @return this color scheme's "shininess" exponent
	 */
	public double getShininess() {

		return shininess;
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return this color scheme's "shininess" exponent for the given point in
	 *         global coordinates
	 */
	public double getShininess(double x, double y, double z) {

		return getShininess(new Vector3D(x, y, z));
	}

	/**
	 * @param coord
	 * @return this color scheme's "shininess" exponent for the given point in
	 *         global coordinates
	 */
	public double getShininess(Vector3D coord) {

		return getShininess();
	}

	/**
	 * Set this color scheme's "shininess" exponent
	 * 
	 * @param shininess
	 */
	public void setShininess(double shininess) {

		this.shininess = shininess;
	}

	/**
	 * @return this color scheme's "reflectivity" fraction
	 */
	public double getReflectivity() {

		return reflectivity;
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return this color scheme's "reflectivity" fraction for the given point
	 *         in global coordinates
	 */
	public double getReflectivity(double x, double y, double z) {

		return getReflectivity(new Vector3D(x, y, z));
	}

	/**
	 * @param coord
	 * @return this color scheme's "reflectivity" fraction for the given point
	 *         in global coordinates
	 */
	public double getReflectivity(Vector3D coord) {

		return getReflectivity();
	}

	/**
	 * Set this color scheme's "reflectivity" fraction
	 * 
	 * @param reflectivity
	 */
	public void setReflectivity(double reflectivity) {

		this.reflectivity = reflectivity;
	}

	@Override
	public Deque<Transformer> getTransformers() {

		return transformers;
	}

}
