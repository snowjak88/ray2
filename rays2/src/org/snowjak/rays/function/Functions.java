package org.snowjak.rays.function;

import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.floor;
import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;
import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.round;

import java.util.Random;

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

	private static final int perlinGridSize = 20;

	private static Vector3D[][][] perlinGradiants = null;

	private static final double[][] availablePerlinGradiants = new double[][] { { -1, -1, -1 }, { -1, -1, 1 },
			{ -1, 1, -1 }, { -1, 1, 1 }, { 1, -1, -1 }, { 1, -1, 1 }, { 1, 1, -1 }, { 1, 1, 1 } };

	private static Random perlinRnd = new Random();

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

		if (perlinGradiants == null)
			initializePerlinGradiants();

		long maxExtent = perlinGridSize - 2l;

		while (x <= (double) -maxExtent)
			x += (double) perlinGridSize;
		while (x >= (double) maxExtent)
			x -= (double) perlinGridSize;

		while (y <= (double) -maxExtent)
			y += (double) perlinGridSize;
		while (y >= (double) maxExtent)
			y -= (double) perlinGridSize;

		while (z <= (double) -maxExtent)
			z += (double) perlinGridSize;
		while (z >= (double) maxExtent)
			z -= (double) perlinGridSize;

		x = abs(x);
		y = abs(y);
		z = abs(z);

		double fx = x - floor(x), fy = y - floor(y), fz = z - floor(z);

		int x0 = (int) x, x1 = (int) x + 1;
		int y0 = (int) y, y1 = (int) y + 1;
		int z0 = (int) z, z1 = (int) z + 1;

		double lerp_x_y0_z0 = linearInterpolate(getPerlinDotProduct(x0, y0, z0, x, y, z),
				getPerlinDotProduct(x1, y0, z0, x, y, z), fx);
		double lerp_x_y1_z0 = linearInterpolate(getPerlinDotProduct(x0, y1, z0, x, y, z),
				getPerlinDotProduct(x1, y1, z0, x, y, z), fx);
		double lerp_x_y0_z1 = linearInterpolate(getPerlinDotProduct(x0, y0, z1, x, y, z),
				getPerlinDotProduct(x1, y0, z1, x, y, z), fx);
		double lerp_x_y1_z1 = linearInterpolate(getPerlinDotProduct(x0, y1, z1, x, y, z),
				getPerlinDotProduct(x1, y1, z1, x, y, z), fx);

		double lerp_y_z0 = linearInterpolate(lerp_x_y0_z0, lerp_x_y1_z0, fy);
		double lerp_y_z1 = linearInterpolate(lerp_x_y0_z1, lerp_x_y1_z1, fy);

		double lerp = linearInterpolate(lerp_y_z0, lerp_y_z1, fz);

		return lerp;
	}

	private static double getPerlinDotProduct(int gx, int gy, int gz, double px, double py, double pz) {

		Vector3D displacement = new Vector3D(px - (double) gx, py - (double) gy, pz - (double) gz);
		return displacement.dotProduct(perlinGradiants[gx][gy][gz]);
	}

	private static void initializePerlinGradiants() {

		perlinGradiants = new Vector3D[perlinGridSize][perlinGridSize][perlinGridSize];
		for (int i = 0; i < perlinGradiants.length; i++)
			for (int j = 0; j < perlinGradiants[i].length; j++)
				for (int k = 0; k < perlinGradiants[i][j].length; k++)
					perlinGradiants[i][j][k] = new Vector3D(availablePerlinGradiants[perlinRnd.nextInt(8)]);

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
