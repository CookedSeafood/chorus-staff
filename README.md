# Ender Staff

Ender Staff is a staff that teleports the player 8 blocks forward for 1 mana when right-clicked. The teleport will end up at a block center before a block with collision or fluids is found.

A "Ender Staff" is an item where `minecraft:item_name` is "Ender Staff".

An example command to get it:

```mcfunction
/give @s diamond_sword[item_name="Ender Staff"]
```

Get the original design of it:

```mcfunction
/give @s diamond_sword[item_model="minecraft:diamond_shovel",item_name="Ender Staff",rarity=rare]
```

## Configuration

Below is a template config file `config/ender-staff.json` filled with default values. You may only need to write the lines you would like to modify.

```json
{
  "manaConsumption": 1,
  "teleportDistance": 8
}
```

- `manaConsumption`: Mana consumption per use.
- `teleportDistance`: 1 to 127, in blocks.
