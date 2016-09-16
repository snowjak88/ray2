package org.snowjak.rays.light.indirect;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.intersect.Intersection;
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

	private List<Vector3D> photonLocations = new LinkedList<>();

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
	 * Iterate over every {@link Light} in the {@link World} and shoot
	 * {@code photonCount} photons from each light, storing their eventual
	 * locations in the map.
	 * <p>
	 * <strong>Note</strong> that this does not clear the map. If you wish to
	 * re-build it from scratch, you must first remove all entries from it.
	 * </p>
	 * 
	 * @param photonCount
	 */
	public void add(int photonCount) {

		for (Light light : World.getSingleton().getLights())
			addForLight(light, photonCount);
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

		for (int i = 0; i < photonCount; i++) {
			Ray photonPath = new Ray(light.getLocation(), getRandomVector());

			followPhoton(photonPath);
		}

		photonLocations.sort((v1, v2) -> Double.compare(v1.getNorm(), v2.getNorm()));
	}

	private void followPhoton(Ray ray) {

		List<Intersection<Shape>> photonIntersections = World.getSingleton().getShapeIntersections(ray);
		if (photonIntersections.isEmpty())
			return;

		Optional<LightingResult> photonLightingResult = World.getSingleton().getLightingModel().determineRayColor(ray,
				photonIntersections);

		if (!photonLightingResult.isPresent())
			return;

		if (photonLightingResult.get().getContributingResults().isEmpty()
				&& Double.compare(photonLightingResult.get().getPoint().getNorm(), World.WORLD_BOUND) < 0)
			photonLocations.add(photonLightingResult.get().getPoint());

		List<Pair<LightingResult, Double>> contributingResults = new LinkedList<>();
		contributingResults.addAll(photonLightingResult.get().getContributingResults());
		EnumeratedDistribution<LightingResult> resultDistribution = new EnumeratedDistribution<>(contributingResults);

		Vector3D newEyeVector = resultDistribution.sample().getEye();
		Ray newPhotonPath = new Ray(photonLightingResult.get().getPoint(), newEyeVector);
		followPhoton(newPhotonPath);
	}

	/**
	 * @return a vector of unit-length, pointing in some random direction within
	 *         the unit-sphere
	 */
	private Vector3D getRandomVector() {

		Vector3D result = new Vector3D(rnd.nextGaussian(), rnd.nextGaussian(), rnd.nextGaussian());
		while (Double.compare(result.getNorm(), 0d) != 0)
			result = new Vector3D(rnd.nextGaussian(), rnd.nextGaussian(), rnd.nextGaussian());

		return result.normalize();
	}

	/**
	 * @return the stored list of photon locations
	 */
	public List<Vector3D> getPhotonLocations() {

		return photonLocations;
	}

	/**
	 * @return <code>true</code> if this photon-map has been populated.
	 */
	public boolean isPhotonMapPopulated() {

		return !photonLocations.isEmpty();
	}

	/**
	 * @param point
	 * @return a photon-location that is at most {@link World#DOUBLE_ERROR}
	 *         distant from the given point
	 */
	public Optional<Vector3D> getPhotonCloseToPoint(Vector3D point) {

		return photonLocations.parallelStream()
				.filter(l -> Double.compare(l.distance(point), World.DOUBLE_ERROR) <= 0)
				.findFirst();
	}
}
