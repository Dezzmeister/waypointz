package com.obama69.waypointz.waypoints;

import net.minecraft.world.phys.Vec3;

public class Waypoint {
	public final String name;
	public final Vec3 location;
	public final String world;
	
	public Waypoint(final String _name, final Vec3 _location, final String _world) {
		name = _name;
		location = _location;
		world = _world;
	}
}
