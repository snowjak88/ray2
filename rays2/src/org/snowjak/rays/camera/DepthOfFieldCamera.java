package org.snowjak.rays.camera;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.Ray;
import org.snowjak.rays.RaytracerContext;
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

	private double focalLength;

	private double eyeDistance;

	private int sampleCount;

	private double lensRadius;

	private RealDistribution sampleWeighting;

	private Random rnd = new Random();

	private SuperSamplingAntialiaser<Vector3D, Optional<LightingResult>, Optional<LightingResult>> antialiaser = new SuperSamplingAntialiaser<>();

	/**
	 * Construct a new {@link DepthOfFieldCamera}, using a default lens-diameter
	 * equal to 1/10th of the camera's frame-width and selecting
	 * {@code sampleCount} samples from across the lens.
	 * 
	 * @param cameraFrameWidth
	 * @param fieldOfView
	 * @param focalLength
	 * @param sampleCount
	 */
	public DepthOfFieldCamera(double cameraFrameWidth, double fieldOfView, double focalLength, int sampleCount) {
		this(cameraFrameWidth, fieldOfView, focalLength, sampleCount, (1d / 10d) * cameraFrameWidth);
	}

	/**
	 * Construct a new {@link DepthOfFieldCamera}, using a lens of the given
	 * diameter and taking {@code sampleCount} randomly-selected samples across
	 * the lens.
	 * 
	 * @param cameraFrameWidth
	 * @param fieldOfView
	 * @param focalLength
	 * @param sampleCount
	 * @param lensDiameter
	 */
	public DepthOfFieldCamera(double cameraFrameWidth, double fieldOfView, double focalLength, int sampleCount,
			double lensDiameter) {
		super(cameraFrameWidth, fieldOfView);

		this.sampleCount = sampleCount;
		this.eyeDistance = getEyeLocation().distance(Vector3D.ZERO);
		this.focalLength = focalLength;
		this.lensRadius = lensDiameter / 2d;

		this.sampleWeighting = new UniformRealDistribution(0d, lensDiameter);
	}

	@Override
	public Optional<LightingResult> shootRay(double cameraX, double cameraY) {

		Vector3D eyeLocation = getEyeLocation();
		Vector3D caxelLocation = new Vector3D(cameraX, cameraY, 0d);
		Vector3D caxelDirection = caxelLocation.subtract(eyeLocation).normalize();
		double caxelDistance = caxelLocation.distance(eyeLocation);
		Vector3D focalPoint = eyeLocation
				.add(caxelDirection.scalarMultiply((caxelDistance / eyeDistance) * (eyeDistance + focalLength)));

		return antialiaser.execute(new Vector3D(cameraX, cameraY, 0d), (v) -> {
			Collection<Vector3D> results = new LinkedList<>();

			results.add(v);

			for (int i = 0; i < sampleCount - 1; i++) {
				double theta = rnd.nextDouble() * 2d * FastMath.PI;
				double r = rnd.nextDouble() * lensRadius;
				results.add(
						new Vector3D(v.getX() + FastMath.cos(theta) * r, v.getY() + FastMath.sin(theta) * r, v.getZ()));
			}

			return results;

		}, (v) -> {

			Ray ray = localToWorld(new Ray(v, focalPoint.subtract(v)));
			Optional<Intersection<Shape>> intersection = RaytracerContext.getSingleton()
					.getCurrentWorld()
					.getClosestShapeIntersection(ray);

			return RaytracerContext.getSingleton().getCurrentWorld().getLightingModel().determineRayColor(ray,
					intersection);

		}, (lp) -> {

			Pair<Double, RawColor> resultPair = lp.parallelStream()
					.map(p -> new Pair<>(p.getKey().distance(caxelLocation),
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
