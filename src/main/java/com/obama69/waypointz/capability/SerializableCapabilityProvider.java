package com.obama69.waypointz.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

public class SerializableCapabilityProvider<T> extends CapabilityProvider<T> implements INBTSerializable<Tag> {
	private final INBTSerializable<Tag> serializableInstance;

	@SuppressWarnings("unchecked")
	public SerializableCapabilityProvider(final Capability<T> _capability, final Direction _facing, final T _instance) {
		super(_capability, _facing, _instance);
		
		if (!(instance instanceof INBTSerializable)) {
			throw new IllegalArgumentException("instance must implement INBTSerializable");
		}
		
		serializableInstance = (INBTSerializable<Tag>) instance;
	}

	@Override
	public Tag serializeNBT() {
		return serializableInstance.serializeNBT();
	}

	@Override
	public void deserializeNBT(final Tag nbt) {
		serializableInstance.deserializeNBT(nbt);
	}

}
