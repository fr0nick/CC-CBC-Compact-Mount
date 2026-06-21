CC:CBC Compact Mount turns compact cannon mounts from [CBC: Compact Mount](https://github.com/CubesterYT/CBC-CompactMount) into peripherals for computers from [CC: Tweaked](https://github.com/cc-tweaked/CC-Tweaked/), recreating the functionality of [CC:CBC](https://github.com/Drakon7009/CC-CBC).

## Lua API

Peripheral type: ```compact_cannon_mount```

| **Method** | **Parameters** | **Returns** | **Description** |
| :--- | :--- | :--- | :--- |
| ```setComputerControl``` | ```enabled: boolean``` | ```nil``` | Enables/disables computer control mode. |
| ```isComputerControl``` | - | ```boolean``` | Current computer control mode status. |
| ```setTargetPitch``` | ```pitch: number``` | ```nil``` | Sets vertical angle only. |
| ```getInfo``` | - | ```table``` | Returns cannon telemetry/state. |
| ```assemble``` | ```enabled: boolean``` | ```boolean``` | ```true``` = assemble, ```false``` = disassemble. |
| ```fire``` | ```enabled: boolean``` | ```boolean``` | Controls fire signal (```true``` keeps firing signal active). |
## ```getInfo()``` fields
| **Field** | **Type** | **Description** |
| :--- | :--- | :--- |
| ```computerControl``` | ```boolean``` | Whether computer control mode is active. |
| ```assembled``` | ```boolean``` | Whether the cannon is assembled on the mount. |
| ```pitch``` | ```number``` | Current vertical angle. |
| ```targetPitch``` | ```number``` | Target vertical angle. |
| ```pitchShaftSpeed``` | ```number``` | Pitch interface shaft speed. |
| ```x```, ```y```, ```z``` | ```number``` | ```Cannon Mount``` position in the world. |
