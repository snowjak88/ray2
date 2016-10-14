package org.snowjak.rays.color;

import org.snowjak.rays.builder.Builder;
import org.snowjak.rays.world.HasName;

/**
 * Allows automatic instantiation of {@link RawColor} instances.
 * 
 * @author snowjak88
 *
 */
@HasName("color")
public class RawColorBuilder implements Builder<RawColor> {

	private double red = 0d, green = 0d, blue = 0d;

	/**
	 * @return a new RawColorBuilder instance
	 */
	public static RawColorBuilder builder() {

		return new RawColorBuilder();
	}

	protected RawColorBuilder() {

	}

	/**
	 * Configure this RawColor's red value.
	 * 
	 * @param red
	 * @return this Builder, for method-chaining
	 */
	@HasName("r")
	public RawColorBuilder red(double red) {

		this.red = red;
		return this;
	}

	/**
	 * Configure this RawColor's green value.
	 * 
	 * @param green
	 * @return this Builder, for method-chaining
	 */
	@HasName("g")
	public RawColorBuilder green(double green) {

		this.green = green;
		return this;
	}

	/**
	 * Configure this RawColor's blue value.
	 * 
	 * @param blue
	 * @return this Builder, for method-chaining
	 */
	@HasName("b")
	public RawColorBuilder blue(double blue) {

		this.blue = blue;
		return this;
	}

	@Override
	public RawColor build() {

		return new RawColor(red, green, blue);
	}

}
