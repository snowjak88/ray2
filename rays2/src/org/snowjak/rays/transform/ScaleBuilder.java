package org.snowjak.rays.transform;

import org.snowjak.rays.builder.Builder;
import org.snowjak.rays.world.importfile.HasName;

/**
 * A convenient interface for instantiating {@link Scale}s.
 * 
 * @author snowjak88
 *
 */
@HasName("scale")
public class ScaleBuilder implements Builder<Scale> {

	private double scaleX = 1d, scaleY = 1d, scaleZ = 1d;

	/**
	 * @return a new ScaleBuilder instance
	 */
	public static ScaleBuilder builder() {

		return new ScaleBuilder();
	}

	protected ScaleBuilder() {

	}

	/**
	 * Configure this {@link Scale} to use the given X scale-factor.
	 * 
	 * @param scaleX
	 * @return this Builder, for method-chaining
	 */
	@HasName("x")
	public ScaleBuilder x(double scaleX) {

		this.scaleX = scaleX;
		return this;
	}

	/**
	 * Configure this {@link Scale} to use the given Y scale-factor.
	 * 
	 * @param scaleY
	 * @return this Builder, for method-chaining
	 */
	@HasName("y")
	public ScaleBuilder y(double scaleY) {

		this.scaleY = scaleY;
		return this;
	}

	/**
	 * Configure this {@link Scale} to use the given Z scale-factor.
	 * 
	 * @param scaleZ
	 * @return this Builder, for method-chaining
	 */
	@HasName("z")
	public ScaleBuilder z(double scaleZ) {

		this.scaleZ = scaleZ;
		return this;
	}

	@Override
	public Scale build() {

		return new Scale(scaleX, scaleY, scaleZ);
	}

}
