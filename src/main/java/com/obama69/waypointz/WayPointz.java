package com.obama69.waypointz;

import com.obama69.waypointz.commands.RegisterCommandsEventListener;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("waypointz")
public class WayPointz {
	public static final String MODID = "waypointz";

    public WayPointz() {    	        
        MinecraftForge.EVENT_BUS.register(new RegisterCommandsEventListener());
    }
}
