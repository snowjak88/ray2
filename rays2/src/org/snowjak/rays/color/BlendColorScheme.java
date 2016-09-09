package org.snowjak.rays.color;

import java.util.function.Function;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;

import javafx.scene.paint.Color;

/**
 * A BlendColorScheme blends between two colors based on the provided function.
 * <p>
 * The {@code selectionFunction} is a function which maps object-local
 * coordinates to a Double fraction. If the returned double is outside the range
 * [0,1], it is clamped to that range.
 * </p>
 * <p>
 * This double is interpreted as an interpolation-fraction between the two
 * colors, from {@code color1} (0.0) to {@code color2} (1.0).
 * </p>
 * 
 * @author snowjak88
 *
 */
public class BlendColorScheme extends ColorScheme {

	private ColorScheme color1, color2;

	private Function<Vector3D, Double> selectionFunction;

	/**
	 * Construct a new {@link BlendColorScheme}, varying between color1 and
	 * color2 according to the selectionFunction.
	 * 
	 * @param color1
	 * @param color2
	 * @param selectionFunction
	 */
	public BlendColorScheme(Color color1, Color color2, Function<Vector3D, Double> selectionFunction) {
		this(new SimpleColorScheme(color1), new SimpleColorScheme(color2), selectionFunction);
	}

	/**
	 * Construct a new {@link BlendColorScheme}, varying between color1 and
	 * color2 according to the selectionFunction.
	 * 
	 * @param color1
	 * @param color2
	 * @param selectionFunction
	 */
	public BlendColorScheme(ColorScheme color1, ColorScheme color2, Function<Vector3D, Double> selectionFunction) {
		super();
		this.color1 = color1;
		this.color2 = color2;
		this.selectionFunction = selectionFunction;
	}

	@Override
	public ColorScheme copy() {

		return new BlendColorScheme(color1.copy(), color2.copy(), selectionFunction);
	}

	@Override
	public RawColor getColor(double x, double y, double z) {

		RawColor c1 = color1.getColor(x, y, z), c2 = color2.getColor(x, y, z);

		double fraction = FastMath.max(FastMath.min(selectionFunction.apply(new Vector3D(x, y, z)), 1d), 0d);
		return c1.multiplyScalar(1d - fraction).add(c2.multiplyScalar(fraction));
	}

}
