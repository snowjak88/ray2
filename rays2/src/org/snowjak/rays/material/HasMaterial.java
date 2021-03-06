package org.snowjak.rays.material;

/**
 * Denotes that an object has a {@link Material} assigned to it
 * 
 * @author snowjak88
 *
 */
public interface HasMaterial {

	/**
	 * @return this object's assigned Material
	 */
	public Material getMaterial();

	/**
	 * Assign a new Material to this object
	 * 
	 * @param material
	 */
	public void setMaterial(Material material);
}
