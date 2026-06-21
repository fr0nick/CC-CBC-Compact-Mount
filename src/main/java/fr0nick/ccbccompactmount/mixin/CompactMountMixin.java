package fr0nick.ccbccompactmount.mixin;

import com.cubester.cbc_compact_mount.content.CompactCannonMountBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import fr0nick.ccbccompactmount.api.IComputerControllableCannon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

@Mixin(
   value = {CompactCannonMountBlockEntity.class},
   remap = false
)
public abstract class CompactMountMixin extends KineticBlockEntity implements IComputerControllableCannon {
   @Shadow(
      remap = false
   )
   private float cannonPitch;
   @Shadow(
      remap = false
   )
   private float prevPitch;
   @Shadow(
      remap = false
   )
   private boolean running;
   @Shadow(
      remap = false
   )
   protected PitchOrientedContraptionEntity mountedContraption;
   @Unique
   private boolean ccbcm$computerControlled = false;
   @Unique
   private float ccbcm$targetPitch = 0.0F;

   protected CompactMountMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   public boolean cc_isComputerControlled() {
      return this.ccbcm$computerControlled;
   }

   public float cc_getCannonPitch() {
      return this.cannonPitch;
   }

   public float cc_getTargetPitch() {
      return this.ccbcm$targetPitch;
   }

   public void cc_setComputerControlled(boolean controlled) {
      if (this.ccbcm$computerControlled != controlled) {
         this.ccbcm$computerControlled = controlled;
         if (controlled) {
            this.ccbcm$targetPitch = this.cannonPitch;
         }

         this.setChanged();
         if (this.getLevel() != null && !this.getLevel().isClientSide) {
            this.sendData();
         }

      }
   }

   public void cc_setTargetPitch(float pitch) {
      float clamped = Mth.clamp(pitch, -25.0F, 50.0F);
      if (this.ccbcm$targetPitch != clamped) {
         this.ccbcm$targetPitch = clamped;
         this.setChanged();
         if (this.getLevel() != null && !this.getLevel().isClientSide) {
            this.sendData();
         }
      }

   }

   @Inject(
      method = {"write"},
      at = {@At("TAIL")},
      remap = false
   )
   protected void ccbcm$write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
      compound.putBoolean("CCBCM_Controlled", this.ccbcm$computerControlled);
      compound.putFloat("CCBCM_TargetPitch", this.ccbcm$targetPitch);
   }

   @Inject(
      method = {"read"},
      at = {@At("TAIL")},
      remap = false
   )
   protected void ccbcm$read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
      if (compound.contains("CCBCM_Controlled")) {
         this.ccbcm$computerControlled = compound.getBoolean("CCBCM_Controlled");
      }

      if (compound.contains("CCBCM_TargetPitch")) {
         this.ccbcm$targetPitch = compound.getFloat("CCBCM_TargetPitch");
      }

   }

   @Inject(
      method = {"onSpeedChanged"},
      at = {@At("TAIL")},
      remap = false
   )
   private void ccbcm$onSpeedChanged(float previousSpeed, CallbackInfo ci) {
      if (this.ccbcm$computerControlled) {
         this.ccbcm$computerControlled = false;
         this.setChanged();
         if (this.getLevel() != null && !this.getLevel().isClientSide) {
            this.sendData();
         }
      }

   }

   @Inject(
      method = {"applyRotation"},
      at = {@At("HEAD")},
      remap = false
   )
   private void ccbcm$onApplyRotation(CallbackInfo ci) {
      if (this.ccbcm$computerControlled) {
         this.cannonPitch = this.prevPitch;
         if (this.running && this.mountedContraption != null && !this.mountedContraption.isStalled()) {
            float maxStep = Math.abs(KineticBlockEntity.convertToAngular(this.getSpeed()) * 0.125F);
            if (this.getLevel() != null && this.getLevel().isClientSide) {
               maxStep *= ServerSpeedProvider.get();
            }

            if (maxStep > 0.0F && this.cannonPitch != this.ccbcm$targetPitch) {
               float diff = this.ccbcm$targetPitch - this.cannonPitch;
               if (Math.abs(diff) <= maxStep) {
                  this.cannonPitch = this.ccbcm$targetPitch;
               } else {
                  this.cannonPitch += Math.signum(diff) * maxStep;
               }

               this.cannonPitch = Mth.clamp(this.cannonPitch, -25.0F, 50.0F);
            }
         }
      }

   }

   @Inject(
      method = {"getPitchOffset"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private void ccbcm$getPitchOffset(float partialTicks, CallbackInfoReturnable<Float> cir) {
      if (this.ccbcm$computerControlled) {
         float modifier = this.mountedContraption != null && this.mountedContraption.getInitialOrientation() == Direction.DOWN ? -1.0F : 1.0F;
         if (this.isVirtual()) {
            cir.setReturnValue(Mth.lerp(partialTicks + 0.5F, this.prevPitch, this.cannonPitch) * modifier);
         } else {
            float pt = partialTicks;
            if (this.mountedContraption == null || this.mountedContraption.isStalled() || !this.running) {
               pt = 0.0F;
            }

            cir.setReturnValue(Mth.lerp(pt, this.prevPitch, this.cannonPitch) * modifier);
         }
      }
   }
}
