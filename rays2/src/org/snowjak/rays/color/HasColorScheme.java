package org.snowjak.rays.color;

import java.util.function.Function;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Indicates that an object has a {@link ColorScheme} associated with it.
 * 
 * @author snowjak88
 *
 */
public interface HasColorScheme {

	/**
	 * @param worldCoord
	 * @return this object's diffuse color for the given global location
	 */
	public default RawColor getDiffuse(Vector3D worldCoord) {

		return getDiffuseColorScheme().getColorForWorld(worldCoord);
	}

	/**
	 * @param worldX
	 * @param worldY
	 * @param worldZ
	 * @return this object's diffuse color for the given global location
	 */
	public default RawColor getDiffuse(double worldX, double worldY, double worldZ) {

		return getDiffuseColorScheme().getColorForWorld(worldX, worldY, worldZ);
	}

	/**
	 * @param worldCoord
	 * @return this object's specular color for the given global location
	 */
	public default RawColor getSpecular(Vector3D worldCoord) {

		return getSpecularColorScheme().getColorForWorld(worldCoord);
	}

	/**
	 * @param worldX
	 * @param worldY
	 * @param worldZ
	 * @return this object's specular color for the given global location
	 */
	public default RawColor getSpecular(double worldX, double worldY, double worldZ) {

		return getSpecularColorScheme().getColorForWorld(worldX, worldY, worldZ);
	}

	/**
	 * @param worldCoord
	 * @return this object's emissive color for the given global location
	 */
	public default RawColor getEmissive(Vector3D worldCoord) {

		return getEmissiveColorScheme().getColorForWorld(worldCoord);
	}

	/**
	 * @param worldX
	 * @param worldY
	 * @param worldZ
	 * @return this object's emissive color for the given global location
	 */
	public default RawColor getEmissive(double worldX, double worldY, double worldZ) {

		return getEmissiveColorScheme().getColorForWorld(worldX, worldY, worldZ);
	}

	/**
	 * @return this object's diffuse {@link ColorScheme}
	 */
	public ColorScheme getDiffuseColorScheme();

	/**
	 * Set this object's diffuse {@link ColorScheme}.
	 * 
	 * @param diffuseColorScheme
	 */
	public void setDiffuseColorScheme(ColorScheme diffuseColorScheme);

	/**
	 * Set this object's diffuse ColorScheme to use the provided
	 * {@link Function}
	 * 
	 * @param functionalDiffuseColor
	 */
	public default void setDiffuseColorScheme(Function<Vector3D, RawColor> functionalDiffuseColor) {

		setDiffuseColorScheme(new FunctionalColorScheme(functionalDiffuseColor));
	}

	/**
	 * @return this object's specular {@link ColorScheme}
	 */
	public ColorScheme getSpecularColorScheme();

	/**
	 * Set this object's specular {@link ColorScheme}.
	 * 
	 * @param specularColorScheme
	 */
	public void setSpecularColorScheme(ColorScheme specularColorScheme);

	/**
	 * Set this object's specular ColorScheme to use the provided
	 * {@link Function}
	 * 
	 * @param functionalSpecularColor
	 */
	public default void setSpecularColorScheme(Function<Vector3D, RawColor> functionalSpecularColor) {

		setSpecularColorScheme(new FunctionalColorScheme(functionalSpecularColor));
	}

	/**
	 * @return this object's emissive {@link ColorScheme}
	 */
	public ColorScheme getEmissiveColorScheme();

	/**
	 * Set this object's emissive {@link ColorScheme}.
	 * 
	 * @param emissiveColorScheme
	 */
	public void setEmissiveColorScheme(ColorScheme emissiveColorScheme);

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
