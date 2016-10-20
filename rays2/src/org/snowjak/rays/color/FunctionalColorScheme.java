package org.snowjak.rays.color;

import java.util.function.Function;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * An implementation of {@link ColorScheme} that acts merely as a container for
 * an underlying {@link Function}.
 * 
 * @author snowjak88
 *
 */
public class FunctionalColorScheme extends ColorScheme {

	private Function<Vector3D, RawColor> function;

	/**
	 * Construct a new {@link FunctionalColorScheme}, based on the given
	 * {@link Function}.
	 * 
	 * @param function
	 */
	public FunctionalColorScheme(Function<Vector3D, RawColor> function) {
		this.function = function;
	}

	@Override
	public ColorScheme copy() {

		return new FunctionalColorScheme(this.function);
	}

	@Override
	public RawColor getColor(double x, double y, double z) {

		return function.apply(worldToLocal(new Vector3D(x, y, z)));
	}

}
