package org.snowjak.rays.function;

import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;
import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.round;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.snowjak.rays.color.BlendColorScheme;

/**
 * Specifies a number of functions, useful for creating complicated effects via
 * composition with, e.g., a {@link BlendColorScheme}
 * 
 * @author snowjak88
 *
 */
public class Functions {

	/**
	 * Compute the checkerboard function for the given point.
	 * <p>
	 * The checkerboard function implements the following algorithm:
	 * 
	 * <pre>
	 * result = sum ( round(abs(x)), ...y, ...z) MOD 2
	 * </pre>
	 * 
	 * {@code result} will always be in {0, 1}.
	 * </p>
	 * 
	 * @param v
	 * 
	 * @return the value of the checkerboard function
	 */
	public static double checkerboard(Vector2D v) {

		return checkerboard(v.getX(), v.getY());
	}

	/**
	 * Compute the checkerboard function for the given point.
	 * <p>
	 * The checkerboard function implements the following algorithm:
	 * 
	 * <pre>
	 * result = sum ( round(abs(x)), ...y, ...z) MOD 2
	 * </pre>
	 * 
	 * {@code result} will always be in {0, 1}.
	 * </p>
	 * 
	 * @param x
	 * @param y
	 * 
	 * @return the value of the checkerboard function
	 */
	public static double checkerboard(double x, double y) {

		return checkerboard(x, y, 0d);
	}

	/**
	 * Compute the checkerboard function for the given point.
	 * <p>
	 * The checkerboard function implements the following algorithm:
	 * 
	 * <pre>
	 * result = sum ( round(abs(x)), ...y, ...z) MOD 2
	 * </pre>
	 * 
	 * {@code result} will always be in {0, 1}.
	 * </p>
	 * 
	 * @param v
	 * 
	 * @return the value of the checkerboard function
	 */
	public static double checkerboard(Vector3D v) {

		return checkerboard(v.getX(), v.getY(), v.getZ());
	}

	/**
	 * Compute the checkerboard function for the given point.
	 * <p>
	 * The checkerboard function implements the following algorithm:
	 * 
	 * <pre>
	 * result = sum ( round(abs(x)), ...y, ...z) MOD 2
	 * </pre>
	 * 
	 * {@code result} will always be in {0, 1}.
	 * </p>
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * 
	 * @return the value of the checkerboard function
	 */
	public static double checkerboard(double x, double y, double z) {

		long lx = (long) round(abs(x));
		long ly = (long) round(abs(y));
		long lz = (long) round(abs(z));
		return (double) ((lx + ly + lz) % 2);
	}

	/**
	 * Computes Perlin noise at the specified point.
	 * 
	 * @param v
	 * @return the computed Perlin noise
	 */
	public static double perlinNoise(Vector2D v) {

		return perlinNoise(v.getX(), v.getY());
	}

	/**
	 * Computes Perlin noise at the specified point.
	 * 
	 * @param x
	 * @param y
	 * @return the computed Perlin noise
	 */
	public static double perlinNoise(double x, double y) {

		return perlinNoise(x, y, 0d);
	}

	/**
	 * Computes Perlin noise at the specified point.
	 * 
	 * @param v
	 * @return the computed Perlin noise
	 */
	public static double perlinNoise(Vector3D v) {

		return perlinNoise(v.getX(), v.getY(), v.getZ());
	}

	/**
	 * Computes Perlin noise at the specified point.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return the computed Perlin noise
	 */
	public static double perlinNoise(double x, double y, double z) {

		return PerlinNoise.getSingleton().perlinNoise(x, y, z);
	}

	/**
	 * Computers a linear-interpolation between two values.
	 * 
	 * @param v1
	 * @param v2
	 * @param w
	 * @return the new linear-interpolation function
	 */
	public static double linearInterpolate(double v1, double v2, double w) {

		return v1 * (1d - w) + v2 * w;
	}

	/**
	 * Evaluates the smoothstep function.
	 * 
	 * @param x
	 * @param edge1
	 * @param edge2
	 * @return the computed smoothstep value
	 */
	public static double smoothstep(double x, double edge1, double edge2) {

		x = min(max((x - edge1) / (edge2 - edge1), 0d), 1d);
		return (3d * pow(x, 2d)) - (2d * pow(x, 3d));
	}

}
