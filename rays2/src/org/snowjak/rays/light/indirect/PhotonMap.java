package org.snowjak.rays.light.indirect;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.Ray;
import org.snowjak.rays.RaytracerContext;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.light.indirect.LightSourceMap.Coordinates;
import org.snowjak.rays.light.model.FresnelLightingModel.FresnelResult;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.util.KdTree;
import org.snowjak.rays.world.World;

/**
 * A photon-map represents a collection of photon intersections with various
 * surfaces in the world.
 * <p>
 * Behind the scenes, this class relies on {@link KdTree} in order to provide
 * efficient lookups.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class PhotonMap {

	private KdTree<PhotonEntry, Double> tree = null;

	private static final Random RND = new Random();

	/**
	 * Build a new {@link PhotonMap}. See
	 * {@link #build(int, boolean, double, int, double, double)} for more
	 * details.
	 * 
	 * <p>
	 * The map takes default values for:
	 * <ul>
	 * <li>Degrees per {@link LightSourceMap} entry = 5</li>
	 * <li>Sampling ray count per {@link LightSourceMap} entry = 16</li>
	 * <li>Photon cull threshold = 0.1</li>
	 * <li>Photon cull probability = 0.2</li>
	 * </ul>
	 * </p>
	 * 
	 * @param photonCount
	 * @param isCausticsMap
	 * @return a new PhotonMap instance
	 */
	public static PhotonMap build(int photonCount, boolean isCausticsMap) {

		return build(photonCount, isCausticsMap, 5d, 16, 0.1, 0.2);
	}

	/**
	 * Build a new {@link PhotonMap}. A number of photons ({@code photonCount})
	 * are distributed among all currently-known light-sources and shot towards
	 * all visible shapes in the world. If {@code isCausticsMap = true}, then
	 * only shapes with <em>specular properties</em> are considered.
	 * <p>
	 * A shape has <em>specular properties</em> if any of the following are
	 * true:
	 * <ul>
	 * <li>albedo > 0</li>
	 * <li>surface-transparency > 0</li>
	 * </ul>
	 * See {@link Material#getAlbedo()},
	 * {@link Material#getSurfaceTransparency()}.
	 * </p>
	 * 
	 * @param photonCount
	 * @param isCausticsMap
	 * @param degreesPerLightSourceMapEntry
	 * @param rayCountPerLightSourceMapEntry
	 * @param photonCullThreshold
	 * @param photonCullProbability
	 * @return a new PhotonMap instance
	 */
	public static PhotonMap build(int photonCount, boolean isCausticsMap, double degreesPerLightSourceMapEntry,
			int rayCountPerLightSourceMapEntry, double photonCullThreshold, double photonCullProbability) {

		World world = RaytracerContext.getSingleton().getCurrentWorld();

		double photonsPerUnitRadiance = photonCount / world.getEmissiveShapes()
				.parallelStream()
				.map(s -> s.getEmissive(s.getLocation()).orElse(new RawColor()).getLuminance())
				.reduce((d1, d2) -> d1 + d2)
				.orElse(1d);

		PhotonMap photonMap = new PhotonMap();

		Collection<PhotonEntry> photonEntries = world.getEmissiveShapes().parallelStream().flatMap(s -> {
			LightSourceMap lightSourceMap = new LightSourceMap(s, degreesPerLightSourceMapEntry,
					rayCountPerLightSourceMapEntry);

			List<LightSourceMap.Entry> shapeMapEntries = lightSourceMap
					.getEntries(e -> e.isShape() && (!isCausticsMap || e.isSpecular())),
					allMapEntries = lightSourceMap.getEntries(e -> true);

			double shapeEmissiveLuminance = s.getEmissive(s.getLocation()).orElse(new RawColor()).getLuminance();
			double ratioMapEntriesWithShapes = (double) shapeMapEntries.size() / (double) allMapEntries.size();
			double photonEnergyScale = 1d / (photonsPerUnitRadiance * shapeEmissiveLuminance)
					* (ratioMapEntriesWithShapes);

			return IntStream
					.range(1,
							(int) FastMath.round(photonsPerUnitRadiance
									* s.getEmissive(s.getLocation()).orElse(new RawColor()).getLuminance()))
					.mapToObj(i -> {

						Ray sampleRay = null;
						Optional<Intersection<Shape>> testIntersection = null;
						do {
							LightSourceMap.Entry selectedEntry = shapeMapEntries
									.get(RND.nextInt(shapeMapEntries.size()));

							double sampleU = selectedEntry.getU() + RND.nextDouble(),
									sampleV = selectedEntry.getV() + RND.nextDouble();
							Coordinates sampleCoordinates = lightSourceMap.new Coordinates(sampleU, sampleV);
							Vector3D sampleDirection = sampleCoordinates.getUnitVector();
							sampleRay = new Ray(s.getLocation(), sampleDirection);

							if (isCausticsMap)
								testIntersection = world.getShapeIntersections(sampleRay)
										.stream()
										.sequential()
										.filter(inter -> inter.getIntersected() != s)
										.findFirst();

						} while (!isCausticsMap || !isSpecularMaterial(testIntersection.get().getEnteringMaterial(),
								testIntersection.get().getPoint()));

						RawColor photonRadiance = s.getEmissive(sampleRay.getOrigin())
								.orElse(new RawColor())
								.multiplyScalar(photonEnergyScale);

						double weight = 1d;

						return followPhoton(s, sampleRay, photonRadiance, weight, isCausticsMap, photonCullThreshold,
								photonCullProbability);
					});
		}).flatMap(cpe -> cpe.stream()).collect(Collectors.toCollection(LinkedList::new));

		photonMap.addAll(photonEntries);

		return photonMap;
	}

	private static Collection<PhotonEntry> followPhoton(Shape emittingShape, Ray currentRay, RawColor photonRadiance,
			double weight, boolean acceptOnlySpecular, double photonCullThreshold, double photonCullProbability) {

		Optional<Intersection<Shape>> closestIntersection = RaytracerContext.getSingleton()
				.getCurrentWorld()
				.getShapeIntersections(currentRay)
				.stream()
				.sequential()
				.filter(i -> i.getIntersected() != emittingShape)
				.findFirst();

		if (!closestIntersection.isPresent())
			return Collections.emptyList();

		Vector3D intersectionPoint = closestIntersection.get().getPoint();

		Material intersectionMaterial = closestIntersection.get().getEnteringMaterial();
		if (acceptOnlySpecular && !isSpecularMaterial(intersectionMaterial, intersectionPoint))
			return Collections.emptyList();

		double intersectTransparency = intersectionMaterial.getSurfaceTransparency(intersectionPoint);
		double intersectAlbedo = intersectionMaterial.getAlbedo(intersectionPoint);

		if (weight <= photonCullThreshold) {
			double p = RND.nextDouble();
			if (p < photonCullProbability)
				return Collections.emptyList();

			weight /= (1d - photonCullProbability);
		}

		Collection<PhotonEntry> results = new LinkedList<>();

		FresnelResult fresnelResult = new FresnelResult(closestIntersection.get());
		double reflectProbability = fresnelResult.getReflectance() * intersectAlbedo;
		double transmitProbability = fresnelResult.getTransmittance() * intersectTransparency;

		Ray nextStageRay = null;
		RawColor nextStageRadiance = null;

		double randomNumber = RND.nextDouble();
		if (randomNumber <= reflectProbability) {
			// Do (specular) reflection
			nextStageRay = fresnelResult.getReflectedRay();
			nextStageRadiance = photonRadiance;
			weight *= intersectAlbedo;

		} else if (randomNumber <= (reflectProbability + transmitProbability)) {
			// Do transmittance
			nextStageRay = fresnelResult.getRefractedRay();
			nextStageRadiance = photonRadiance;
			weight *= intersectTransparency;

		} else {
			results.add(new PhotonEntry(intersectionPoint, currentRay.getVector().negate(), photonRadiance));

			// Do diffuse reflection
			RawColor diffuseSurfaceColor = closestIntersection.get().getDiffuse(intersectionPoint);

			Vector3D randomDirection = new EnumeratedDistribution<>(IntStream.range(1, 16).mapToObj(i -> {
				double theta = 2d * RND.nextDouble() * FastMath.PI;
				double phi = 0.5 * RND.nextDouble() * FastMath.PI;
				Vector3D vect = new Vector3D(FastMath.cos(theta) * FastMath.cos(phi), FastMath.sin(phi),
						FastMath.sin(theta) * FastMath.cos(phi));
				return new Pair<>(vect, vect.dotProduct(Vector3D.PLUS_J));
			}).collect(Collectors.toCollection(LinkedList::new))).sample();

			nextStageRay = new Ray(intersectionPoint, randomDirection);
			nextStageRadiance = photonRadiance.multiply(diffuseSurfaceColor);
			weight *= intersectAlbedo;
		}

		results.addAll(followPhoton(emittingShape, nextStageRay, nextStageRadiance, weight, false, photonCullThreshold,
				photonCullProbability));
		return results;
	}

	private static boolean isSpecularMaterial(Material material, Vector3D point) {

		return (material.getAlbedo(point) > 0d) || (material.getSurfaceTransparency(point) > 0d);
	}

	/**
	 * Create a new (empty) photon map.
	 */
	public PhotonMap() {
		this.tree = new KdTree<>(3);
	}

	/**
	 * Add a {@link PhotonEntry} to this map.
	 * 
	 * @param entry
	 */
	public void add(PhotonEntry entry) {

		this.tree.addPoint(entry);
	}

	/**
	 * Add a collection of {@link PhotonEntry}s to this map.
	 * 
	 * @param entries
	 */
	public void addAll(Collection<PhotonEntry> entries) {

		this.tree.addPoints(entries);
	}

	/**
	 * @return a new PhotonMap, being this map after rebalancing
	 */
	public PhotonMap rebalance() {

		PhotonMap result = new PhotonMap();
		result.tree.addPoints(this.tree.getAllPoints());
		return result;
	}

	/**
	 * Calculate the direct intensity afforded by this photon-map at the given
	 * {@code point}, given the surface {@code normal}. Gather, at most,
	 * {@code photonCount} photons.
	 * 
	 * @param point
	 * @param normal
	 * @param photonCount
	 * @return the calculated direct intensity.
	 */
	public RawColor getIntensityAt(Vector3D point, Vector3D normal, int photonCount) {

		Collection<PhotonEntry> closePhotons = tree.getNClosestPointsTo(
				new PhotonEntry(point, Vector3D.ZERO, new RawColor()), photonCount,
				(Predicate<PhotonEntry>) (p) -> p.getArrivalFromDirection().dotProduct(normal) > 0d);

		double maxDistance = closePhotons.parallelStream()
				.map(p -> p.getIntersectPoint().distance(point))
				.max(Double::compare)
				.orElse(Double.MAX_VALUE);

		double radianceScale = 1d / (FastMath.PI * FastMath.pow(maxDistance, 2d));

		return closePhotons.parallelStream()
				.map(p -> p.getColor()
						.multiplyScalar(radianceScale)
						.multiplyScalar(p.getArrivalFromDirection().dotProduct(normal))
						.multiplyScalar(1d - (p.getIntersectPoint().distance(point) / maxDistance)))
				.reduce(new RawColor(), RawColor::add);
	}
}
