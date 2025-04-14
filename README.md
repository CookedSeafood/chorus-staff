# Ender Staff

Ender Staff is an item that teleports the player up to 8 blocks forward for 1 mana when right-clicked. The teleport will end up at a block center before a block with collision or fluids is found.

## Feature

- Lantency compensation.
- Not move player if be fully blocked.

## The Item

An item where `minecraft:item_name` is "Ender Staff" is considered as an ender staff.

An example command that gives the ender staff to yourslef:

```mcfunction
/give @s diamond_sword[item_name="Ender Staff"]
```

Another example command for the original design:

```mcfunction
/give @s diamond_sword[item_model="minecraft:diamond_shovel",item_name="Ender Staff",rarity=rare]
```

## Configuration

Below is a template config file `config/ender-staff.json` filled with default values. You may only need to write the lines you would like to modify.

```json
{
  "manaConsumption": 1,
  "teleportDistance": 8,
  "isParticleVisible": true,
  "particleXWidthScale": 0.5,
  "particleYOffset": 0.25,
  "particleZWidthScale": 0.5,
  "particleCount": 128,
  "particleOffsetXOffset": 0.5,
  "particleOffsetXMultiplier": 2.0,
  "particleOffsetZOffset": 0.5,
  "particleOffsetZMultiplier": 2.0,
  "particleSpeed": 1.0,
  "isLantencyCompensation": true,
  "maxLantencyCompensationPredictMilliseconds": 200
}
```

- `manaConsumption`: Mana consumption per use.
- `teleportDistance`: 1 to 127, in blocks.
