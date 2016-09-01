package org.snowjak.rays.color;

import javafx.scene.paint.Color;

/**
 * Implements a basic {@link ColorScheme}, in which every point is the same
 * color. In effect, paints an entire object the same color.
 * 
 * @author rr247200
 *
 */
public class SimpleColorScheme extends ColorScheme {

	private RawColor color;

	/**
	 * Create a new SimpleColorScheme with the specified color
	 * 
	 * @param red
	 *            fraction of red, in [0..1]
	 * @param green
	 *            fraction of green
	 * @param blue
	 *            fraction of blue
	 */
	public SimpleColorScheme(double red, double green, double blue) {

		this(new RawColor(red, green, blue));
	}

	public SimpleColorScheme(Color color) {
		this(new RawColor(color));
	}

	public SimpleColorScheme() {
		this(Color.TRANSPARENT);
	}

	/**
	 * Create a new SimpleColorScheme with the specified color
	 * 
	 * @param color
	 */
	public SimpleColorScheme(RawColor color) {
		this.color = color;
	}

	@Override
	public RawColor getColor(double x, double y, double z) {

		return color;
	}

}
