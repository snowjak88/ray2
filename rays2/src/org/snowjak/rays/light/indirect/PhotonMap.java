package org.snowjak.rays.light.indirect;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.light.model.LightingModel.LightingResult;
import org.snowjak.rays.shape.Shape;

/**
 * A photon-map is a relatively-simple method for obtaining indirect
 * illumination.
 * <p>
 * Before rendering commences, a number of photons are shot in random directions
 * from each {@link Light} in the {@link World}. Where a photon intersects a
 * surface, that photon has the chance to (1) reflect, (2) refract, or (3) be
 * absorbed. Once a photon is absorbed, its location is stored within the
 * "photon-map".
 * </p>
 * <p>
 * After the photon-map is built, it can be referenced during rendering.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class PhotonMap {

	private static PhotonMap INSTANCE = null;

	private Random rnd = new Random();

	private List<Pair<Vector3D, RawColor>> photonLocations = new LinkedList<>();

	private List<Shape> aimShapes = new LinkedList<>();

	private boolean currentlyPopulating = false;

	/**
	 * @return the singleton PhotonMap instance
	 */
	public static PhotonMap getSingleton() {

		if (INSTANCE == null)
			INSTANCE = new PhotonMap();
		return INSTANCE;
	}

	protected PhotonMap() {
	}

	/**
	 * Erases the PhotonMap.
	 */
	public void clear() {

		this.photonLocations = new LinkedList<>();
	}

	/**
	 * Iterate over every {@link Light} in the {@link World} and shoot
	 * {@code photonCount} photons from each light, storing their eventual
	 * locations in the map.
	 * <p>
	 * <strong>Note</strong> that this does not clear the map. If you wish to
	 * re-build it from scratch, you must first clear it using {@link #clear()}.
	 * </p>
	 * 
	 * @param photonCount
	 */
	public void add(int photonCount) {

		currentlyPopulating = true;

		int lightCount = 1;
		for (Light light : World.getSingleton().getLights()) {

			System.out.println("Building Photon-Map: shooting " + photonCount + " photons for light #" + lightCount
					+ "/" + World.getSingleton().getLights().size());
			addForLight(light, photonCount);
		}

		currentlyPopulating = false;

		System.out.println("Building Photon-Map: Complete!");
	}

	/**
	 * Shoot {@code photonCount} photons from the given {@link Light}, storing
	 * their eventual locations in the map.
	 * <p>
	 * <strong>Note</strong> that this does not clear the map. If you wish to
	 * re-build it from scratch, you must first remove all entries from it.
	 * </p>
	 * 
	 * @param light
	 * @param photonCount
	 */
	public void addForLight(Light light, int photonCount) {

		BlockingQueue<Pair<Vector3D, RawColor>> buildingList = new LinkedBlockingQueue<>();

		AtomicInteger photonsCompleted = new AtomicInteger(0);
		boolean lastCurrentlyPopulating = currentlyPopulating;
		currentlyPopulating = true;

		ScheduledExecutorService photonMapProgressExecutor = Executors.newSingleThreadScheduledExecutor();

		photonMapProgressExecutor.scheduleAtFixedRate(() -> {
			int percentage = (int) (((double) photonsCompleted.get() / (double) photonCount) * 100d);
			System.out.println(percentage + "% complete (" + photonsCompleted.get() + " photons ...)");

		}, 1, 3, TimeUnit.SECONDS);

		for (int i = 0; i < photonCount; i++) {

			World.getSingleton().getWorkerThreadPool().submit(() -> {
				Ray photonPath;
				Optional<LightingResult> photonLightingResult;
				do {
					do {
						photonPath = new Ray(light.getLocation(), getRandomVector(light.getLocation()), 1);
					} while (!isRayAcceptable(photonPath));

					photonLightingResult = World.getSingleton().getLightingModel().determineRayColor(photonPath,
							World.getSingleton().getShapeIntersections(photonPath));

				} while (!photonLightingResult.isPresent());

				followPhoton(buildingList, light.getDiffuseColor(), photonPath, photonLightingResult.get(), light,
						photonCount);
				photonsCompleted.incrementAndGet();
			});
		}

		while (World.getSingleton().getWorkerThreadPool().getActiveCount() > 0) {
		}
		photonMapProgressExecutor.shutdownNow();

		photonLocations.addAll(buildingList);
		photonLocations.sort((l1, l2) -> Double.compare(l1.getKey().getNorm(), l2.getKey().getNorm()));
		currentlyPopulating = lastCurrentlyPopulating;
	}

	private boolean isRayAcceptable(Ray photonRay) {

		if (aimShapes.isEmpty())
			aimShapes.addAll(World.getSingleton().getShapes());

		return aimShapes.parallelStream().anyMatch(s -> s.getIntersection(photonRay).isPresent());
	}

	private void followPhoton(BlockingQueue<Pair<Vector3D, RawColor>> buildingList, RawColor currentPhotonColor,
			Ray ray, LightingResult photonLightingResult, Light light, int photonCount) {

		if (ray.getOrigin().getNorm() >= World.WORLD_BOUND)
			return;

		if (photonLightingResult.getContributingResults().isEmpty()) {
			buildingList.add(new Pair<>(photonLightingResult.getPoint(),
					currentPhotonColor.multiplyScalar(light.getIntensity(photonLightingResult.getPoint()))));
			return;
		}

		List<Pair<LightingResult, Double>> contributingResults = new LinkedList<>();
		contributingResults.addAll(photonLightingResult.getContributingResults());
		EnumeratedDistribution<LightingResult> resultDistribution = new EnumeratedDistribution<>(contributingResults);

		LightingResult followingResult = resultDistribution.sample();
		Ray followingEye = followingResult.getEye();
		Ray followingPhotonPath = new Ray(followingEye.getOrigin(), followingEye.getVector());

		RawColor newPhotonColor = currentPhotonColor;
		newPhotonColor = currentPhotonColor.multiply(photonLightingResult.getTint());

		followPhoton(buildingList, newPhotonColor, followingPhotonPath, followingResult, light, photonCount);
	}

	/**
	 * @return a vector of unit-length, pointing in some random direction within
	 *         the unit-sphere
	 */
	private Vector3D getRandomVector(Vector3D origin) {

		if (aimShapes.isEmpty())
			return Vector3D.PLUS_J;

		Vector3D result;
		do {
			Shape rndAimShape = aimShapes.get(rnd.nextInt(aimShapes.size()));
			Vector3D toAimShape = rndAimShape.getLocation().subtract(origin);
			Vector3D rndPerturb = new Vector3D(rnd.nextGaussian(), rnd.nextGaussian(), rnd.nextGaussian());
			result = toAimShape.normalize().add(rnd.nextDouble(), rndPerturb);
		} while (Double.compare(result.getNorm(), 0d) == 0);

		return result.normalize();
	}

	/**
	 * @return the stored list of photons
	 */
	public List<Pair<Vector3D, RawColor>> getPhotons() {

		return photonLocations;
	}

	/**
	 * @return the set of {@link Shape}s which all the generated photons will be
	 *         aimed at initially
	 */
	public List<Shape> getAimShapes() {

		return aimShapes;
	}

	/**
	 * @return <code>true</code> if this photon-map has been populated.
	 */
	public boolean isPhotonMapPopulated() {

		return !photonLocations.isEmpty() && !currentlyPopulating;
	}

	/**
	 * @param point
	 * @return a photon-location that is at most {@link World#DOUBLE_ERROR}
	 *         distant from the given point
	 */
	public Optional<Pair<Vector3D, RawColor>> getPhotonCloseToPoint(Vector3D point) {

		return photonLocations.parallelStream()
				.filter(l -> Double.compare(l.getKey().distance(point), World.DOUBLE_ERROR) <= 0)
				.findFirst();
	}

	/**
	 * @param point
	 * @param distance
	 * @return all photons at are at most {@code distance} away from the given
	 *         point
	 */
	public List<Pair<Vector3D, RawColor>> getPhotonsCloseToPoint(Vector3D point, double distance) {

		return photonLocations.parallelStream()
				.filter(l -> Double.compare(l.getKey().distance(point), distance) <= 0)
				.collect(Collectors.toCollection(LinkedList::new));
	}

	/**
	 * Calculate the total illumination afforded by the photon map to the given
	 * point.
	 * 
	 * @param point
	 * @return the calculated total illumination
	 */
	public RawColor getIlluminationAtPoint(Vector3D point) {

		return getIlluminationAtPoint(point, World.DOUBLE_ERROR);
	}

	/**
	 * Calculate the total illumination afforded by the photon map to the given
	 * point. Include all photons within the given {@code distance} of the
	 * point.
	 * 
	 * @param point
	 * @param distance
	 * @return the calculated total illumination
	 */
	public RawColor getIlluminationAtPoint(Vector3D point, double distance) {

		if (currentlyPopulating)
			return new RawColor();

		Collection<Pair<Vector3D, RawColor>> closePhotons = getPhotonsCloseToPoint(point, distance);
		if (closePhotons.isEmpty())
			return new RawColor();

		return closePhotons.parallelStream()
				.map(p -> p.getValue().multiplyScalar(1d / (4d * FastMath.PI * distance)))
				.reduce(new RawColor(), (c1, c2) -> c1.add(c2))
				.multiplyScalar(1d / (double) photonLocations.size());

	}
}
