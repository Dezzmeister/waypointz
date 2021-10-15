package com.obama69.waypointz.waypoints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class DefaultWaypointStore implements WaypointStore {
	private final Map<String, Waypoint> waypoints;
	
	public DefaultWaypointStore(final Map<String, Waypoint> _waypoints) {
		waypoints = _waypoints;
	}
	
	public DefaultWaypointStore() {
		waypoints = new HashMap<String, Waypoint>();
	}

	@Override
	public List<Waypoint> getAllWaypoints() {
		return new ArrayList<Waypoint>(waypoints.values());
	}

	@Override
	public Optional<Waypoint> getWaypoint(final String name, final String world) {
		final Waypoint waypoint = waypoints.get(uniqueKey(name, world));
		
		return Optional.ofNullable(waypoint);
	}

	@Override
	public List<Waypoint> getWaypointsByWorld(final String world) {
		final ArrayList<Waypoint> out = new ArrayList<Waypoint>();
 		final Collection<Waypoint> all = waypoints.values();
		
		for (final Waypoint waypoint : all) {
			if (waypoint.world.equals(world)) {
				out.add(waypoint);
			}
		}
		
		return out;
	}

	@Override
	public boolean addWaypoint(final Waypoint waypoint) {
		final String key = uniqueKey(waypoint.name, waypoint.world);
		
		if (waypoints.containsKey(key)) {
			return false;
		}
		
		waypoints.put(key, waypoint);
		return true;
	}

	@Override
	public Optional<Waypoint> updateWaypoint(final String name, final String world, final Waypoint waypoint) {
		final Waypoint old = waypoints.put(uniqueKey(name, world), waypoint);
		
		return Optional.ofNullable(old);
	}

	@Override
	public Optional<Waypoint> removeWaypoint(final String name, final String world) {
		final Waypoint waypoint = waypoints.remove(uniqueKey(name, world));
		
		return Optional.ofNullable(waypoint);
	}
	
	@Override
	public void replaceWith(final WaypointStore other) {
		final List<Waypoint> all = other.getAllWaypoints();
		
		waypoints.clear();
		
		for (final Waypoint waypoint : all) {
			waypoints.put(uniqueKey(waypoint.name, waypoint.world), waypoint);
		}
	}
	
	@Override
	public String getWorldName(final ServerPlayer player) {
		final ServerLevel world = player.getLevel();
		final String worldName = world.toString();
		final String dimensionName = world.dimension().location().toString();
		
		return worldName + "/" + dimensionName;
	}

	@Override
	public ListTag serializeNBT() {
		final ListTag out = new ListTag();
		
		for (final Waypoint waypoint : waypoints.values()) {
			final CompoundTag tag = new CompoundTag();
			
			tag.putString("name", waypoint.name);
			tag.putString("world", waypoint.world);
			tag.putDouble("x", waypoint.location.x);
			tag.putDouble("y", waypoint.location.y);
			tag.putDouble("z", waypoint.location.z);
			
			out.add(tag);
		}
		
		return out;
	}

	@Override
	public void deserializeNBT(ListTag nbt) {
		final List<Waypoint> in = new ArrayList<Waypoint>();
		
		for (final Tag tag : nbt) {
			if (!(tag instanceof CompoundTag)) {
				continue;
			}
			
			final CompoundTag ctag = (CompoundTag) tag;
			final String name = ctag.getString("name");
			final String world = ctag.getString("world");
			final double x = ctag.getDouble("x");
			final double y = ctag.getDouble("y");
			final double z = ctag.getDouble("z");
			
			final Vec3 pos = new Vec3(x, y, z);
			
			final Waypoint waypoint = new Waypoint(name, pos, world);
			
			in.add(waypoint);
		}
		
		for (final Waypoint waypoint : in) {
			waypoints.put(uniqueKey(waypoint.name, waypoint.world), waypoint);
		}
	}
	
	private String uniqueKey(final String name, final String world) {
		return name + "/" + world;
	}
}
