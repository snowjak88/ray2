package org.snowjak.rays.light;

import org.snowjak.rays.color.ColorScheme;

/**
 * Denotes a Builder for a type that implements or extends {@link CanEmitLight}.
 * 
 * @author snowjak88
 * @param <T>
 *            a type that implements or extends {@link CanEmitLight}
 *
 */
public interface CanEmitLightBuilder<T extends CanEmitLight> {

	/**
	 * Add an emissive {@link ColorScheme} to this in-progress object.
	 * 
	 * @param emissiveColor
	 * @return this Builder, for method-chaining
	 */
	public CanEmitLightBuilder<T> emissive(ColorScheme emissiveColor);
}
