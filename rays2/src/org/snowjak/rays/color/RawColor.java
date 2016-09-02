package org.snowjak.rays.color;

import org.apache.commons.math3.util.FastMath;

import javafx.scene.paint.Color;

/**
 * Represents a RGB color that allows any values on its components. Contains a
 * convenience method ({@link #toColor()} to convert to a JavaFX {@link Color}
 * instance, clamping the RGB values to [0,1].
 * 
 * @author rr247200
 *
 */
public class RawColor {

	private double red, green, blue;

	/**
	 * Create a new RawColor, initialized to (R:0, G:0, B:0)
	 */
	public RawColor() {
		this(0d, 0d, 0d);
	}

	/**
	 * Create a new RawColor, initialized to match the provided {@link Color}
	 * 
	 * @param color
	 */
	public RawColor(Color color) {
		this.red = color.getRed();
		this.green = color.getGreen();
		this.blue = color.getBlue();
	}

	/**
	 * Create a new RawColor, with R,G,B initialized with the given values.
	 * 
	 * @param red
	 * @param green
	 * @param blue
	 */
	public RawColor(double red, double green, double blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	/**
	 * @param addend
	 * @return this + addend
	 */
	public RawColor add(RawColor addend) {

		return new RawColor(this.red + addend.red, this.green + addend.green, this.blue + addend.blue);
	}

	/**
	 * @param subtrahend
	 * @return this - subtrahend
	 */
	public RawColor subtract(RawColor subtrahend) {

		return new RawColor(this.red - subtrahend.red, this.green - subtrahend.green, this.blue - subtrahend.blue);
	}

	/**
	 * 
	 * @param other
	 * @return this.red * other.red, this.green * other.green, this.blue *
	 *         other.blue
	 */
	public RawColor multiply(RawColor other) {

		return new RawColor(this.red * other.red, this.green * other.green, this.blue * other.blue);
	}

	/**
	 * @param scalar
	 * @return this.red * scalar, this.green * scalar, this.blue * scalar
	 */
	public RawColor multiplyScalar(double scalar) {

		return new RawColor(this.red * scalar, this.green * scalar, this.blue * scalar);
	}

	/**
	 * @return a {@link Color} with this RawColor's values of R,G,B clamped to
	 *         [0,1]
	 */
	public Color toColor() {

		return new Color(clamp(red, 0d, 1d), clamp(green, 0d, 1d), clamp(blue, 0d, 1d), 1d);
	}

	private double clamp(double value, double min, double max) {

		return FastMath.min(FastMath.max(value, min), max);
	}

	/**
	 * @return this RawColor's R value
	 */
	public double getRed() {

		return red;
	}

	/**
	 * @return this RawColor's G value
	 */
	public double getGreen() {

		return green;
	}

	/**
	 * @return this RawColor's B value
	 */
	public double getBlue() {

		return blue;
	}

}
