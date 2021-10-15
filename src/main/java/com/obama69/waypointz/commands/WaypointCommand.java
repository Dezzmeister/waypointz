package com.obama69.waypointz.commands;

import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.obama69.waypointz.waypoints.Waypoint;
import com.obama69.waypointz.waypoints.WaypointStore;
import com.obama69.waypointz.waypoints.WaypointStoreCapability;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class WaypointCommand {	
	private static final MutableComponent SUCCESS_MESSAGE_MIDDLE = new TextComponent(" at ").withStyle(ChatFormatting.GREEN);
	
	private static final Style LINK_WAYPOINT_NAME_STYLE = Style.EMPTY.withUnderlined(true).withColor(ChatFormatting.YELLOW);
	
	private static final DynamicCommandExceptionType ERROR_WAYPOINT_NOT_FOUND = new DynamicCommandExceptionType(arg -> {
		return new TextComponent("Waypoint not found");
	});
	
	private static final DynamicCommandExceptionType ERROR_WAYPOINT_ALREADY_EXISTS = new DynamicCommandExceptionType(arg -> {
		return new TextComponent("Waypoint already exists");
	});
	
	private static final DynamicCommandExceptionType UNKNOWN_ERROR = new DynamicCommandExceptionType(arg -> {
		return new TextComponent("Something went wrong");
	});

	public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {		
		dispatcher.register(
				Commands.literal("waypoint")
				.requires(s -> s.hasPermission(0))
				.then(
						Commands.literal("set")
						.then(
								Commands.argument("name", StringArgumentType.string())
								.executes(WaypointCommand::setWaypoint)
						)
				)
				.then(
						Commands.literal("list")
						.executes(WaypointCommand::listWaypoints)
				)
				.then(
						Commands.literal("goto")
						.then(
								Commands.argument("waypoint", StringArgumentType.string())
								.executes(WaypointCommand::gotoWaypoint)
						)
				)
				.then(
						Commands.literal("remove")
						.then(
								Commands.argument("waypoint", StringArgumentType.string())
								.executes(WaypointCommand::removeWaypoint)
						)
				)				
		);
	}
	
	private static int removeWaypoint(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		final ServerPlayer player = context.getSource().getPlayerOrException();
		final String waypointName = StringArgumentType.getString(context, "waypoint");
			
		final WaypointStore store = WaypointStoreCapability.getWaypointStore(player).orElseThrow(() -> UNKNOWN_ERROR.create(waypointName));
		store.getWaypoint(waypointName, store.getWorldName(player)).orElseThrow(() -> ERROR_WAYPOINT_NOT_FOUND.create(waypointName));
		
		store.removeWaypoint(waypointName, store.getWorldName(player));
		
		final MutableComponent message = new TextComponent("Removed waypoint ").withStyle(ChatFormatting.GREEN).append(new TextComponent(waypointName).withStyle(ChatFormatting.YELLOW));
		
		player.sendMessage(message, player.getUUID());
		
		return 1;
	}
	
	private static int gotoWaypoint(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		final ServerPlayer player = context.getSource().getPlayerOrException();
		final String waypointName = StringArgumentType.getString(context, "waypoint");
			
		final WaypointStore store = WaypointStoreCapability.getWaypointStore(player).orElseThrow(() -> UNKNOWN_ERROR.create(waypointName));
		final Waypoint waypoint = store.getWaypoint(waypointName, store.getWorldName(player)).orElseThrow(() -> ERROR_WAYPOINT_NOT_FOUND.create(waypointName));
		final Vec3 loc = waypoint.location;
		
		player.teleportTo(loc.x, loc.y, loc.z);
		
		return 1;
	}
	
	private static int setWaypoint(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		final ServerPlayer player = context.getSource().getPlayerOrException();
		final String name = StringArgumentType.getString(context, "name");
		final Vec3 pos = player.position();
		final WaypointStore store = WaypointStoreCapability.getWaypointStore(player).orElseThrow(() -> UNKNOWN_ERROR.create(name));
		
		if (store.getWaypoint(name, store.getWorldName(player)).isPresent()) {
			throw ERROR_WAYPOINT_ALREADY_EXISTS.create(name);
		}
		
		store.addWaypoint(new Waypoint(name, pos, store.getWorldName(player)));
		
		final String formattedPos = String.format("[%.1f %.1f %.1f]", pos.x, pos.y, pos.z);
		final MutableComponent onHoverText = new TextComponent("Click to teleport to ").withStyle(ChatFormatting.BLUE);
		final ClickEvent onClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/waypoint goto " + name);
		final HoverEvent onHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, onHoverText.append(new TextComponent(name).withStyle(LINK_WAYPOINT_NAME_STYLE)));
		final Style nameStyle = LINK_WAYPOINT_NAME_STYLE.withClickEvent(onClick).withHoverEvent(onHover);
		final MutableComponent nameComponent = new TextComponent(name).withStyle(nameStyle);
		final MutableComponent posComponent = new TextComponent(formattedPos).withStyle(ChatFormatting.BLUE);
		final MutableComponent message = new TextComponent("Created waypoint ").withStyle(ChatFormatting.GREEN).append(nameComponent).append(SUCCESS_MESSAGE_MIDDLE).append(posComponent);
		
		player.sendMessage(message, player.getUUID());
		
		return 1;
	}
	
	private static int listWaypoints(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		final ServerPlayer player = context.getSource().getPlayerOrException();
		
		final WaypointStore store = WaypointStoreCapability.getWaypointStore(player).orElseThrow(() -> UNKNOWN_ERROR.create(""));
		final List<Waypoint> waypoints = store.getWaypointsByWorld(store.getWorldName(player));
		
		if (waypoints.isEmpty()) {
			final MutableComponent message = new TextComponent("You have no waypoints, do ").withStyle(ChatFormatting.GRAY).append(new TextComponent("/waypoint set").withStyle(ChatFormatting.BLUE));
			
			player.sendMessage(message, player.getUUID());
			return 1;
		}
		
		final MutableComponent message = new TextComponent("");
		final MutableComponent SPACER = new TextComponent("   ");
		
		for (final Waypoint waypoint : waypoints) {
			final MutableComponent onHoverText = new TextComponent("Click to teleport to ").withStyle(ChatFormatting.BLUE);
			final ClickEvent onClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/waypoint goto " + waypoint.name);
			final HoverEvent onHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, onHoverText.append(new TextComponent(waypoint.name).withStyle(LINK_WAYPOINT_NAME_STYLE)));
			final MutableComponent name = new TextComponent(waypoint.name).withStyle(LINK_WAYPOINT_NAME_STYLE);
			
			name.setStyle(name.getStyle().withClickEvent(onClick).withHoverEvent(onHover));
			message.append(name);
			message.append(SPACER);
		}
		
		player.sendMessage(message, player.getUUID());
		
		return 1;
	}
}
