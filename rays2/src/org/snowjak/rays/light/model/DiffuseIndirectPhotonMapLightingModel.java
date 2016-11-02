package org.snowjak.rays.light.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Ray;
import org.snowjak.rays.RaytracerContext;
import org.snowjak.rays.antialias.SuperSamplingAntialiaser;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.light.indirect.PhotonMap;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.shape.Shape;

public class DiffuseIndirectPhotonMapLightingModel implements LightingModel {

	private PhotonMap globalMap = null;

	private int samplingRayCount = 0;

	private int photonCount = 0;

	private SuperSamplingAntialiaser<Vector3D, Optional<RawColor>, RawColor> sampler = new SuperSamplingAntialiaser<>();

	private static final Random RND = new Random();

	public DiffuseIndirectPhotonMapLightingModel(PhotonMap globalMap, int samplingRayCount, int photonCount) {
		this.globalMap = globalMap;
		this.samplingRayCount = samplingRayCount;
		this.photonCount = photonCount;
	}

	@Override
	public Optional<LightingResult> determineRayColor(Ray ray, Optional<Intersection<Shape>> intersection) {

		if (!intersection.isPresent())
			return Optional.empty();

		Intersection<Shape> intersect = intersection.get();
		Vector3D point = intersect.getPoint();
		Vector3D normal = intersect.getNormal();
		Shape intersected = intersect.getIntersected();
		Material material = intersect.getEnteringMaterial();

		Ray reflectedRay = new FresnelLightingModel.FresnelResult(intersect).getReflectedRay();

		RawColor sampledRadiance = sampler.execute(reflectedRay.getVector(), (v) -> {
			Collection<Vector3D> sampleVectors = new LinkedList<>();
			sampleVectors.add(v);
			sampleVectors.addAll(IntStream.range(1, samplingRayCount).mapToObj(i -> {
				Vector3D sampleVector = null;
				do {
					double theta = 2d * RND.nextDouble() * FastMath.PI;
					double phi = 0.5d * RND.nextDouble() * FastMath.PI;
					sampleVector = new Vector3D(FastMath.cos(theta) * FastMath.cos(phi), FastMath.sin(phi),
							FastMath.sin(theta) * FastMath.cos(phi));
				} while (FastMath.pow(sampleVector.dotProduct(v), (1d / (1d - material.getAlbedo(point)))) < 0.5);

				return sampleVector;
			}).collect(Collectors.toCollection(LinkedList::new)));
			return sampleVectors;

		}, (v) -> {
			Optional<Intersection<Shape>> sampledIntersection = RaytracerContext.getSingleton()
					.getCurrentWorld()
					.getShapeIntersections(new Ray(point, v))
					.stream()
					.sequential()
					.filter(i -> i.getIntersected() != intersected)
					.findFirst();
			if (!sampledIntersection.isPresent())
				return Optional.empty();

			return Optional.of(globalMap.getIntensityAt(sampledIntersection.get().getPoint(),
					sampledIntersection.get().getNormal(), photonCount));

		}, (cp) -> cp.parallelStream()
				.map(p -> p.getValue())
				.filter(orc -> orc.isPresent())
				.map(orc -> orc.get())
				.reduce((c1, c2) -> c1.add(c2))
				.orElse(new RawColor())
				.multiplyScalar(1d / (double) cp.size()));

		RawColor resultingRadiance = sampledRadiance.multiply(intersect.getDiffuse(point));

		LightingResult result = new LightingResult();
		result.setEye(ray);
		result.setPoint(point);
		result.setNormal(normal);
		result.setRadiance(resultingRadiance);
		return Optional.of(result);
	}

}
