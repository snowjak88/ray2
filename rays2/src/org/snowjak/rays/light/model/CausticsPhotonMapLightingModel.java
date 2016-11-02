package org.snowjak.rays.light.model;

import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.light.indirect.PhotonMap;
import org.snowjak.rays.shape.Shape;

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
	 */
	public CausticsPhotonMapLightingModel(PhotonMap causticsMap, int photonCount) {
		this.causticsMap = causticsMap;
		this.photonCount = photonCount;
	}

	@Override
	public Optional<LightingResult> determineRayColor(Ray ray, Optional<Intersection<Shape>> intersection) {

		if (!intersection.isPresent())
			return Optional.empty();

		Vector3D point = intersection.get().getPoint();
		Vector3D normal = intersection.get().getNormal();

		RawColor diffuseColor = intersection.get().getDiffuse(point);

		RawColor photonRadiance = causticsMap.getIntensityAt(point, normal, photonCount);

		LightingResult result = new LightingResult();
		result.setEye(ray);
		result.setPoint(point);
		result.setNormal(normal);
		result.setRadiance(photonRadiance.multiply(diffuseColor));
		return Optional.of(result);
	}

}
