package org.snowjak.rays.transform;

import org.snowjak.rays.builder.Builder;
import org.snowjak.rays.world.importfile.HasName;

/**
 * A convenient interface for instantiating {@link Rotation}s.
 * 
 * @author snowjak88
 *
 */
@HasName("rotate")
public class RotationBuilder implements Builder<Rotation> {

	private double pitch = 0d, roll = 0d, yaw = 0d;

	/**
	 * @return a new RotationBuilder instance
	 */
	public RotationBuilder builder() {

		return new RotationBuilder();
	}

	protected RotationBuilder() {

	}

	/**
	 * Configure this Rotation to use the specified pitch angle.
	 * 
	 * @param pitch
	 * @return this Builder, for method-chaining
	 */
	@HasName("pitch")
	public RotationBuilder pitch(double pitch) {

		this.pitch = pitch;
		return this;
	}

	/**
	 * Configure this Rotation to use the specified roll angle.
	 * 
	 * @param roll
	 * @return this Builder, for method-chaining
	 */
	@HasName("roll")
	public RotationBuilder roll(double roll) {

		this.roll = roll;
		return this;
	}

	/**
	 * Configure this Rotation to use the specified yaw angle.
	 * 
	 * @param yaw
	 * @return this Builder, for method-chaining
	 */
	@HasName("yaw")
	public RotationBuilder yaw(double yaw) {

		this.yaw = yaw;
		return this;
	}

	@Override
	public Rotation build() {

		return new Rotation(pitch, yaw, roll);
	}

}
