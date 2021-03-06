/*
 * Copyright (c) 2017, Aria <aria@ar1as.space>
 * Copyright (c) 2017, Devin French <https://github.com/devinfrench>
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
package net.runelite.client.plugins.zulrah.overlays;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.zulrah.ZulrahInstance;
import net.runelite.client.plugins.zulrah.ZulrahPlugin;
import net.runelite.client.plugins.zulrah.phase.ZulrahPhase;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

@Slf4j
public class ZulrahOverlay extends Overlay
{
	private static final Color TILE_BORDER_COLOR = new Color(0, 0, 0, 100);
	private static final Color NEXT_TEXT_COLOR = new Color(255, 255, 255, 100);

	private final Client client;
	private final ZulrahPlugin plugin;

	@Inject
	ZulrahOverlay(Client client, ZulrahPlugin plugin)
	{
		setPosition(OverlayPosition.DYNAMIC);
		this.client = client;
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		ZulrahInstance instance = plugin.getInstance();

		if (instance == null)
		{
			return null;
		}

		ZulrahPhase currentPhase = instance.getPhase();
		ZulrahPhase nextPhase = instance.getNextPhase();
		if (currentPhase == null)
		{
			return null;
		}

		WorldPoint startWorldPoint = instance.getStartWorldPoint();
		if (nextPhase != null && currentPhase.getStandLocation() == nextPhase.getStandLocation())
		{
			drawStandTiles(graphics, startWorldPoint, currentPhase, nextPhase);
		}
		else
		{
			drawStandTile(graphics, startWorldPoint, currentPhase, false);
			drawStandTile(graphics, startWorldPoint, nextPhase, true);
		}

		return null;
	}

	private void drawStandTiles(Graphics2D graphics, WorldPoint startWorldPoint, ZulrahPhase currentPhase, ZulrahPhase nextPhase)
	{
		WorldPoint world = currentPhase.getStandTile(startWorldPoint);
		LocalPoint local = LocalPoint.fromWorld(client, world);
		Polygon northPoly = getCanvasTileNorthPoly(client, local);
		Polygon southPoly = getCanvasTileSouthPoly(client, local);
		Polygon poly = Perspective.getCanvasTilePoly(client, local);
		Point textLoc = Perspective.getCanvasTextLocation(client, graphics, local, "Next", 0);
		if (northPoly != null && southPoly != null && poly != null && textLoc != null)
		{
			Color northColor = currentPhase.getColor();
			Color southColor = nextPhase.getColor();
			graphics.setColor(northColor);
			graphics.fillPolygon(northPoly);
			graphics.setColor(southColor);
			graphics.fillPolygon(southPoly);
			graphics.setColor(TILE_BORDER_COLOR);
			graphics.setStroke(new BasicStroke(2));
			graphics.drawPolygon(poly);
			graphics.setColor(NEXT_TEXT_COLOR);
			graphics.drawString("Next", textLoc.getX(), textLoc.getY());
		}
		if (nextPhase.isJad())
		{
			Image jadPrayerImg = ZulrahImageManager.getProtectionPrayerBufferedImage(nextPhase.getPrayer());
			if (jadPrayerImg != null)
			{
				Point imageLoc = Perspective.getCanvasImageLocation(client, graphics, local, (BufferedImage) jadPrayerImg, 0);
				if (imageLoc != null)
				{
					graphics.drawImage(jadPrayerImg, imageLoc.getX(), imageLoc.getY(), null);
				}
			}
		}
	}

	private void drawStandTile(Graphics2D graphics, WorldPoint startWorldPoint, ZulrahPhase phase, boolean next)
	{
		if (phase == null)
		{
			return;
		}
		WorldPoint world = phase.getStandTile(startWorldPoint);
		LocalPoint local = LocalPoint.fromWorld(client, world);
		if (local == null)
		{
			return;
		}
		Polygon poly = Perspective.getCanvasTilePoly(client, local);
		Color color = phase.getColor();
		if (poly != null)
		{
			graphics.setColor(TILE_BORDER_COLOR);
			graphics.setStroke(new BasicStroke(2));
			graphics.drawPolygon(poly);
			graphics.setColor(color);
			graphics.fillPolygon(poly);
		}
		if (next)
		{
			Point textLoc = Perspective.getCanvasTextLocation(client, graphics, local, "Next", 0);
			if (textLoc != null)
			{
				graphics.setColor(NEXT_TEXT_COLOR);
				graphics.drawString("Next", textLoc.getX(), textLoc.getY());
			}
			if (phase.isJad())
			{
				Image jadPrayerImg = ZulrahImageManager.getProtectionPrayerBufferedImage(phase.getPrayer());
				if (jadPrayerImg != null)
				{
					Point imageLoc = Perspective.getCanvasImageLocation(client, graphics, local, (BufferedImage) jadPrayerImg, 0);
					if (imageLoc != null)
					{
						graphics.drawImage(jadPrayerImg, imageLoc.getX(), imageLoc.getY(), null);
					}
				}
			}
		}
	}

	private Polygon getCanvasTileNorthPoly(Client client, LocalPoint local)
	{
		int plane = client.getPlane();
		int halfTile = Perspective.LOCAL_TILE_SIZE / 2;

		Point p1 = Perspective.worldToCanvas(client, local.getX() - halfTile, local.getY() - halfTile, plane);
		Point p2 = Perspective.worldToCanvas(client, local.getX() - halfTile, local.getY() + halfTile, plane);
		Point p3 = Perspective.worldToCanvas(client, local.getX() + halfTile, local.getY() + halfTile, plane);

		if (p1 == null || p2 == null || p3 == null)
		{
			return null;
		}

		Polygon poly = new Polygon();
		poly.addPoint(p1.getX(), p1.getY());
		poly.addPoint(p2.getX(), p2.getY());
		poly.addPoint(p3.getX(), p3.getY());

		return poly;
	}

	private Polygon getCanvasTileSouthPoly(Client client, LocalPoint local)
	{
		int plane = client.getPlane();
		int halfTile = Perspective.LOCAL_TILE_SIZE / 2;

		Point p1 = Perspective.worldToCanvas(client, local.getX() - halfTile, local.getY() - halfTile, plane);
		Point p2 = Perspective.worldToCanvas(client, local.getX() + halfTile, local.getY() + halfTile, plane);
		Point p3 = Perspective.worldToCanvas(client, local.getX() + halfTile, local.getY() - halfTile, plane);

		if (p1 == null || p2 == null || p3 == null)
		{
			return null;
		}

		Polygon poly = new Polygon();
		poly.addPoint(p1.getX(), p1.getY());
		poly.addPoint(p2.getX(), p2.getY());
		poly.addPoint(p3.getX(), p3.getY());

		return poly;
	}
}
