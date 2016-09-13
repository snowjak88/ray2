package org.snowjak.rays.function;

import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;
import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.round;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.color.RawColor;

import javafx.scene.paint.Color;

/**
 * Specifies a number of functions, useful for creating complicated effects via
 * composition with, e.g., a {@link BlendColorScheme}
 * 
 * @author snowjak88
 *
 */
public class Functions {

	/**
	 * Create a new function that simply returns a constant RawColor value.
	 * 
	 * @param color
	 * @return a new constant-RawColor function
	 */
	public static Function<Vector3D, RawColor> constant(RawColor color) {

		return (v) -> color;
	}

	/**
	 * Create a new function that simply returns a constant RawColor value.
	 * 
	 * @param color
	 * @return a new constant-RawColor function
	 */
	public static Function<Vector3D, RawColor> constant(Color color) {

		return constant(new RawColor(color));
	}

	/**
	 * Create a new function that simply returns a constant double value.
	 * 
	 * @param value
	 * @return a new constant-double function
	 */
	public static Function<Vector3D, Double> constant(Double value) {

		return (v) -> value;
	}

	/**
	 * Given a list of colors and their respective thresholds, return a color
	 * which is either:
	 * <ul>
	 * <li>linearly-interpolated between two list-items, if the given value is
	 * equal or between the list of doubles</li>
	 * <li>the lowest or highest colors, if the given value is outside the list
	 * of doubles</li>
	 * </ul>
	 * <p>
	 * For example, given the color-list:
	 * 
	 * <pre>
	 * { { 0.0, WHITE }, { 0.5, RED }, { 1.0, BLUE } }
	 * </pre>
	 * 
	 * and the input {@code 0.4}, the function would return the value RGB {0.2,
	 * 0.2, 1.0) -- i.e., 0.4/0.5 or 80% of the way from WHITE to RED
	 * </p>
	 * 
	 * @param colors
	 * @return a function which selects a color from a list based on a double
	 *         lookup value
	 */
	@SafeVarargs
	public static Function<Double, RawColor> blend(Pair<Double, Color>... colors) {

		return blend(Arrays.asList(colors));
	}

	/**
	 * Given a list of colors and their respective thresholds, return a color
	 * which is either:
	 * <ul>
	 * <li>linearly-interpolated between two list-items, if the given value is
	 * equal or between the list of doubles</li>
	 * <li>the lowest or highest colors, if the given value is outside the list
	 * of doubles</li>
	 * </ul>
	 * <p>
	 * For example, given the color-list:
	 * 
	 * <pre>
	 * { { 0.0, WHITE }, { 0.5, RED }, { 1.0, BLUE } }
	 * </pre>
	 * 
	 * and the input {@code 0.4}, the function would return the value RGB {0.2,
	 * 0.2, 1.0) -- i.e., 0.4/0.5 or 80% of the way from WHITE to RED
	 * </p>
	 * 
	 * @param colors
	 * @return a function which selects a color from a list based on a double
	 *         lookup value
	 */
	public static Function<Double, RawColor> blend(List<Pair<Double, Color>> colors) {

		return (d) -> {
			Pair<Double, Color> lower = null, higher = null;
			for (Pair<Double, Color> pair : colors.stream()
					.sorted((p1, p2) -> Double.compare(p1.getKey(), p2.getKey()))
					.collect(Collectors.toCollection(LinkedList::new))) {
				lower = higher;
				higher = pair;

				if (lower != null) {
					if (higher.getKey() > d && lower.getKey() <= d)
						return lerp(new RawColor(lower.getValue()), new RawColor(higher.getValue()),
								(d - lower.getKey()) / (higher.getKey() - lower.getKey()));
					else if (lower.getKey() > d)
						return new RawColor(lower.getValue());
				}

			}

			return new RawColor(higher.getValue());
		};
	}

	/**
	 * Create a new function that linearly-interpolates between two colors based
	 * on the point's distance from two control points.
	 * 
	 * @param color1
	 * @param p1
	 * @param color2
	 * @param p2
	 * @return a new linear-interpolation function
	 */
	public static Function<Vector3D, RawColor> lerp(RawColor color1, Vector3D p1, RawColor color2, Vector3D p2) {

		return (v) -> {
			double dotProduct = p2.subtract(p1).normalize().dotProduct(v.subtract(p1)), distance = p2.distance(p1);
			return Functions.lerp(color1, color2, dotProduct / distance);
		};
	}

	/**
	 * Create a new function that linearly-interpolates between two
	 * double-values based on the point's distance from two control points.
	 * 
	 * @param value1
	 * @param p1
	 * @param value2
	 * @param p2
	 * @return a new linear-interpolation function
	 */
	public static Function<Vector3D, Double> lerp(Double value1, Vector3D p1, Double value2, Vector3D p2) {

		return (v) -> {
			double dotProduct = p2.subtract(p1).normalize().dotProduct(v.subtract(p1)), distance = p2.distance(p1);
			return Functions.lerp(value1, value2, dotProduct / distance);
		};
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
	 * Calculate a turbulent-noise function using a number of aggregated
	 * Perlin-noise values.
	 * <p>
	 * The final turbulence value is calculated as:
	 * 
	 * <pre>
	 *                          Perlin( b^i * X )
	 * noise = sum (i = 1->N) ---------------------
	 *                                a^i
	 * </pre>
	 * 
	 * where {@code X} = the 3D point in question, {@code a} = "smoothness
	 * parameter" (by default, 2), {@code b} = "scaling factor" (some power of
	 * 2), and {@code N} = the number of octaves to use
	 * </p>
	 * 
	 * @param point
	 * @param octaves
	 * @return the calculated turbulent-noise value
	 */
	public static double turbulence(Vector2D point, int octaves) {

		return turbulence(point.getX(), point.getY(), octaves);
	}

	/**
	 * Calculate a turbulent-noise function using a number of aggregated
	 * Perlin-noise values.
	 * <p>
	 * The final turbulence value is calculated as:
	 * 
	 * <pre>
	 *                          Perlin( b^i * X )
	 * noise = sum (i = 1->N) ---------------------
	 *                                a^i
	 * </pre>
	 * 
	 * where {@code X} = the 3D point in question, {@code a} = "smoothness
	 * parameter" (by default, 2), {@code b} = "scaling factor" (some power of
	 * 2), and {@code N} = the number of octaves to use
	 * </p>
	 * 
	 * @param x
	 * @param y
	 * @param octaves
	 * @return the calculated turbulent-noise value
	 */
	public static double turbulence(double x, double y, int octaves) {

		return turbulence(x, y, 0d, octaves);
	}

	/**
	 * Calculate a turbulent-noise function using a number of aggregated
	 * Perlin-noise values.
	 * <p>
	 * The final turbulence value is calculated as:
	 * 
	 * <pre>
	 *                          Perlin( b^i * X )
	 * noise = sum (i = 1->N) ---------------------
	 *                                a^i
	 * </pre>
	 * 
	 * where {@code X} = the 3D point in question, {@code a} = "smoothness
	 * parameter" (by default, 2), {@code b} = "scaling factor" (some power of
	 * 2), and {@code N} = the number of octaves to use
	 * </p>
	 * 
	 * @param point
	 * @param octaves
	 * @return the calculated turbulent-noise value
	 */
	public static double turbulence(Vector3D point, int octaves) {

		return turbulence(point.getX(), point.getY(), point.getZ(), octaves);
	}

	/**
	 * Calculate a turbulent-noise function using a number of aggregated
	 * Perlin-noise values.
	 * <p>
	 * The final turbulence value is calculated as:
	 * 
	 * <pre>
	 *                          Perlin( b^i * X )
	 * noise = sum (i = 1->N) ---------------------
	 *                                a^i
	 * </pre>
	 * 
	 * where {@code X} = the 3D point in question, {@code a} = "smoothness
	 * parameter" (by default, 2), {@code b} = "scaling factor" (some power of
	 * 2), and {@code N} = the number of octaves to use
	 * </p>
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param octaves
	 * @return the calculated turbulent-noise value
	 */
	public static double turbulence(double x, double y, double z, int octaves) {

		if (octaves < 0)
			throw new IllegalArgumentException("octaves cannot be < 0 -- argument given = " + octaves);

		double finalNoise = 0d;
		for (int i = 1; i <= octaves; i++) {
			finalNoise += perlinNoise(x * pow(2d, (double) i), y * pow(2d, (double) i), z * pow(2d, (double) i))
					/ pow(2d, (double) i);
		}

		return finalNoise;
	}

	/**
	 * Computers a linear-interpolation between two values.
	 * 
	 * @param v1
	 * @param v2
	 * @param w
	 * @return the new linearly-interpolated value
	 */
	public static double lerp(double v1, double v2, double w) {

		return v1 * (1d - w) + v2 * w;
	}

	/**
	 * Computers a linear-interpolation between two colors.
	 * 
	 * @param v1
	 * @param v2
	 * @param w
	 * @return the new linearly-interpolated color
	 */
	public static RawColor lerp(RawColor v1, RawColor v2, double w) {

		w = min(max(w, 0d), 1d);
		return v1.multiplyScalar(1d - w).add(v2.multiplyScalar(w));
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
