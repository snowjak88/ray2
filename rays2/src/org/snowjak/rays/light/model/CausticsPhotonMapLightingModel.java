package org.snowjak.rays.light.model;

import java.time.Instant;
import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.light.indirect.PhotonMap;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.util.ExecutionTimeTracker;

/**
 * Models caustics illumination by means of a photon-map. The photon-map is
 * visualized directly -- i.e., it is consulted for radiance right at the
 * intersection-point.
 * 
 * @author snowjak88
 *
 */
public class CausticsPhotonMapLightingModel implements LightingModel {

	private PhotonMap causticsMap = null;

	private int photonCount = 0;

	/**
	 * Construct a new CausticsPhotonMapLightingModel, referring to the
	 * specified {@link PhotonMap} and using {@code photonCount} # of photons in
	 * every radiance-estimation.
	 * 
	 * @param causticsMap
	 * @param photonCount
	 * @param maxDistance
	 */
	public CausticsPhotonMapLightingModel(PhotonMap causticsMap, int photonCount) {
		this.causticsMap = causticsMap;
		this.photonCount = photonCount;

		System.out.println("Photon-map size: " + causticsMap.getSize());
	}

	@Override
	public Optional<RawColor> determineRayColor(Ray ray, Optional<Intersection<Shape>> intersection) {

		Instant start = Instant.now();

		if (!intersection.isPresent())
			return Optional.empty();

		Vector3D point = intersection.get().getPoint();
		Vector3D normal = intersection.get().getNormal();

		RawColor diffuseColor = intersection.get().getDiffuse(point);

		RawColor photonRadiance = causticsMap.getIntensityAt(point, normal, photonCount);

		RawColor result = photonRadiance.multiply(diffuseColor);

		ExecutionTimeTracker.logExecutionRecord("CausticsPhotonMapLightingModel", start, Instant.now(), null);

		return Optional.of(result);
	}

}
