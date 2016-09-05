package org.snowjak.rays.color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;

import javafx.scene.paint.Color;

/**
 * Implements a "checkerboard" pattern, alternating between two different child
 * ColorSchemes.
 * <p>
 * This ColorScheme effectively divides up the local coordinate-space into
 * alternating cubes of a given side-length (by default, 1.0).
 * </p>
 * 
 * @author rr247200
 *
 */
public class CheckerboardColorScheme extends ColorScheme {

	private double cubeSize;

	private ColorScheme color1, color2;

	/**
	 * Create a new CheckerboardColorScheme, of cubes of side-length 1.0
	 * alternating between the two provided colors.
	 * 
	 * @param color1
	 * @param color2
	 */
	public CheckerboardColorScheme(Color color1, Color color2) {
		this(new SimpleColorScheme(new RawColor(color1)), new SimpleColorScheme(new RawColor(color2)));
	}

	/**
	 * Create a new CheckerboardColorScheme, of cubes of side-length 1.0
	 * alternating between the two provided colors.
	 * 
	 * @param color1
	 * @param color2
	 */
	public CheckerboardColorScheme(ColorScheme color1, ColorScheme color2) {
		this(1d, color1, color2);
	}

	/**
	 * Create a new CheckerboardColorScheme, of cubes of side-length
	 * {@code squareSize} alternating between the two provided colors.
	 * 
	 * @param cubeSize
	 * @param color1
	 * @param color2
	 */
	public CheckerboardColorScheme(double cubeSize, Color color1, Color color2) {
		this(cubeSize, new SimpleColorScheme(new RawColor(color1)), new SimpleColorScheme(new RawColor(color2)));
	}

	/**
	 * Create a new CheckerboardColorScheme, of cubes of side-length
	 * {@code squareSize} alternating between the two provided colors.
	 * 
	 * @param cubeSize
	 * @param color1
	 * @param color2
	 */
	public CheckerboardColorScheme(double cubeSize, ColorScheme color1, ColorScheme color2) {
		this.cubeSize = cubeSize;
		this.color1 = color1;
		this.color2 = color2;
	}

	private ColorScheme getColorSchemeByCoordinate(double x, double y, double z) {

		long xPart = (long) FastMath.round(x / cubeSize);
		long yPart = (long) FastMath.round(y / cubeSize);
		long zPart = (long) FastMath.round(z / cubeSize);

		if ((xPart + yPart + zPart) % 2 == 0)
			return color1;
		else
			return color2;
	}

	@Override
	public RawColor getColor(double x, double y, double z) {

		return getColorSchemeByCoordinate(x, y, z).getColor(x, y, z);
	}

	@Override
	public double getShininess(double x, double y, double z) {

		return getShininess(new Vector3D(x, y, z));
	}

	@Override
	public double getShininess(Vector3D coord) {

		Vector3D localCoord = worldToLocal(coord);
		return getColorSchemeByCoordinate(localCoord.getX(), localCoord.getY(), localCoord.getZ())
				.getShininess(localCoord);
	}

	@Override
	public double getReflectivity(double x, double y, double z) {

		return getReflectivity(new Vector3D(x, y, z));
	}

	@Override
	public double getReflectivity(Vector3D coord) {

		Vector3D localCoord = worldToLocal(coord);

		return getColorSchemeByCoordinate(localCoord.getX(), localCoord.getY(), localCoord.getZ())
				.getReflectivity(localCoord);
	}

	/**
	 * @return the configured cube side-length
	 */
	public double getCubeSize() {

		return cubeSize;
	}

	/**
	 * @return the 1st of the 2 alternating colors
	 */
	public ColorScheme getColor1() {

		return color1;
	}

	/**
	 * @return the 2nd of the 2 alternating colors
	 */
	public ColorScheme getColor2() {

		return color2;
	}

}
