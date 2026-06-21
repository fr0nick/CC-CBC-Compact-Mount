package fr0nick.ccbccompactmount.peripheral;

import com.cubester.cbc_compact_mount.content.CompactCannonMountBlock;
import com.cubester.cbc_compact_mount.content.CompactCannonMountBlockEntity;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import fr0nick.ccbccompactmount.api.IComputerControllableCannon;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CompactMountPeripheral implements IPeripheral {

    private final CompactCannonMountBlockEntity mount;
    private final IComputerControllableCannon   ctrl;

    private final Set<IComputerAccess> computers = Collections.synchronizedSet(new HashSet<>());

    public CompactMountPeripheral(CompactCannonMountBlockEntity mount) {
        this.mount = mount;
        if (!(mount instanceof IComputerControllableCannon controllable)) {
            throw new IllegalStateException(
                "[CC:CBC Compact Mount] Mixin not applied to CompactCannonMountBlockEntity! " +
                "Ensure that cc_cbc_compact_mount.mixins.json is loaded.");
        }
        this.ctrl = controllable;
    }

    @Override public String getType() { return "compact_cannon_mount"; }

    @Override public void attach(IComputerAccess computer) { computers.add(computer); }

    @Override 
    public void detach(IComputerAccess computer) { 
        computers.remove(computer);
        
        if (computers.isEmpty()) {
            Level level = mount.getLevel();
            if (level != null && !level.isClientSide() && level.getServer() != null) {
                level.getServer().execute(() -> {
                    if (computers.isEmpty() && !mount.isRemoved()) {
                        
                        if (ctrl.cc_isComputerControlled()) {
                            ctrl.cc_setComputerControlled(false);
                        }
                        
                        if (mount.isRunning() && !level.hasNeighborSignal(mount.getBlockPos())) {
                            BlockState state = mount.getBlockState();
                            boolean firePowered = state.getValue(CompactCannonMountBlock.FIRE_POWERED);
                            
                            mount.onRedstoneUpdate(false, true, firePowered, firePowered, 0);
                            clearAssemblyPowered();
                        }
                    }
                });
            }
        }
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        if (this == other) return true;
        return other instanceof CompactMountPeripheral o && this.mount == o.mount;
    }

    @Override
    public Object getTarget() { return mount; }

    @LuaFunction(mainThread = true)
    public final boolean setComputerControl(boolean control) {
        ctrl.cc_setComputerControlled(control);
        return ctrl.cc_isComputerControlled();
    }

    @LuaFunction(mainThread = true)
    public final boolean isComputerControl() {
        return ctrl.cc_isComputerControlled();
    }

    @LuaFunction(mainThread = true)
    public final void setTargetPitch(double pitch) throws LuaException {
        if (!Double.isFinite(pitch)) throw new LuaException("pitch must be a finite number");
        ctrl.cc_setTargetPitch((float) pitch);
    }

    @LuaFunction(mainThread = true)
    public final boolean assemble(boolean enabled) {
        BlockState state = mount.getBlockState();
        if (enabled) {
            if (!mount.isRunning()) {
                boolean firePowered = state.getValue(CompactCannonMountBlock.FIRE_POWERED);
                mount.onRedstoneUpdate(true, false, firePowered, firePowered, firePowered ? 15 : 0);
            }
            clearAssemblyPowered();
            return mount.isRunning();
        } else {
            if (mount.isRunning()) {
                mount.disassemble();
            }
            clearAssemblyPowered();
            return mount.isRunning();
        }
    }

    private void clearAssemblyPowered() {
        BlockState state = mount.getBlockState();
        if (state.getValue(CompactCannonMountBlock.ASSEMBLY_POWERED) && mount.getLevel() != null) {
            mount.getLevel().setBlock(mount.getBlockPos(), state.setValue(CompactCannonMountBlock.ASSEMBLY_POWERED, false), 3);
        }
    }

    @LuaFunction(mainThread = true)
    public final boolean fire(boolean enabled) {
        BlockState state = mount.getBlockState();
        boolean assemblyPowered = state.getValue(CompactCannonMountBlock.ASSEMBLY_POWERED);
        boolean prevFirePowered = state.getValue(CompactCannonMountBlock.FIRE_POWERED);
        
        mount.onRedstoneUpdate(assemblyPowered, assemblyPowered, enabled, prevFirePowered, enabled ? 15 : 0);
        return mount.getBlockState().getValue(CompactCannonMountBlock.FIRE_POWERED);
    }

    @LuaFunction(mainThread = true)
    public final Map<String, Object> getInfo() {
        PitchOrientedContraptionEntity contraption = mount.getContraption();
        Map<String, Object> info = new HashMap<>();

        info.put("computerControl", ctrl.cc_isComputerControlled());
        info.put("assembled",       contraption != null);

        info.put("pitch",           (double) ctrl.cc_getCannonPitch());
        info.put("targetPitch",     (double) ctrl.cc_getTargetPitch());
        info.put("pitchShaftSpeed", (double) mount.getSpeed());

        BlockPos pos = mount.getBlockPos();
        info.put("x", pos.getX());
        info.put("y", pos.getY());
        info.put("z", pos.getZ());

        return info;
    }
}