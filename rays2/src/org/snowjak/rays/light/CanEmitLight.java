package org.snowjak.rays.light;

import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.color.ColorScheme;
import org.snowjak.rays.color.FunctionalColorScheme;
import org.snowjak.rays.color.RawColor;

/**
 * Denotes that an object can emit radiance, and so be a candidate for
 * consideration as a light-source.
 * 
 * @author snowjak88
 *
 */
public interface CanEmitLight {

	/**
	 * @return <code>true</code> if this object has been assigned an emissive
	 *         {@link ColorScheme}, and <code>false</code> if not
	 * @see #setEmissiveColorScheme(ColorScheme)
	 */
	public default boolean isEmissive() {

		return getEmissiveColorScheme().isPresent();
	}

	/**
	 * @param worldCoord
	 * @return this object's emissive color for the given global location
	 */
	public default Optional<RawColor> getEmissive(Vector3D worldCoord) {

		if (!isEmissive())
			return Optional.empty();

		return Optional.of(getEmissiveColorScheme().get().getColorForWorld(worldCoord));
	}

	/**
	 * @param worldX
	 * @param worldY
	 * @param worldZ
	 * @return this object's emissive color for the given global location
	 */
	public default Optional<RawColor> getEmissive(double worldX, double worldY, double worldZ) {

		if (!isEmissive())
			return Optional.empty();

		return Optional.of(getEmissiveColorScheme().get().getColorForWorld(worldX, worldY, worldZ));
	}

	/**
	 * @return this object's emissive {@link ColorScheme}, if it is assigned
	 */
	public Optional<ColorScheme> getEmissiveColorScheme();

	/**
	 * Set this object's emissive {@link ColorScheme}.
	 * 
	 * @param emissiveColorScheme
	 */
	public void setEmissiveColorScheme(Optional<ColorScheme> emissiveColorScheme);

	/**
	 * Set this object's emissive {@link ColorScheme}.
	 * 
	 * @param emissiveColorScheme
	 */
	public default void setEmissiveColorScheme(ColorScheme emissiveColorScheme) {

		setEmissiveColorScheme(Optional.of(emissiveColorScheme));
	}

	/**
	 * Set this object's emissive ColorScheme to use the provided
	 * {@link Function}
	 * 
	 * @param functionalEmissiveColor
	 */
	public default void setEmissiveColorScheme(Function<Vector3D, RawColor> functionalEmissiveColor) {

		setEmissiveColorScheme(new FunctionalColorScheme(functionalEmissiveColor));
	}

}
