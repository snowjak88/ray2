package org.snowjak.rays.camera;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.antialias.SuperSamplingAntialiaser;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.light.model.LightingModel.LightingResult;
import org.snowjak.rays.shape.Shape;

/**
 * Version of {@link Camera} that models depth-of-field.
 * 
 * @author snowjak88
 *
 */
public class DepthOfFieldCamera extends Camera {

	private double eyeRadius;

	private int sampleCount;

	private RealDistribution sampleWeighting;

	private Random rnd = new Random();

	private SuperSamplingAntialiaser<Vector3D, Optional<LightingResult>, Optional<LightingResult>> antialiaser = new SuperSamplingAntialiaser<>();

	/**
	 * Construct a new {@link DepthOfFieldCamera}, focusing upon objects
	 * {@code focusDistance} away, and using {@code sampleCount}
	 * randomly-selected samples.
	 * <p>
	 * The naive {@link Camera} models a pin-hole lens: each {@link Ray} is shot
	 * from the single eye-location, through a particular point on the Camera's
	 * focal-plane, and tested for intersections.
	 * </p>
	 * <p>
	 * This DepthOfFieldCamera models an eye with a certain size. The diameter
	 * of the eye-disk is calculated to be
	 * 
	 * <pre>
	 *   focal-distance + focal-length
	 * ---------------------------------
	 *           focal-distance
	 * </pre>
	 * 
	 * This camera then randomly selects {@code sampleCount} points from within
	 * that disk, samples them from the world, and antialiases the resulting
	 * {@link RawColor}s together.
	 * </p>
	 * 
	 * @param cameraFrameWidth
	 * @param fieldOfView
	 * @param focusDistance
	 * @param sampleCount
	 */
	public DepthOfFieldCamera(double cameraFrameWidth, double fieldOfView, double focusDistance, int sampleCount) {
		super(cameraFrameWidth, fieldOfView);

		this.sampleCount = sampleCount;
		double focalLength = getEyeLocation().distance(Vector3D.ZERO);
//		this.eyeRadius = ((focusDistance + focalLength) / focusDistance) / 2d;
		this.eyeRadius = ((focalLength) / focusDistance) / 2d;

		this.sampleWeighting = new NormalDistribution(0d, eyeRadius / 2d);
	}

	@Override
	public Optional<LightingResult> shootRay(double cameraX, double cameraY) {

		return antialiaser.execute(getEyeLocation(), (v) -> {
			Collection<Vector3D> results = new LinkedList<>();

			results.add(v);

			for (int i = 0; i < sampleCount - 1; i++) {
				double theta = rnd.nextDouble() * 2d * FastMath.PI;
				double r = rnd.nextDouble() * eyeRadius;
				results.add(
						new Vector3D(v.getX() + FastMath.cos(theta) * r, v.getY() + FastMath.sin(theta) * r, v.getZ()));
			}

			return results;

		}, (v) -> {
			Vector3D caxelLocation = new Vector3D(cameraX, cameraY, 0d);
			Ray ray = localToWorld(new Ray(caxelLocation, caxelLocation.subtract(v)));
			Optional<Intersection<Shape>> intersection = World.getSingleton().getClosestShapeIntersection(ray);

			return World.getSingleton().getLightingModel().determineRayColor(ray, intersection);

		}, (lp) -> {

			Pair<Double, RawColor> resultPair = lp.parallelStream()
					.map(p -> new Pair<>(p.getKey().distance(getEyeLocation()),
							p.getValue().map(lr -> lr.getRadiance()).orElse(new RawColor())))
					.map(p -> new Pair<>(sampleWeighting.density(p.getKey()), p.getValue()))
					.map(p -> new Pair<>(p.getKey(), p.getValue().multiplyScalar(p.getKey())))
					.reduce(new Pair<>(0d, new RawColor()),
							(p1, p2) -> new Pair<>(p1.getKey() + p2.getKey(), p1.getValue().add(p2.getValue())));

			RawColor resultingColor = resultPair.getValue().multiplyScalar(1d / resultPair.getKey());

			LightingResult result = new LightingResult();
			result.setRadiance(resultingColor);
			return Optional.of(result);
		});
	}

}
