package fr0nick.ccbccompactmount.api;

public interface IComputerControllableCannon {
   boolean cc_isComputerControlled();

   void cc_setComputerControlled(boolean var1);

   float cc_getCannonPitch();

   float cc_getTargetPitch();

   void cc_setTargetPitch(float var1);
}
