package org.snowjak.rays.light.indirect;

import java.util.Arrays;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.Ray;
import org.snowjak.rays.RaytracerContext;
import org.snowjak.rays.World;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.light.indirect.PhotonMap.Kd3dTree.Dimension;
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
@Deprecated
public class PhotonMap {

	private static PhotonMap INSTANCE = null;

	private Random rnd = new Random();

	private List<PhotonMap.Entry> photonLocations = new LinkedList<>();

	private Kd3dTree photonMap;

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
		for (Light light : RaytracerContext.getSingleton().getCurrentWorld().getLights()) {

			System.out.println("Building Photon-Map: shooting " + photonCount + " photons for light #" + lightCount
					+ "/" + RaytracerContext.getSingleton().getCurrentWorld().getLights().size());
			addForLight(light, photonCount);
		}

		System.out.println("Building KD-tree ...");
		photonMap = createKdTreeFromList(photonLocations);
		photonLocations = null;

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
	protected void addForLight(Light light, int photonCount) {

		BlockingQueue<PhotonMap.Entry> buildingList = new LinkedBlockingQueue<>();

		AtomicInteger photonsCompleted = new AtomicInteger(0);
		boolean lastCurrentlyPopulating = currentlyPopulating;
		currentlyPopulating = true;

		ScheduledExecutorService photonMapProgressExecutor = Executors.newSingleThreadScheduledExecutor();

		photonMapProgressExecutor.scheduleAtFixedRate(() -> {
			int percentage = (int) (((double) photonsCompleted.get() / (double) photonCount) * 100d);
			System.out.println(percentage + "% complete (" + photonsCompleted.get() + " photons ...)");

		}, 1, 3, TimeUnit.SECONDS);

		for (int i = 0; i < photonCount; i++) {

			RaytracerContext.getSingleton().getWorkerThreadPool().submit(() -> {
				Ray photonPath;
				Optional<LightingResult> photonLightingResult;
				do {
					do {
						photonPath = new Ray(light.getLocation(), getRandomVector(light.getLocation()), 1);
					} while (!isRayAcceptable(photonPath));

					photonLightingResult = RaytracerContext.getSingleton()
							.getCurrentRenderer()
							.getLightingModel()
							.determineRayColor(photonPath, RaytracerContext.getSingleton()
									.getCurrentWorld()
									.getClosestShapeIntersection(photonPath));

				} while (!photonLightingResult.isPresent());

				followPhoton(buildingList, light.getDiffuseColor().multiplyScalar(1d / (double) photonCount),
						photonPath, photonLightingResult.get(), light, photonCount);
				photonsCompleted.incrementAndGet();
			});
		}

		while (RaytracerContext.getSingleton().getWorkerThreadPool().getActiveCount() > 0) {
		}
		photonMapProgressExecutor.shutdownNow();

		photonLocations.addAll(buildingList);
		photonLocations.sort((l1, l2) -> Double.compare(l1.getPoint().getNorm(), l2.getPoint().getNorm()));
		currentlyPopulating = lastCurrentlyPopulating;
	}

	private boolean isRayAcceptable(Ray photonRay) {

		if (aimShapes.isEmpty())
			aimShapes.addAll(RaytracerContext.getSingleton().getCurrentWorld().getShapes());

		return aimShapes.parallelStream().anyMatch(s -> s.getIntersection(photonRay).isPresent());
	}

	private void followPhoton(BlockingQueue<PhotonMap.Entry> buildingList, RawColor currentPhotonColor, Ray ray,
			LightingResult photonLightingResult, Light light, int photonCount) {

		if (ray.getOrigin().getNorm() >= World.FAR_AWAY)
			return;

		if (photonLightingResult.getContributingResults().isEmpty()) {
			buildingList.add(
					new Entry(currentPhotonColor.multiplyScalar(light.getIntensity(photonLightingResult.getPoint())),
							photonLightingResult.getPoint(), photonLightingResult.getEye().getVector()));
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
	public List<PhotonMap.Entry> getPhotons() {

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

		// return !photonLocations.isEmpty() && !currentlyPopulating;
		return photonMap != null && !currentlyPopulating;
	}

	/**
	 * @param point
	 * @return a photon-location that is at most {@link World#NEARLY_ZERO}
	 *         distant from the given point
	 */
	public Optional<PhotonMap.Entry> getPhotonCloseToPoint(Vector3D point) {

		return photonLocations.parallelStream()
				.filter(l -> Double.compare(l.getPoint().distance(point), World.NEARLY_ZERO) <= 0)
				.findFirst();
	}

	/**
	 * @param point
	 * @param distance
	 * @return all photons at are at most {@code distance} away from the given
	 *         point
	 */
	public List<PhotonMap.Entry> getPhotonsCloseToPoint(Vector3D point, double distance) {

		return photonLocations.parallelStream()
				.filter(l -> Double.compare(l.getPoint().distance(point), distance) <= 0)
				.collect(Collectors.toCollection(LinkedList::new));
	}

	/**
	 * Calculate the total illumination afforded by the photon map to the given
	 * point, using the default of 8 sampled photons.
	 * 
	 * @param point
	 * @param normal
	 * @return the calculated total illumination
	 */
	public RawColor getIlluminationAtPoint(Vector3D point, Vector3D normal) {

		return getIlluminationAtPoint(point, normal, 8);
	}

	/**
	 * Calculate the total illumination afforded by the photon map to the given
	 * point, using the {@code sampleSize} photons nearest to the given point.
	 * 
	 * @param point
	 * @param normal
	 * @param sampleSize
	 * @return the calculated total illumination
	 */
	public RawColor getIlluminationAtPoint(Vector3D point, Vector3D normal, int sampleSize) {

		if (currentlyPopulating)
			return new RawColor();

		Collection<PhotonMap.Entry> closePhotons = getNClosestEntries(point, sampleSize, photonMap);
		if (closePhotons.isEmpty())
			return new RawColor();

		double furthestDistance = closePhotons.stream()
				.map(e -> e.getPoint().distance(point))
				.max(Double::compare)
				.get();

		final Function<Double, Double> gaussianFilter = (d) -> 0.918
				* (1d - (1d - FastMath.exp(-1.953 * FastMath.pow(d, 2d) / (2d * FastMath.pow(furthestDistance, 2d))))
						/ (1d - FastMath.exp(-1.953)));

		return closePhotons.parallelStream()
				.filter(e -> e.getFromDirection().negate().dotProduct(normal) >= 0d)
				.map(e -> e.getColor()
						.multiplyScalar(1d / (FastMath.PI * FastMath.pow(furthestDistance, 2d)))
						.multiplyScalar(gaussianFilter.apply(e.getPoint().distance(point))))
				.reduce(new RawColor(), (c1, c2) -> c1.add(c2));

	}

	/**
	 * An entry within a {@link PhotonMap}. Describes the color (i.e.,
	 * frequency) of the incoming photon, its location within the map, and the
	 * direction it came from.
	 * 
	 * @author snowjak88
	 *
	 */
	public static class Entry {

		private RawColor color;

		private Vector3D point, fromDirection;

		/**
		 * Construct a new {@link PhotonMap} entry.
		 * 
		 * @param color
		 * @param point
		 * @param fromDirection
		 */
		public Entry(RawColor color, Vector3D point, Vector3D fromDirection) {
			this.color = color;
			this.point = point;
			this.fromDirection = fromDirection;
		}

		/**
		 * @return this photon's color (i.e., frequency)
		 */
		public RawColor getColor() {

			return color;
		}

		/**
		 * @return this photon's location within the {@link PhotonMap}
		 */
		public Vector3D getPoint() {

			return point;
		}

		/**
		 * @return the direction from which this photon arrived at its ultimate
		 *         location
		 */
		public Vector3D getFromDirection() {

			return fromDirection;
		}

	}

	/**
	 * Retrieve the {@code n} PhotonMap {@link Entry}s stored in the given
	 * {@link Kd3dTree} instance that are nearest to the given {@code point}
	 * 
	 * @param point
	 * @param n
	 * @param tree
	 * @return the {@code n} nearest photon-map entries
	 */
	public List<PhotonMap.Entry> getNClosestEntries(Vector3D point, int n, Kd3dTree tree) {

		List<Entry> aboveResults = new LinkedList<>(), beneathResults = new LinkedList<>();

		boolean pointIsAbove = Double.compare(tree.getDimension().getCoordinateFromPoint(point),
				tree.getDimension().getCoordinateFromPoint(tree.getEntry().getPoint())) > 0;
		boolean pointIsBeneath = !pointIsAbove;

		if (tree.getBeneath() != null && pointIsBeneath)
			beneathResults = getNClosestEntries(point, n, tree.getBeneath());

		if (tree.getAbove() != null && pointIsAbove)
			aboveResults = getNClosestEntries(point, n, tree.getAbove());

		List<Entry> results = new LinkedList<>();
		results.addAll(beneathResults);
		results.addAll(aboveResults);

		double bestChildDistance = results.stream()
				.map(e -> e.getPoint().distanceSq(point))
				.min((d1, d2) -> Double.compare(d1, d2))
				.orElse(Double.MAX_VALUE);
		double searchPointDimensionalDistance = FastMath.abs(tree.getDimension().getCoordinateFromPoint(point)
				- tree.getDimension().getCoordinateFromPoint(tree.getEntry().getPoint()));

		if (Double.compare(bestChildDistance, tree.getEntry().getPoint().distanceSq(point)) > 0) {
			results.add(tree.getEntry());
		}

		if (Double.compare(FastMath.sqrt(bestChildDistance), searchPointDimensionalDistance) >= 0) {
			if (tree.getBeneath() != null && pointIsAbove)
				results.addAll(getNClosestEntries(point, n, tree.getBeneath()));
			else if (tree.getAbove() != null && pointIsBeneath)
				results.addAll(getNClosestEntries(point, n, tree.getAbove()));
		}

		return results.stream()
				.sorted((e1, e2) -> Double.compare(e1.getPoint().distanceSq(point), e2.getPoint().distanceSq(point)))
				.limit(n)
				.collect(Collectors.toCollection(LinkedList::new));
	}

	/**
	 * Get all PhotonMap {@link Entry}s stored in the given {@link Kd3dTree}
	 * that are within {@code distance} of the given point.
	 * 
	 * @param point
	 * @param distance
	 * @param tree
	 * @return all photon-map entries that are within the given distance of the
	 *         specified point
	 */
	public List<PhotonMap.Entry> getNearbyEntries(Vector3D point, double distance, Kd3dTree tree) {

		List<Entry> aboveResults = new LinkedList<>(), beneathResults = new LinkedList<>();

		boolean pointIsAbove = Double.compare(tree.getDimension().getCoordinateFromPoint(point),
				tree.getDimension().getCoordinateFromPoint(tree.getEntry().getPoint())) > 0;
		boolean pointIsBeneath = !pointIsAbove;

		if (tree.getBeneath() != null && pointIsBeneath)
			beneathResults = getNearbyEntries(point, distance, tree.getBeneath());

		if (tree.getAbove() != null && pointIsAbove)
			aboveResults = getNearbyEntries(point, distance, tree.getAbove());

		List<Entry> results = new LinkedList<>();
		results.addAll(beneathResults);
		results.addAll(aboveResults);

		double searchPointDimensionalDistance = FastMath.abs(tree.getDimension().getCoordinateFromPoint(point)
				- tree.getDimension().getCoordinateFromPoint(tree.getEntry().getPoint()));

		if (Double.compare(distance, tree.getEntry().getPoint().distance(point)) > 0) {
			results.add(tree.getEntry());
		}

		if (Double.compare(distance, searchPointDimensionalDistance) >= 0) {
			if (tree.getBeneath() != null && pointIsAbove)
				results.addAll(getNearbyEntries(point, distance, tree.getBeneath()));
			else if (tree.getAbove() != null && pointIsBeneath)
				results.addAll(getNearbyEntries(point, distance, tree.getAbove()));
		}

		return results;

	}

	/**
	 * Given a list of photon-map {@link Entry}s, create a {@link Kd3dTree}
	 * instance.
	 * 
	 * @param entryList
	 * @return a new {@link Kd3dTree}
	 */
	public Kd3dTree createKdTreeFromList(List<PhotonMap.Entry> entryList) {

		List<PhotonMap.Entry> sortedList = entryList.stream()
				.sorted((e1, e2) -> Double.compare(e1.getPoint().getNormSq(), e2.getPoint().getNormSq()))
				.collect(Collectors.toCollection(LinkedList::new));

		return createKdTreeFromList(sortedList, Dimension.X, 1);
	}

	private Kd3dTree createKdTreeFromList(List<PhotonMap.Entry> entryList, Dimension currentDimension, int level) {

		int medianIndex = 0;
		if (entryList.size() > 1)
			medianIndex = entryList.size() / 2;
		Entry medianEntry = entryList.get(medianIndex);

		Kd3dTree tree = new Kd3dTree(medianEntry, currentDimension, level);

		if (entryList.size() > 1) {
			List<PhotonMap.Entry> beneathEntries = entryList.parallelStream()
					.filter(e -> Double.compare(currentDimension.getCoordinateFromPoint(e.getPoint()),
							currentDimension.getCoordinateFromPoint(medianEntry.getPoint())) <= 0)
					.filter(e -> e != medianEntry)
					.collect(Collectors.toCollection(LinkedList::new));
			if (!beneathEntries.isEmpty())
				tree.setBeneath(createKdTreeFromList(beneathEntries, currentDimension.getNext(), level + 1));

			List<PhotonMap.Entry> aboveEntries = entryList.parallelStream()
					.filter(e -> Double.compare(currentDimension.getCoordinateFromPoint(e.getPoint()),
							currentDimension.getCoordinateFromPoint(medianEntry.getPoint())) > 0)
					.filter(e -> e != medianEntry)
					.collect(Collectors.toCollection(LinkedList::new));
			if (!aboveEntries.isEmpty())
				tree.setAbove(createKdTreeFromList(aboveEntries, currentDimension.getNext(), level + 1));
		}

		return tree;
	}

	/**
	 * Implements a kd-tree in 3 dimensions.
	 * 
	 * @author snowjak88
	 *
	 */
	@SuppressWarnings("javadoc")
	public static class Kd3dTree {

		private PhotonMap.Entry entry;

		private Dimension dimension;

		private Kd3dTree beneath = null, above = null;

		private int level, size;

		public Kd3dTree(Entry entry, Dimension dimension, int level) {
			this.entry = entry;
			this.dimension = dimension;
			this.level = level;
			this.size = 1;
		}

		public PhotonMap.Entry getEntry() {

			return entry;
		}

		public Dimension getDimension() {

			return dimension;
		}

		public int getLevel() {

			return level;
		}

		public Kd3dTree getBeneath() {

			return beneath;
		}

		public Kd3dTree getAbove() {

			return above;
		}

		public void setBeneath(Kd3dTree beneath) {

			this.beneath = beneath;
		}

		public void setAbove(Kd3dTree above) {

			this.above = above;
		}

		public int getSize() {

			return size + (getBeneath() == null ? 0 : getBeneath().getSize())
					+ (getAbove() == null ? 0 : getAbove().getSize());
		}

		/**
		 * Represents the 3 dimensions used in our implementation of a kd-tree
		 * 
		 * @author snowjak88
		 *
		 */
		public enum Dimension {
			X("X", Vector3D.PLUS_I, "Y"), Y("Y", Vector3D.PLUS_J, "Z"), Z("Z", Vector3D.PLUS_K, "X");

			private Vector3D mask;

			private String label, next;

			Dimension(String label, Vector3D mask, String next) {
				this.label = label;
				this.mask = mask;
				this.next = next;
			}

			public Dimension getNext() {

				return Arrays.asList(Dimension.values())
						.stream()
						.filter(d -> d.label.equalsIgnoreCase(this.next))
						.findFirst()
						.get();
			}

			public double getCoordinateFromPoint(Vector3D point) {

				return point.dotProduct(mask);
			}
		}
	}
}
