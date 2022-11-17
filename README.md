## FastQuit

### About this mod

This mod lets you return to the title screen early while your world is still saving in the background.

Don't worry, if you quit Minecraft while the world is still saving, it will finish saving before closing the game.

If you try to rejoin the same world while it is still saving, the game will wait until its finished before loading it.

### Download this mod

You can download this mod on [Modrinth](https://modrinth.com/mod/fastquit)!

### Configure

To configure the mod, go to .minecraft/config/fastquit-config.txt or open the config screen from [ModMenu](https://modrinth.com/mod/modmenu).

There is currently three options to configure:

**showToasts**:
Determines whether a toast gets shown when a world finishes saving

**backgroundPriority**:
Sets the thread priority of the server when saving worlds in the background.
This is done to improve client performance while saving, but will make the saving take longer over all.
Value has to be between 0 and 10, setting it to 0 will disable changing thread priority

**renderSavingScreen**:
When playing on high render distance, quitting the world can still take a bit because the client-side chunk storage has to be cleared.
By enabling this setting the "Saving world" screen will be rendered.

### Thanks to...

#### Translators:
- Felix14-v2 (russian)
####

### Problems?

If you are experiencing any problems, please open an issue on the [issue tracker](https://github.com/KingContaria/FastQuit/issues)!
