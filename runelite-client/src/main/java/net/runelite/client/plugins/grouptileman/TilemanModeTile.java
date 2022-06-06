/*
 * Copyright (c) 2018, TheLonelyDev <https://github.com/TheLonelyDev>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
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
package net.runelite.client.plugins.grouptileman;

import lombok.Getter;
import net.runelite.api.World;
import net.runelite.api.coords.WorldPoint;

import java.awt.*;

/**
 * Used for serialization of ground marker points.
 */

class TilemanModeTile
{
	//Flags
	public static final int TILE_REMOTE = 0b1;
	public static final int TILE_FROM_OTHER = 0b10;

	@Getter
	private final WorldPoint point;
	@Getter
	private final int flags;
	@Getter
	private final byte player;


	public TilemanModeTile(int regionId, int regionX, int regionY, int z, byte player, int flags)
	{
		this.point = WorldPoint.fromRegion(regionId, regionX, regionY, z);
		this.flags = flags;
		this.player = player;
	}

	public int getRegionId() {
		return point.getRegionID();
	}

	public int getRegionX() {
		return point.getRegionX();
	}

	public int getRegionY() {
		return point.getRegionY();
	}

	public int getZ()
	{
		return point.getPlane();
	}

	@Override
	public boolean equals(Object o)
	{
		TilemanModeTile t = (TilemanModeTile) o;
		return point.equals(t.point);
	}

	public Color getColor()
	{
		return getColor(player);
	}

	public static Color getColor(byte player)
	{
		int r = ((player << 7) & 0b10000000) +
				((player << 4) & 0b01000000) +
				(player       &  0b00100000);
		int g = ((player << 6) & 0b10000000) +
				((player << 3) & 0b01000000) +
				((player >> 1) & 0b00100000);
		int b = ((player << 2) & 0b10000000) +
				(                0b01000000) +
				((player >> 2) & 0b00100000);

		r |= 0b00011111;
		g |= 0b00011111;
		b |= 0b00011111;

		return new Color(r, g, b);
	}
}
