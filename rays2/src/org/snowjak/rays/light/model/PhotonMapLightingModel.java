package org.snowjak.rays.light.model;

import java.util.List;
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
public class PhotonMapLightingModel implements LightingModel {

	@Override
	public Optional<LightingResult> determineRayColor(Ray ray, List<Intersection<Shape>> intersections) {

		if (intersections.isEmpty())
			return Optional.empty();

		if (!PhotonMap.getSingleton().isPhotonMapPopulated())
			return Optional.empty();

		Intersection<Shape> intersect = intersections.get(0);

		LightingResult lightingResult = new LightingResult();
		lightingResult.setEye(ray);
		lightingResult.setPoint(intersect.getPoint());
		lightingResult.setNormal(intersect.getNormal());

		RawColor photonIllumination = PhotonMap.getSingleton().getIlluminationAtPoint(intersect.getPoint(), 1d);
		RawColor surfaceColor = intersect.getDiffuse(intersect.getPoint());
		RawColor litColor = surfaceColor.multiply(photonIllumination);
		lightingResult.setRadiance(litColor);

		return Optional.of(lightingResult);
	}

}
