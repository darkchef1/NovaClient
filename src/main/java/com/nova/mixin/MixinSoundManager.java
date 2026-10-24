package com.nova.mixin;

import net.minecraft.client.audio.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fixes "no sound" on fast startups (a 1.8.9 Forge bug).
 * <p>
 * The sound system starts on a background thread. Forge reloads resources right after mods load,
 * which starts the sound system again. When the game loads quickly, the first start hasn't
 * finished yet, two OpenAL contexts collide ("Only one OpenAL context may be instantiated")
 * and sound stays broken. Here a second start is skipped while the first is still starting.
 */
@Mixin(SoundManager.class)
public abstract class MixinSoundManager {
    /** A start that hasn't finished after this long is treated as failed, so a new one may try. */
    private static final long START_TIMEOUT_MS = 20_000;

    @Shadow private boolean loaded;

    /** When the current start began, or 0 if no start is in progress. */
    private long nova$startingSince;

    @Inject(method = "loadSoundSystem", at = @At("HEAD"), cancellable = true)
    private void nova$skipDuplicateStart(CallbackInfo ci) {
        long now = System.currentTimeMillis();
        if (!loaded && nova$startingSince != 0 && now - nova$startingSince < START_TIMEOUT_MS) {
            ci.cancel();
            return;
        }
        nova$startingSince = now;
    }

    /** Unloading a sound system that finished starting means the next start is a real restart. */
    @Inject(method = "unloadSoundSystem", at = @At("HEAD"))
    private void nova$allowRestart(CallbackInfo ci) {
        if (loaded) nova$startingSince = 0;
    }
}
