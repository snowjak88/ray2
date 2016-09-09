package org.snowjak.rays.function;

import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.floor;

import java.util.Random;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Encapsulates a Perlin noise-generator.
 * 
 * @author snowjak88
 *
 */
public class PerlinNoise {

	private int perlinGridSize = 20;

	private Vector3D[][][] perlinGradiants = null;

	private final double[][] availablePerlinGradiants = new double[][] { { -1, -1, -1 }, { -1, -1, 1 }, { -1, 1, -1 },
			{ -1, 1, 1 }, { 1, -1, -1 }, { 1, -1, 1 }, { 1, 1, -1 }, { 1, 1, 1 } };

	private Random perlinRnd = new Random();

	private static PerlinNoise INSTANCE = null;

	/**
	 * @return the PerlinNoise singleton instance
	 */
	public static PerlinNoise getSingleton() {

		if (INSTANCE == null)
			INSTANCE = new PerlinNoise();

		return INSTANCE;
	}

	protected PerlinNoise() {
		initializePerlinGradiants();
	}

	/**
	 * Computes Perlin noise at the specified point.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return the computed Perlin noise
	 */
	public double perlinNoise(double x, double y, double z) {

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

		double lerp_x_y0_z0 = Functions.linearInterpolate(getPerlinDotProduct(x0, y0, z0, x, y, z),
				getPerlinDotProduct(x1, y0, z0, x, y, z), fx);
		double lerp_x_y1_z0 = Functions.linearInterpolate(getPerlinDotProduct(x0, y1, z0, x, y, z),
				getPerlinDotProduct(x1, y1, z0, x, y, z), fx);
		double lerp_x_y0_z1 = Functions.linearInterpolate(getPerlinDotProduct(x0, y0, z1, x, y, z),
				getPerlinDotProduct(x1, y0, z1, x, y, z), fx);
		double lerp_x_y1_z1 = Functions.linearInterpolate(getPerlinDotProduct(x0, y1, z1, x, y, z),
				getPerlinDotProduct(x1, y1, z1, x, y, z), fx);

		double lerp_y_z0 = Functions.linearInterpolate(lerp_x_y0_z0, lerp_x_y1_z0, fy);
		double lerp_y_z1 = Functions.linearInterpolate(lerp_x_y0_z1, lerp_x_y1_z1, fy);

		double lerp = Functions.linearInterpolate(lerp_y_z0, lerp_y_z1, fz);

		return lerp;
	}

	private double getPerlinDotProduct(int gx, int gy, int gz, double px, double py, double pz) {

		if (gx < 0 || gx >= perlinGradiants.length)
			throw new ArrayIndexOutOfBoundsException("gx is outside of gradiant-array bounds!");
		if (gy < 0 || gy >= perlinGradiants[gx].length)
			throw new ArrayIndexOutOfBoundsException("gy is outside of gradiant-array bounds!");
		if (gz < 0 || gz >= perlinGradiants[gx][gy].length)
			throw new ArrayIndexOutOfBoundsException("gz is outside of gradiant-array bounds!");

		if (perlinGradiants[gx][gy][gz] == null)
			throw new RuntimeException(
					String.format("Perlin gradiant at [%d][%d][%d] is not initialized!", gx, gy, gz));

		Vector3D displacement = new Vector3D(px - (double) gx, py - (double) gy, pz - (double) gz);
		return displacement.dotProduct(perlinGradiants[gx][gy][gz]);
	}

	private void initializePerlinGradiants() {

		synchronized (this) {
			if (perlinGradiants != null)
				return;

			perlinGradiants = new Vector3D[perlinGridSize][perlinGridSize][perlinGridSize];
			for (int i = 0; i < perlinGradiants.length; i++)
				for (int j = 0; j < perlinGradiants[i].length; j++)
					for (int k = 0; k < perlinGradiants[i][j].length; k++)
						perlinGradiants[i][j][k] = new Vector3D(
								availablePerlinGradiants[perlinRnd.nextInt(availablePerlinGradiants.length)]);

		}
	}
}
