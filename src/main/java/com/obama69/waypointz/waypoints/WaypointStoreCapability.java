package com.obama69.waypointz.waypoints;

import com.obama69.waypointz.WayPointz;
import com.obama69.waypointz.capability.SerializableCapabilityProvider;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class WaypointStoreCapability {

	public static final Capability<WaypointStore> WAYPOINT_STORE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
	
	public static final Direction DEFAULT_FACING = null;
	
	public static final ResourceLocation ID = new ResourceLocation(WayPointz.MODID, "waypoint_store");
	
	public static void register(final RegisterCapabilitiesEvent event) {
		event.register(WaypointStore.class);
	}
	
	public static LazyOptional<WaypointStore> getWaypointStore(final LivingEntity entity) {
		if (entity instanceof ServerPlayer) {
			return entity.getCapability(WAYPOINT_STORE_CAPABILITY, DEFAULT_FACING);
		}
		
		return LazyOptional.empty();
	}
	
	public static ICapabilityProvider createProvider(final WaypointStore store) {
		return new SerializableCapabilityProvider<>(WAYPOINT_STORE_CAPABILITY, DEFAULT_FACING, store);
	}

	@Mod.EventBusSubscriber(modid = WayPointz.MODID)
	private static class EventHandler {
		
		@SubscribeEvent
		public static void attachCapabilities(final AttachCapabilitiesEvent<Entity> event) {
			if (!(event.getObject() instanceof ServerPlayer)) {
				return;
			}
			
			final WaypointStore store = new DefaultWaypointStore();
			event.addCapability(ID, createProvider(store));
		}
		
		@SubscribeEvent
		public static void playerClone(final PlayerEvent.Clone event) {
			final Player ogPlayer = event.getOriginal();
			final Player newPlayer = event.getPlayer();
			
			getWaypointStore(ogPlayer).ifPresent(oldStore -> getWaypointStore(newPlayer).ifPresent(newStore -> newStore.replaceWith(oldStore)));
		}
	}
}
