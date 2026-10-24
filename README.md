# Nova Client

A Lunar-style PvP client for Minecraft **1.8.9** (Forge), built for Hypixel and Minemen.

## Install

1. Build it: run `gradlew.bat build` in this folder (or use the jar that's already built).
2. Copy `build/libs/novaclient-1.0.0.jar` into `%appdata%\.minecraft\mods`.
3. In the Minecraft Launcher, start the **forge 1.8.9** profile (`1.8.9-forge1.8.9-11.15.1.2318`).
4. In game, press **Right Shift** to open the menu.

## Using it

| Action | How |
|---|---|
| Open / close menu | Right Shift (change it in Options > Controls > Nova Client) |
| Turn a mod on/off | Click its card or its ENABLED/DISABLED bar |
| Mod settings + toggle key | Click **OPTIONS** on the card, or right-click the card |
| Find a mod | Just start typing, or use the filter buttons (ALL, ENABLED, HUD...) |
| Client options (accent color, blur...) | **SETTINGS** tab |

### HUD editor (menu > **EDIT HUD LAYOUT**)

| Action | How |
|---|---|
| Move | Drag. Snaps to the screen center, edges and other elements (blue lines) |
| Move freely | Hold **ALT** while dragging, or turn **SNAP** off |
| Resize | Drag the small corner square, or scroll over the element |
| Fine-tune | Click an element, then arrow keys (hold Shift for 10px) |
| Settings | Right-click the element |
| Hide | Click it and press Delete |
| Start over | **RESET ALL** |

Elements stick to the side of the screen they're closest to, so your layout survives
window and GUI-scale changes.

Everything is saved to `.minecraft/config/novaclient.json`.

### Included mods

- **FPS**, **CPS**, **Ping**, **Coordinates**: text boxes with background/brackets/color (including Chroma)
- **Keystrokes**: WASD, mouse buttons with CPS, space bar
- **Armor Status**: armor and held item with durability
- **Toggle Sprint**: always sprint; set a toggle key in its settings
- **Active Mods**: a list of every mod you have on, sliding in and out as you toggle them

## Adding your own mod

### 1. A simple text mod (easiest)

Create a file in `src/main/java/com/nova/module/impl/`, for example `DayCounter.java`:

```java
package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.TextHudModule;

public class DayCounter extends TextHudModule {
    public DayCounter() {
        //    name          description             category      default x, y
        super("Day Counter", "Shows the world day", Category.HUD, 2, 120);
    }

    @Override
    protected String getText() {
        return "Day " + (mc.theWorld.getWorldTime() / 24000L);
    }
}
```

### 2. Register it

Add one line in `src/main/java/com/nova/module/ModuleManager.java`:

```java
register(new DayCounter());
```

### 3. Build

```
gradlew.bat build
```

Then copy the new jar into your mods folder again.

### Settings

Add settings as fields. They show up in the menu and save automatically:

```java
private final BooleanSetting showSeconds = add(new BooleanSetting("Show seconds", true));      // switch
private final ModeSetting style = add(new ModeSetting("Style", "Short", "Short", "Long"));      // < Short >
private final NumberSetting opacity = add(new NumberSetting("Opacity", 50, 0, 100, 5, "%"));   // slider

// use them with: showSeconds.isOn()   style.is("Long")   opacity.getInt()
```

Give your mod a menu icon by overriding `getIcon()` with any Minecraft item:

```java
@Override
public ItemStack getIcon() {
    return new ItemStack(Items.clock);
}
```

### Other kinds of mods

| You want | Extend | Implement |
|---|---|---|
| One line of text on screen | `TextHudModule` | `getText()` |
| Custom drawing on screen (like Keystrokes) | `HudModule` | `getWidth()`, `getHeight()`, `render(preview)` |
| Something that runs in the background (like Toggle Sprint) | `Module` | `onTick()`, `onEnable()`, `onDisable()` |

Need a Forge event (chat messages, entity rendering, attacks...)? Add a method to your module:

```java
@SubscribeEvent
public void onChat(ClientChatReceivedEvent event) {
    if (!isEnabled()) return;   // required: modules always receive events
    // ...
}
```

For changes Forge events can't reach (like freelook or custom zoom), put Mixin classes in
`src/main/java/com/nova/mixin/`. They're picked up automatically.

### Testing without copying jars

`gradlew.bat testClient` launches a dev Minecraft with the mod loaded (offline account, singleplayer only).
(The template's `runClient` doesn't work with Gradle 8, so use `testClient`.)

## Staying safe on servers

Hypixel and Minemen ban "unfair advantage" mods. HUD info, keystrokes, CPS, toggle sprint,
zoom, and FPS boosts are fine. **Never** add anything that clicks, aims, moves, or reaches for you
(auto-clicker, aim assist, reach, velocity, ESP/wallhacks, etc.). Those get you banned. Check
each server's allowed-modifications rules before adding something new.
