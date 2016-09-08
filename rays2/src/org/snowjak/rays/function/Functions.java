package org.snowjak.rays.function;

import static org.apache.commons.math3.util.FastMath.*;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;

public class Functions {

	private static int perlinGradiantSize = 20;

	private static Vector3D[][][] perlinGradiant = null;

	private static Random rnd = new Random(Functions.class.hashCode());

	public static void main(String[] args) {

		NumberFormat fmt = new DecimalFormat("#0.000");

		double minNoise = 1e30, maxNoise = -1e30;
		int lastPercentage = -1;

		for (double x = -20; x <= 20d; x += 0.3d) {
			for (double y = -20; y <= 20d; y += 0.3d) {
				for (double z = -20; z <= 20d; z += 0.3d) {
					double noise = getPerlinNoise(x, y, z);
					minNoise = FastMath.min(minNoise, noise);
					maxNoise = FastMath.max(maxNoise, noise);
				}
			}

			int percentage = (int) (FastMath.round(((x + 20d) / 40d) * 100d));
			if (percentage != lastPercentage) {
				lastPercentage = percentage;
				System.out.println(percentage + "% complete ... min = " + fmt.format(minNoise) + ", max = "
						+ fmt.format(maxNoise));
			}
		}

		System.out.println("Across that interval --");
		System.out.println("Min noise = " + fmt.format(minNoise));
		System.out.println("Max noise = " + fmt.format(maxNoise));

	}

	public static double getCheckerboard(Vector3D point) {

		return getCheckerboard(point.getX(), point.getY(), point.getZ());
	}

	public static double getCheckerboard(double x, double y, double z) {

		long xPart = (long) FastMath.round(x);
		long yPart = (long) FastMath.round(y);
		long zPart = (long) FastMath.round(z);

		if ((xPart + yPart + zPart) % 2 == 0)
			return 0d;
		else
			return 1d;
	}

	public static double getPerlinNoise(double x, double y, double z) {

		if (perlinGradiant == null)
			initializeGradiants();

		double noiseX = abs((x / (double) perlinGradiantSize)
				- floor(x / (double) perlinGradiantSize) * ((double) perlinGradiantSize));
		double noiseY = abs((y / (double) perlinGradiantSize)
				- floor(y / (double) perlinGradiantSize) * ((double) perlinGradiantSize));
		double noiseZ = abs((z / (double) perlinGradiantSize)
				- floor(z / (double) perlinGradiantSize) * ((double) perlinGradiantSize));

		double x0 = floor(noiseX), x1 = floor(noiseX) + 1d;
		double y0 = floor(noiseY), y1 = floor(noiseY) + 1d;
		double z0 = floor(noiseZ), z1 = floor(noiseZ) + 1d;
		double wx = noiseX - x0;
		double wy = noiseY - y0;
		double wz = noiseZ - z0;

		List<Pair<Vector3D, Double>> dotProducts = Arrays
				.asList(new Vector3D(x0, y0, z0), new Vector3D(x1, y0, z0), new Vector3D(x0, y1, z0),
						new Vector3D(x1, y1, z0), new Vector3D(x0, y0, z1), new Vector3D(x1, y0, z1),
						new Vector3D(x0, y1, z1), new Vector3D(x1, y1, z1))
				.stream()
				.map(v -> new Pair<>(v,
						perlinGradiant[(int) v.getX() % perlinGradiantSize][(int) v.getY()
								% perlinGradiantSize][(int) v.getZ() % perlinGradiantSize]
										.dotProduct(new Vector3D(noiseX, noiseY, noiseZ).subtract(v))))
				.collect(Collectors.toCollection(ArrayList::new));

		double[] xDotProducts, yDotProducts, zDotProducts;
		zDotProducts = new double[2];
		for (int iz = 0; iz <= 1; iz++) {
			yDotProducts = new double[2];
			for (int iy = 0; iy <= 1; iy++) {
				xDotProducts = new double[2];
				for (int ix = 0; ix <= 1; ix++) {
					xDotProducts[ix] = dotProducts.get(iz * 4 + iy * 2 + ix).getValue();
				}

				yDotProducts[iy] = linearInterpolate(xDotProducts[0], xDotProducts[1], wx);
			}

			zDotProducts[iz] = linearInterpolate(yDotProducts[0], yDotProducts[1], wy);
		}

		double noise = linearInterpolate(zDotProducts[0], zDotProducts[1], wz);
		return noise;
	}

	private static double linearInterpolate(double v1, double v2, double w) {

		return v1 * w + v2 * (1d - w);
	}

	public static double smoothstep(double x, double edge1, double edge2) {

		x = FastMath.min(FastMath.max((x - edge1) / (edge2 - edge1), 0d), 1d);
		return (3d * FastMath.pow(x, 2d)) - (2d * FastMath.pow(x, 3d));
	}

	public static double getPerlinNoise(Vector3D point) {

		return getPerlinNoise(point.getX(), point.getY(), point.getZ());
	}

	private static void randomSeed(long seed) {

		rnd = new Random(seed);
	}

	private static void initializeGradiants() {

		perlinGradiant = new Vector3D[perlinGradiantSize][perlinGradiantSize][perlinGradiantSize];

		for (int x = 0; x < perlinGradiant.length; x++)
			for (int y = 0; y < perlinGradiant[x].length; y++)
				for (int z = 0; z < perlinGradiant[x][y].length; z++) {
					perlinGradiant[x][y][z] = new Vector3D(rnd.nextGaussian(), rnd.nextGaussian(), rnd.nextGaussian())
							.normalize();
				}
	}
}
