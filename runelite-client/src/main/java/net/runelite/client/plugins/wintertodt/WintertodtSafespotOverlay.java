/*
 * Copyright (c) 2018, EmptySet <https://github.com/OTRD5k>
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

package net.runelite.client.plugins.wintertodt;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;

public class WintertodtSafespotOverlay extends Overlay
{
	private final Client client;
	private final WintertodtPlugin plugin;
	private final WintertodtConfig config;

	private static final int MAX_DISTANCE = 2350; //Grabbed from the cannon plugin, magic number to keep overlay from rendering in unloaded tiles

	@Inject
	WintertodtSafespotOverlay(Client client, WintertodtPlugin plugin, WintertodtConfig config)
	{
		setPosition(OverlayPosition.DYNAMIC);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.showSafespot() && plugin.isInWintertodt())
		{
			WorldPoint[] safeSpots = plugin.getSafespots();
			LocalPoint localLocation = client.getLocalPlayer().getLocalLocation();

			for (int i = 0; i < safeSpots.length; i++)
			{
				LocalPoint safeSpotPoint = LocalPoint.fromWorld(client, safeSpots[i]);
				Polygon poly = Perspective.getCanvasTilePoly(client, safeSpotPoint);

				if (poly != null  && localLocation.distanceTo(safeSpotPoint) <= MAX_DISTANCE)
				{
					OverlayUtil.renderPolygon(graphics, poly, Color.GREEN);
				}
			}
		}

		return null;
	}
}
