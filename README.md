# Chorus Staff

Chorus Staff is an item that teleports the player up to 8 blocks forward for 1 mana when right-clicked. The teleport will end up at a block center before a block with collision or fluids is found.

## Feature

- Teleport to block center.
- Lantency compensation.
- Not move player if be fully blocked.

## The Item

An item where `minecraft:custom_data.id` is "chorus_staff:chorus_staff" is considered as an chorus staff.

An example command that gives an chorus staff to yourslef:

```mcfunction
/give @s diamond_sword[custom_data={id:"chorus_staff:chorus_staff"}]
```

Another example command for the original design:

```mcfunction
/give @s diamond_sword[custom_data={id:"chorus_staff:chorus_staff"},item_name={text:"Chorus Staff"},item_model="diamond_shovel",rarity="rare"]
```

## Configuration

Below is a template config file `config/chorus-staff.json` filled with default values. You may only need to write the lines you would like to modify.

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
  "shouldLantencyCompensation": true,
  "maxLantencyCompensationPredictMilliseconds": 200
}
```

### `manaConsumption`

Mana consumption per use.

### `teleportDistance`

1 to 127 in blocks.
