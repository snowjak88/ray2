package org.snowjak.rays.light.model;

import java.util.List;
import java.util.Optional;

import org.snowjak.rays.Ray;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.light.indirect.PhotonMap;
import org.snowjak.rays.shape.Shape;

public class PhotonMapDecoratingLightingModel implements LightingModel {

	private LightingModel child;

	public PhotonMapDecoratingLightingModel(LightingModel child) {
		this.child = child;
	}

	@Override
	public Optional<LightingResult> determineRayColor(Ray ray, List<Intersection<Shape>> intersections) {

		Optional<LightingResult> returnedChildResult = child.determineRayColor(ray, intersections);
		if (!returnedChildResult.isPresent())
			return Optional.empty();

		if (!PhotonMap.getSingleton().isPhotonMapPopulated())
			return returnedChildResult;

		LightingResult childResult = returnedChildResult.get();

		LightingResult finalResult = new LightingResult();
		finalResult.setEye(childResult.getEye());
		finalResult.setPoint(childResult.getPoint());
		finalResult.setNormal(childResult.getNormal());

		RawColor photonIllumination = PhotonMap.getSingleton().getIlluminationAtPoint(childResult.getPoint());
		RawColor childColor = childResult.getRadiance();
		RawColor litColor = childColor.add(photonIllumination);
		finalResult.setRadiance(litColor);

		return Optional.of(finalResult);
	}

}
