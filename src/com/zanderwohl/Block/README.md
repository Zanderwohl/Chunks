# com.zanderwohl.Block
Classes that have to do with blocks and how they exist in the world and interact with the user.

## Block
A class that contains information about a single type of block, including textures.

Blocks are not hard-coded. Rather, they are loaded in from jason files found in that block's domain folder
(e.g. "default" or "colors") in the folder "/[domain]/blocks/". A block existing in that folder will not add it into the
game. It must also be listed in the domain.json file found at the root of that domain's folder.

Here is an example of a block's json file:
```json
{
    "name":"dirt",
    "color": {
                "r":"127",
                "g":"101",
                "b":"34"
            },
    "texture":"dirt.png"
}
```

Note that color is for use in maps.

The texture value describes a file stored in the domain's folder.

Blocks can have textures that are different on different sides. This means replacing the string which describes the
block texture with a json object that has keys and values describing the textures of each side:

```
...
"texture": {
		"top":"grass_top.png",
		"front":"grass_side.png",
		"left":"grass_side.png",
		"right":"grass_side.png",
		"back":"grass_side.png",
		"bottom":"dirt.png"
}
...
```

Alternatively, a block can have a top and bottom, with all the sides the same:

```json
...
"texture": {
		"top":"log_end.png",
		"bottom":"log_end.png",
		"sides":"log_sides.png"
}
...
```

Once the textures are loaded into the block, they are stored in this order:

0: Top, 1: Front, 2: Left, 3: Right, 4: Back, 5: Bottom

## BlockLibrary

An object that manages all the blocks in particular world. It is responsible for assigning block ids to each block.

Receives blocks through the addition of a domain. Takes all blocks from the domain.json file and loads their properties
by loading the json files of each block.

It also manages multiple domains and can find block ids by name and vice-versa. 