package org.snowjak.rays.builder;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.world.importfile.HasName;

/**
 * Allows automated instantiation of {@link Vector3D} instances.
 * 
 * @author snowjak88
 *
 */
@HasName("vector")
public class VectorBuilder implements Builder<Vector3D> {

	private double x = 0d, y = 0d, z = 0d;

	/**
	 * @return a new VectorBuilder instance
	 */
	public static VectorBuilder builder() {

		return new VectorBuilder();
	}

	protected VectorBuilder() {

	}

	/**
	 * Configure this Vector3D's x displacement
	 * 
	 * @param x
	 * @return this VectorBuilder, for method-chaining
	 */
	@HasName("x")
	public VectorBuilder x(double x) {

		this.x = x;
		return this;
	}

	/**
	 * Configure this Vector3D's y displacement
	 * 
	 * @param y
	 * @return this VectorBuilder, for method-chaining
	 */
	@HasName("y")
	public VectorBuilder y(double y) {

		this.y = y;
		return this;
	}

	/**
	 * Configure this Vector3D's z displacement
	 * 
	 * @param z
	 * @return this VectorBuilder, for method-chaining
	 */
	@HasName("z")
	public VectorBuilder z(double z) {

		this.z = z;
		return this;
	}

	@Override
	public Vector3D build() {

		return new Vector3D(x, y, z);
	}

}
