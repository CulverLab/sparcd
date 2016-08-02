/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package view;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

public class SanimalIconLoader
{
	private static SanimalIconLoader instance = new SanimalIconLoader();
	private final ThreadPoolExecutor iconLoader = new ScheduledThreadPoolExecutor(5);

	private SanimalIconLoader()
	{
	}

	public static SanimalIconLoader getInstance()
	{
		return instance;
	}

	public void scheduleTask(Runnable runnable)
	{
		this.iconLoader.submit(runnable);
	}
}
