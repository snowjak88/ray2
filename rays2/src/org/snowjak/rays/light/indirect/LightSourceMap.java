package org.snowjak.rays.light.indirect;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Ray;
import org.snowjak.rays.RaytracerContext;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.world.World;

/**
 * A light-source map allows us to perform more efficient photon-casting.
 * <p>
 * <ol>
 * <li>a mapping is established between the spherical coordinate-system centered
 * on the light-source and a 2-dimensional U/V map</li>
 * <li>the sphere of rays emanating from the light-source is sampled to populate
 * the 2-d map with a rough image of the surrounding world</li>
 * <li>that map is then consulted when selecting directions in which to cast
 * photons</li>
 * </ol>
 * </p>
 * <p>
 * Especially, we can note in the map which encountered shapes have specular
 * properties (and so can generate caustics and therefore require denser
 * photon-casting).
 * </p>
 * 
 * @author snowjak88
 *
 */
public class LightSourceMap {

	private LightSourceMap.Entry[][] entries = null;

	private double degreesPerMapEntry = 0d;

	private int samplingRaysPerMapEntry = 0;

	private Shape lightSource = null;

	private static final Random RND = new Random();

	/**
	 * Create a new LightSourceMap, centered on the given emissive Shape, with
	 * map-entries spanning such-and-such degrees of arc in each angular axis.
	 * The final map has a 2-d resolution of {@code [2u x u]}, where
	 * {@code u = 180 / degreesPerMapEntry}
	 * 
	 * @param lightSource
	 * @param degreesPerMapEntry
	 * @param samplingRaysPerMapEntry
	 */
	public LightSourceMap(Shape lightSource, double degreesPerMapEntry, int samplingRaysPerMapEntry) {
		int entriesPerHemisphere = (int) FastMath.round(FastMath.ceil(180d / degreesPerMapEntry));

		this.lightSource = lightSource;
		this.degreesPerMapEntry = 180d / (double) entriesPerHemisphere;
		this.entries = new Entry[2 * entriesPerHemisphere][entriesPerHemisphere];
		this.samplingRaysPerMapEntry = samplingRaysPerMapEntry;
	}

	/**
	 * Get the {@link Entry}s in this LightSourceMap that fulfill some
	 * predicate.
	 * 
	 * @param predicate
	 * @return the entries in the map that fulfill the predicate
	 */
	public List<Entry> getEntries(Predicate<Entry> predicate) {

		List<Entry> result = new LinkedList<>();

		for (int u = 0; u < entries.length; u++) {

			Integer threadsafe_u = new Integer(u);
			try {
				result.addAll(RaytracerContext.getSingleton().getWorkerThreadPool().submit(() -> {

					List<Entry> r = new LinkedList<>();

					for (int v = 0; v < entries[threadsafe_u].length; v++) {
						Entry entry = getEntry(threadsafe_u, v);
						if (predicate.test(entry))
							result.add(entry);
					}

					return r;

				}).get());

			} catch (InterruptedException | ExecutionException e) {
				System.err.println("Interrupted while generating photon-map -- '" + e.getMessage() + "'.");
				e.printStackTrace(System.err);
				System.exit(0);
			}
		}

		return result;
	}

	/**
	 * Get the {@link Entry} located at [u,v] in this map. If that entry has not
	 * yet been created, then the world is sampled and the entry added to the
	 * map.
	 * 
	 * @param u
	 * @param v
	 * @return the [u,v]th Entry in the map
	 */
	public LightSourceMap.Entry getEntry(int u, int v) {

		if (u < 0 || u >= entries.length)
			throw new ArrayIndexOutOfBoundsException("Given 'u' is out of bounds [0, " + entries.length + ").");
		if (v < 0 || v >= entries[u].length)
			throw new ArrayIndexOutOfBoundsException("Given 'v' is out of bounds [0, " + entries[u].length + ").");

		if (entries[u][v] == null)
			entries[u][v] = buildEntry(u, v);

		return entries[u][v];
	}

	private Entry buildEntry(int u, int v) {

		boolean isShape = false, isSpecular = false;

		for (int r = 0; r < samplingRaysPerMapEntry; r++) {

			double sampleU = (double) u + RND.nextDouble();
			double sampleV = (double) v + RND.nextDouble();

			Coordinates sampleCoordinates = new Coordinates(sampleU, sampleV);
			Vector3D sampleDirection = sampleCoordinates.getUnitVector();
			Ray sampleRay = new Ray(lightSource.getLocation(), sampleDirection);

			Optional<Intersection<Shape>> closestIntersection = RaytracerContext.getSingleton()
					.getCurrentWorld()
					.getShapeIntersections(sampleRay)
					.stream()
					.filter(i -> i.getIntersected() != lightSource)
					.sorted((i1, i2) -> Double.compare(i1.getDistanceFromRayOrigin(), i2.getDistanceFromRayOrigin()))
					.findFirst();

			if (closestIntersection.isPresent()) {
				isShape = true;

				Vector3D intersectionPoint = closestIntersection.get().getPoint();
				boolean isNonZeroAlbedo = closestIntersection.get()
						.getEnteringMaterial()
						.getAlbedo(intersectionPoint) >= World.NEARLY_ZERO;
				boolean isNonZeroTransparency = closestIntersection.get()
						.getEnteringMaterial()
						.getSurfaceTransparency(intersectionPoint) >= World.NEARLY_ZERO;

				if (isNonZeroAlbedo || isNonZeroTransparency)
					isSpecular = true;

				if (isShape && isSpecular)
					break;
			}
		}

		return new Entry(u, v, isShape, isSpecular);
	}

	/**
	 * A single entry in a {@link LightSourceMap}.
	 * 
	 * @author snowjak88
	 *
	 */
	@SuppressWarnings("javadoc")
	public static class Entry {

		private boolean isShape, isSpecular;

		private int u, v;

		public Entry(int u, int v, boolean isShape, boolean isSpecular) {
			this.u = u;
			this.v = v;
			this.isShape = isShape;
			this.isSpecular = isSpecular;
		}

		public boolean isShape() {

			return isShape;
		}

		public boolean isSpecular() {

			return isSpecular;
		}

		public void setShape(boolean isShape) {

			this.isShape = isShape;
		}

		public void setSpecular(boolean isSpecular) {

			this.isSpecular = isSpecular;
		}

		public int getU() {

			return u;
		}

		public int getV() {

			return v;
		}
	}

	/**
	 * Defines a single set of coordiantes to a {@link LightSourceMap}. Allows
	 * for conversion between theta/phi angles and u/v coordinates.
	 * 
	 * <p>
	 * These angles are defined as follows.
	 * <dl>
	 * <dt>Theta</dt>
	 * <dd>Longitude -- distance east/west around the sphere. In the range [0,
	 * 2π). Maps to {@code u}.</dd>
	 * <dt>Phi</dt>
	 * <dd>Latitude -- distance north/south along the sphere. In the range
	 * [-π/2, +π/2]. Maps to {@code v}.</dd>
	 * </p>
	 * 
	 * @author snowjak88
	 *
	 */
	public class Coordinates {

		private static final double RADIANS_PER_DEGREE = FastMath.PI / 180d;

		private double theta, phi;

		/**
		 * Create a new Coordinates at LightSourceMap entry located at [u,v].
		 * 
		 * @param u
		 * @param v
		 */
		public Coordinates(double u, double v) {
			double entryHalfDegreeSpan = degreesPerMapEntry / 2d;

			if (u >= entries.length)
				throw new ArrayIndexOutOfBoundsException(
						"Given 'u' index (" + u + ") is out of bounds [0," + entries.length + ")");
			if (v >= entries[(int) FastMath.floor(u)].length)
				throw new ArrayIndexOutOfBoundsException("Given 'v' index (" + v + ") is out of bounds [0,"
						+ entries[(int) FastMath.floor(u)].length + ")");

			theta = (u / (double) entries.length) * 360d + entryHalfDegreeSpan;
			phi = (v / (double) entries[(int) FastMath.floor(u)].length) * 180d - 90d + entryHalfDegreeSpan;
		}

		/**
		 * @return the LightSourceMap entry u-coordinate associated with these
		 *         Coordinates
		 */
		public int getU() {

			return (int) (FastMath.round(theta / 360d) * ((double) entries.length));
		}

		/**
		 * @return the LightSourceMap entry v-coordinate associated with these
		 *         Coordinates
		 */
		public int getV() {

			return (int) (FastMath.round((phi + 90d) / 180d) * ((double) entries[0].length));
		}

		/**
		 * @return the theta-angle associated with these Coordinates
		 */
		public double getTheta() {

			return theta;
		}

		/**
		 * @return the phi-angle associated with these Coordinates
		 */
		public double getPhi() {

			return phi;
		}

		/**
		 * Calculate the unit-vector (in 3-space) which this Coordinates
		 * expresses.
		 * <p>
		 * The unit-vector is calculated from theta/phi angles as:
		 * 
		 * <pre>
		 *     [ x: cos(theta) * cos(phi) ]
		 * v = [ y: sin(phi)              ]
		 *     [ z: sin(theta) * cos(phi) ]
		 * </pre>
		 * </p>
		 * 
		 * @return the calculated unit-vector
		 */
		public Vector3D getUnitVector() {

			double theta_rad = theta * RADIANS_PER_DEGREE, phi_rad = phi * RADIANS_PER_DEGREE;

			return new Vector3D(FastMath.cos(theta_rad) * FastMath.cos(phi_rad), FastMath.sin(phi_rad),
					FastMath.sin(theta_rad) * FastMath.cos(phi_rad));
		}
	}
}
