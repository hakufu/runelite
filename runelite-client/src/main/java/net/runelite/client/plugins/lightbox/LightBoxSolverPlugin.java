/*
 * Copyright (c) 2018, Not Noob
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.lightbox;

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.Overlay;

import javax.inject.Inject;
import java.util.Arrays;

@PluginDescriptor(
	name = "Light box solver"
)
public class LightBoxSolverPlugin extends Plugin
{
	@Inject
	private LightBoxSolverOverlay overlay;

	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Getter
	private int count = 0;

	@Getter
	private int[] solution = new int[8];

	@Getter
	private boolean solving = false;

	private boolean[][] lightBox = new boolean[5][5];
	private boolean[][][] lightBoxChanges = new boolean[8][5][5];
	private final boolean[][] solvedState = new boolean[][]
			{{true, true, true, true, true},
			{true, true, true, true, true},
			{true, true, true, true, true},
			{true, true, true, true, true},
			{true, true, true, true, true}};

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (client.getWidget(322, 3) != null)
		{
			boolean changed = false;
			boolean[][] tempLightBox = new boolean[5][5];

			int index = 0;
			for (Widget light : client.getWidget(322, 3).getDynamicChildren())
			{
				tempLightBox[index / 5][index % 5] = light.getItemId() == 20357;
				index++;
			}

			for (int h = 0; h < 5; h++)
			{
				for (int k = 0; k < 5; k++)
				{
					if (tempLightBox[h][k] != lightBox[h][k])
					{
						if (!solving)
						{
							lightBoxChanges[count][h][k] = true;
						}
						changed = true;
					}
				}
			}

			if (changed && !solving)
			{
				lightBox = deepCopy(tempLightBox);

				if (count < 7)
				{
					count++;
				}
				else
				{
					solving = true;
					count = 0;
					solution = solve();
				}
			}

			if (changed && solving)
			{
				lightBox = deepCopy(tempLightBox);
				count++;
			}
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == 322)
		{
			int index = 0;
			for (Widget light : client.getWidget(322, 3).getDynamicChildren())
			{
				lightBox[index / 5][index % 5] = light.getItemId() == 20357;
				index++;
			}
			count = 0;
			solving = false;
			lightBoxChanges = new boolean[8][5][5];
		}
	}

	private int[] solve()
	{
		int[] current = new int[] {0, -1, -1, -1, -1, -1, -1, -1};
		boolean[][] tempLightBox;
		int currentCount = 0;

		while (current[7] != 7)
		{
			tempLightBox = deepCopy(lightBox);

			while (current[currentCount] != -1)
			{
				tempLightBox = xor(current[currentCount], tempLightBox).clone();
				currentCount++;
				if (currentCount == 8)
				{
					return null;
				}
			}

			currentCount = 0;
			if (Arrays.deepEquals(tempLightBox, solvedState))
			{
				break;
			}

			current[0]++;

			if (current[0] == 8)
			{
				current[0] = 0;
				current[1]++;
			}

			for (int i = 1; i < 7; i++)
			{
				if (current[i] == 7)
				{
					current[i] = 0;
					if (current[i + 1] != 7)
					{
						current[i + 1]++;
					}
					else
					{
						return null;
					}
				}
			}
		}
		return current;
	}

	private boolean[][] xor(int num, boolean[][] box)
	{
		boolean[][] tempLightBox = box.clone();
		for (int i = 0; i < 5; i++)
		{
			for (int j = 0; j < 5; j++)
			{
				if (lightBoxChanges[num][i][j])
				{
					tempLightBox[i][j] = !tempLightBox[i][j];
				}
			}
		}
		return tempLightBox;
	}

	private boolean[][] deepCopy(boolean[][] original)
	{
		if (original == null)
		{
			return null;
		}

		final boolean[][] result = new boolean[original.length][];
		for (int i = 0; i < original.length; i++)
		{
			result[i] = Arrays.copyOf(original[i], original[i].length);
		}
		return result;
	}

	@Override
	public Overlay getOverlay()
	{
		return overlay;
	}
}
