package fr0nick.ccbccompactmount;

import com.cubester.cbc_compact_mount.CMBlocks;
import com.cubester.cbc_compact_mount.content.CompactCannonMountBlockEntity;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import fr0nick.ccbccompactmount.peripheral.CompactMountPeripheral;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod("cc_cbc_compact_mount")
public class CCBCCompactMount {
   public static final String MOD_ID = "cc_cbc_compact_mount";

   public CCBCCompactMount(IEventBus modEventBus) {
      modEventBus.addListener(this::onRegisterCapabilities);
   }

   private void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
      event.registerBlock(PeripheralCapability.get(), (level, pos, state, be, side) -> {
         if (be instanceof CompactCannonMountBlockEntity mount) {
            return new CompactMountPeripheral(mount);
         } else {
            return null;
         }
      }, new Block[]{(Block)CMBlocks.COMPACT_CANNON_MOUNT.get()});
   }
}
