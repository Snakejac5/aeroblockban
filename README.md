<img src="src/main/resources/assets/aeroblockban/icon.png" alt="Logo" width=200>
Aero Block Ban is a utility mod that prevents certain blocks from being used inside simulated contraptions.

Github will be available soon for any issues. 

Designed for Create-based servers, it allows operators to restrict blocks that may cause issues on simulated contraptions while leaving normal block usage unchanged.

In singleplayer, you can run operator commands if you have cheats enabled. 

## Features

- Block specific blocks from simulated contraptions.
- Supports vanilla and modded blocks.
- Add or remove restrictions while the server is running.
- Supports adding blocks from block tags.
- Saves restrictions between restarts.

## Commands

All commands require operator permissions.

```
/aeroblockban add <block>
```
Adds a block to the deny list. (requires OP)

```
/aeroblockban add <block tag>
```
Adds all blocks from a block tag to the deny list. (requires OP)

```
/aeroblockban remove <block>
```
Removes a block from the deny list. (requires OP)

```
/aeroblockban list
```
Displays the current deny list.
