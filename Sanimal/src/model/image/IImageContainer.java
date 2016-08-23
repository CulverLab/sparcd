/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.image;

import java.io.File;
import java.io.Serializable;

public interface IImageContainer extends Serializable
{
	public File getFile();

	public void setFile(File file);
}
