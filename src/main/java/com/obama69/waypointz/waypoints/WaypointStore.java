package com.obama69.waypointz.waypoints;

import java.util.List;
import java.util.Optional;

import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.INBTSerializable;

public interface WaypointStore extends INBTSerializable<ListTag> {
	List<Waypoint> getAllWaypoints();
	
	Optional<Waypoint> getWaypoint(final String name, final String world);
	
	List<Waypoint> getWaypointsByWorld(final String world);
	
	boolean addWaypoint(final Waypoint waypoint);
	
	Optional<Waypoint> updateWaypoint(final String name, final String world, final Waypoint waypoint);
	
	Optional<Waypoint> removeWaypoint(final String name, final String world);
	
	void replaceWith(final WaypointStore other);
	
	String getWorldName(final ServerPlayer player);
}
