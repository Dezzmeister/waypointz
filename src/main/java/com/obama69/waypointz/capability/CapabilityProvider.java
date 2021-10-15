package com.obama69.waypointz.capability;

import com.google.common.base.Preconditions;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class CapabilityProvider<T> implements ICapabilityProvider {
	
	public final Capability<T> capability;
	
	public final Direction facing;
	
	public final T instance;
	
	protected final LazyOptional<T> lazyOptional;
	
	public CapabilityProvider(final Capability<T> _capability, final Direction _facing, final T _instance) {
		capability = Preconditions.checkNotNull(_capability, "capability");
		facing = _facing;
		instance = Preconditions.checkNotNull(_instance, "instance");
		
		lazyOptional = LazyOptional.of(() -> instance);
	}

	@Override
	public <U> LazyOptional<U> getCapability(Capability<U> cap, Direction side) {
		return capability.orEmpty(cap, lazyOptional);
	}
}
