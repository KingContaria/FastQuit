## FastQuit

### About this mod

This mod lets you return to the title screen early while your world is still saving in the background.

Don't worry, if you quit Minecraft while the world is still saving, it will finish saving before closing the game.

While the world is saving, it won't show up on the world list, this is intended!

### Download this mod

Once it is processed, you can download this mod on [Modrinth](https://modrinth.com/mod/fastquit)!

### Configure

To configure the mod, go to .minecraft/config/fastquit-config.txt
There is currently two options to configure:

**showToasts**:
Determines whether a toast gets shown when a world finishes saving

**backgroundPriority**:
Sets the thread priority of the server when saving worlds in the background.
This is done to improve client performance while saving, but will make the saving take longer over all.
Value has to be between 0 and 10, setting it to 0 will disable changing thread priority

### Problems?

If you are experiencing any problems, please open an issue on the [issue tracker](https://github.com/KingContaria/FastQuit/issues)!