package org.snowjak.rays.material;

import org.snowjak.rays.builder.Builder;

/**
 * Denotes that a Builder is capable of building types that implement
 * {@link HasMaterial}
 * 
 * @author snowjak88
 *
 * @param <T>
 *            a type that implements {@link HasMaterial}
 */
public interface HasMaterialBuilder<T extends HasMaterial> extends Builder<T> {

	/**
	 * Add a {@link Material} to this in-progress object
	 * 
	 * @param material
	 * @return this Builder, for method-chaining
	 */
	public HasMaterialBuilder<T> material(Material material);

}
