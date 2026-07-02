# OpenShulker

OpenShulker is a plugin for 1.14 to 26.2 that makes Shulkerboxes more useful.

You can open a Shulker by Shift-Rightclicking it in your hand or Inventory.

This plugin aims to replace the now inactive resource "BetterShulkerBoxes".

You can download it on spigotmc: [OpenShulker](https://www.spigotmc.org/resources/openshulker.111948/)

# Installation

1. Download or compile the JAR
2. Drop the JAR in your server's plugin folder
3. Restart your server

# Configuration

```
# Sound for opening a Shulker
OpenSound: "BLOCK_SHULKER_BOX_OPEN"
# Sound for closing a Shulker
CloseSound: "BLOCK_SHULKER_BOX_CLOSE"
Hooks:
  # If ChestSort is installed and this is true, opened shulker inventories are marked as unsortable.
  ChestSort: true
  # If WorldGuard is installed and this is true, shulkers inside protected containers can only be used where the player has container access.
  WorldGuard: true
# Ingame admin commands:
# /openshulker reload - reloads this config
# /openshulker hooks - shows if ChestSort/WorldGuard hooks are enabled and loaded
Messages:
  Prefix: "&8[&2OpenShulker&8] &7"
  CannotBreakContainer: "&cYou cannot break this container, since there's an opened shulker in it"
  OpenShulkerCommand:
    Syntax: "&cSyntax: &4/<LABEL> <Reload|Hooks>"
    Reloaded: "The plugin was reloaded!"
```

# Permissions

- `openshulker.use` -> To open shulkers
- `openshulker.write` -> To make changes to shulker's inventory
- `openshulker.admin` -> To use /openshulker

# Commands

- `openshulker reload`
- `openshulker hooks`

# Support

Discord: [https://discord.gg/GxEFhVY6ff](https://discord.gg/GxEFhVY6ff)

Github: [Issues Page](https://github.com/Test-Account666/OpenShulker/issues)

# Todo

- Add update checker
