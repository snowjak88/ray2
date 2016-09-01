package org.snowjak.rays.color;

import org.apache.commons.math3.util.FastMath;

import javafx.scene.paint.Color;

public class RawColor {

	private double red, green, blue;

	public RawColor() {
		this(0d, 0d, 0d);
	}

	public RawColor(Color color) {
		this.red = color.getRed();
		this.green = color.getGreen();
		this.blue = color.getBlue();
	}

	public RawColor(double red, double green, double blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public RawColor add(RawColor addend) {

		return new RawColor(this.red + addend.red, this.green + addend.green, this.blue + addend.blue);
	}

	public RawColor subtract(RawColor subtrahend) {

		return new RawColor(this.red - subtrahend.red, this.green - subtrahend.green, this.blue - subtrahend.blue);
	}

	public RawColor multiply(RawColor other) {

		return new RawColor(this.red * other.red, this.green * other.green, this.blue * other.blue);
	}

	public RawColor multiplyScalar(double scalar) {

		return new RawColor(this.red * scalar, this.green * scalar, this.blue * scalar);
	}

	public Color toColor() {

		return new Color(clamp(red, 0d, 1d), clamp(green, 0d, 1d), clamp(blue, 0d, 1d), 1d);
	}

	private double clamp(double value, double min, double max) {

		return FastMath.min(FastMath.max(value, min), max);
	}

	public double getRed() {

		return red;
	}

	public double getGreen() {

		return green;
	}

	public double getBlue() {

		return blue;
	}

}
