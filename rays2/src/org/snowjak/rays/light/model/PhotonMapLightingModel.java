package org.snowjak.rays.light.model;

import java.util.Optional;

import org.snowjak.rays.Ray;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.light.indirect.PhotonMap;
import org.snowjak.rays.shape.Shape;

/**
 * Uses a {@link PhotonMap} to provide indirect illumination. Photon mapping is
 * most useful for providing caustics, although it can also be leveraged for
 * indirect diffuse lighting and internal scattering (albeit <em>very</em>
 * inefficiently for the second).
 * 
 * @author snowjak88
 *
 */
@Deprecated
public class PhotonMapLightingModel implements LightingModel {

	private int sampleSize;

	/**
	 * Construct a new {@link PhotonMapLightingModel} using the default number
	 * of photons to sample (i.e., 8 photons per point).
	 */
	public PhotonMapLightingModel() {
		this(8);
	}

	/**
	 * Construct a new {@link PhotonMapLightingModel} using the specified number
	 * of photons to sample.
	 * 
	 * @param sampleSize
	 */
	public PhotonMapLightingModel(int sampleSize) {
		this.sampleSize = sampleSize;
	}

	@Override
	public Optional<LightingResult> determineRayColor(Ray ray, Optional<Intersection<Shape>> intersection) {

		if (!intersection.isPresent())
			return Optional.empty();

		if (!PhotonMap.getSingleton().isPhotonMapPopulated())
			return Optional.empty();

		Intersection<Shape> intersect = intersection.get();

		LightingResult lightingResult = new LightingResult();
		lightingResult.setEye(ray);
		lightingResult.setPoint(intersect.getPoint());
		lightingResult.setNormal(intersect.getNormal());

		RawColor photonIllumination = PhotonMap.getSingleton().getIlluminationAtPoint(intersect.getPoint(),
				intersect.getNormal(), sampleSize);
		RawColor surfaceColor = intersect.getDiffuse(intersect.getPoint());
		RawColor litColor = surfaceColor.multiply(photonIllumination);
		lightingResult.setRadiance(litColor);

		return Optional.of(lightingResult);
	}

}
