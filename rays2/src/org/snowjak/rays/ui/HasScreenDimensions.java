package org.snowjak.rays.ui;

/**
 * Denotes that this screen's dimensions can be queried
 * 
 * @author snowjak88
 *
 */
public interface HasScreenDimensions {

	/**
	 * @return the screen's minimum X-coordinate
	 */
	int getScreenMinX();

	/**
	 * @return the screen's minimum Y-coordinate
	 */
	int getScreenMinY();

	/**
	 * @return the screen's maximum X-coordinate
	 */
	int getScreenMaxX();

	/**
	 * @return the screen's maximum Y-coordinate
	 */
	int getScreenMaxY();

}