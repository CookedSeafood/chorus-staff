# Chorus Staff

A **chorus staff** is a casting utility that is used to teleport the caster forward.

| Statistics ||
| - | - |
| Mana Consumption| 1 |
| Rarity | Rare |

## Usage

### Teleport

Pressing use while holding a chorus staff in main hand teleports the player up to 8 blocks forward and consumes 1 mana. The teleport will end up at a block center before a block with collision or fluids is found.

#### Mana Consumption with Ultilization

| Base Mana Consumption | Utilization I | Utilization II | Utilization III | Utilization IV | Utilization V |
| :-: | :-: | :-: | :-: | :-: | :-: |
| 1.0 | 0.9 | 0.8 | 0.7 | 0.6 | 0.5 |

## Data Powered

An item where `minecraft:custom_data.id` is "chorus_staff:chorus_staff" is considered as an chorus staff.

### Give Command

```mcfunction
/give @s minecraft:stick[custom_data={id:"chorus_staff:chorus_staff"},enchantable={value:15},item_name={text:"Chorus Staff"},item_model="minecraft:diamond_shovel",rarity="rare"]
```

### Loot Table Entry

```json
{
    "type": "minecraft:item",
    "functions": [
        {
            "function": "minecraft:set_components",
            "components": {
                "minecraft:custom_data": {
                    "id": "chorus_staff:chorus_staff"
                },
                "minecraft:enchantable": {
                    "value": 15
                },
                "minecraft:item_model": "minecraft:diamond_shovel",
                "minecraft:rarity": "rare"
            }
        },
        {
            "function": "minecraft:set_name",
            "name": {
                "text": "Chorus Staff"
            },
            "target": "item_name"
        }
    ],
    "name": "minecraft:stick"
}
```

## Configuration

Below is a template config file `config/chorus-staff.json` filled with default values. You may only need to write the lines you would like to modify.

```json
{
  "manaConsumption": 1.0,
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

### `teleportDistance`

1 to 127 in blocks.
