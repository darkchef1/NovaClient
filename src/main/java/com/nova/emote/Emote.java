package com.nova.emote;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.util.MathHelper;

/**
 * The emotes. Each one poses the player model at time t (in ticks since it started).
 * <p>
 * Angles are in radians, like Minecraft's model. For arms: X negative = raise forward,
 * Z positive moves the right arm outward (negative for the left arm), Y turns it inward or outward.
 */
public enum Emote {
    WAVE("Wave", 60, false) {
        @Override
        void pose(ModelBiped m, float t) {
            m.bipedRightArm.rotateAngleX = -2.7f;
            m.bipedRightArm.rotateAngleZ = 0.35f + MathHelper.sin(t * 0.6f) * 0.45f;
            m.bipedHead.rotateAngleZ = -0.12f;
        }
    },
    DAB("Dab", 40, false) {
        @Override
        void pose(ModelBiped m, float t) {
            m.bipedRightArm.rotateAngleX = -0.4f;
            m.bipedRightArm.rotateAngleZ = 2.25f;
            m.bipedLeftArm.rotateAngleX = -1.7f;
            m.bipedLeftArm.rotateAngleY = 0.9f;
            m.bipedLeftArm.rotateAngleZ = 0.4f;
            m.bipedHead.rotateAngleX = 0.55f;
            m.bipedHead.rotateAngleY = 0.6f;
        }
    },
    FLOSS("Floss", 200, true) {
        @Override
        void pose(ModelBiped m, float t) {
            float swing = MathHelper.sin(t * 0.5f);
            float frontBack = MathHelper.cos(t * 0.5f) > 0 ? 0.35f : -0.35f;
            m.bipedRightArm.rotateAngleZ = 0.15f + swing * 0.55f;
            m.bipedLeftArm.rotateAngleZ = -0.15f + swing * 0.55f;
            m.bipedRightArm.rotateAngleX = frontBack;
            m.bipedLeftArm.rotateAngleX = -frontBack;
            m.bipedRightLeg.rotateAngleZ = -swing * 0.18f;
            m.bipedLeftLeg.rotateAngleZ = -swing * 0.18f;
            m.bipedBody.rotateAngleZ = -swing * 0.08f;
            m.bipedHead.rotateAngleZ = swing * 0.1f;
        }
    },
    CLAP("Clap", 80, false) {
        @Override
        void pose(ModelBiped m, float t) {
            float open = Math.abs(MathHelper.sin(t * 0.55f));
            m.bipedRightArm.rotateAngleX = -1.25f;
            m.bipedLeftArm.rotateAngleX = -1.25f;
            // hands meet in front (small inward turn), then open outward
            m.bipedRightArm.rotateAngleY = -0.08f + open * 0.5f;
            m.bipedLeftArm.rotateAngleY = 0.08f - open * 0.5f;
            m.bipedHead.rotateAngleX = 0.1f;
        }
    },
    SALUTE("Salute", 50, false) {
        @Override
        void pose(ModelBiped m, float t) {
            m.bipedRightArm.rotateAngleX = -2.4f;
            m.bipedRightArm.rotateAngleY = -0.55f;
            m.bipedRightArm.rotateAngleZ = 0.65f;
            m.bipedHead.rotateAngleX = -0.1f;
        }
    },
    T_POSE("T-Pose", 80, false) {
        @Override
        void pose(ModelBiped m, float t) {
            m.bipedRightArm.rotateAngleX = 0f;
            m.bipedLeftArm.rotateAngleX = 0f;
            m.bipedRightArm.rotateAngleZ = (float) (Math.PI / 2);
            m.bipedLeftArm.rotateAngleZ = (float) (-Math.PI / 2);
            m.bipedRightLeg.rotateAngleX = 0f;
            m.bipedLeftLeg.rotateAngleX = 0f;
            m.bipedHead.rotateAngleX = 0f;
            m.bipedHead.rotateAngleY = 0f;
        }
    },
    FACEPALM("Facepalm", 50, false) {
        @Override
        void pose(ModelBiped m, float t) {
            m.bipedRightArm.rotateAngleX = -2.0f;
            m.bipedRightArm.rotateAngleY = -0.5f;
            m.bipedHead.rotateAngleX = 0.35f;
            m.bipedHead.rotateAngleY = MathHelper.sin(t * 0.35f) * 0.12f;
        }
    },
    CHEER("Cheer", 80, false) {
        @Override
        void pose(ModelBiped m, float t) {
            float wave = MathHelper.sin(t * 0.7f);
            // arms up and spread in a V, swaying
            m.bipedRightArm.rotateAngleX = -0.3f;
            m.bipedLeftArm.rotateAngleX = -0.3f;
            m.bipedRightArm.rotateAngleZ = 2.5f + wave * 0.2f;
            m.bipedLeftArm.rotateAngleZ = -2.5f + wave * 0.2f;
            m.bipedHead.rotateAngleX = -0.35f;
        }
    };

    public final String displayName;
    /** How long it plays, in ticks (20 per second). Looping emotes repeat until you move. */
    public final int duration;
    public final boolean loops;

    Emote(String displayName, int duration, boolean loops) {
        this.displayName = displayName;
        this.duration = duration;
        this.loops = loops;
    }

    /** Sets the emote's angles on the model (on top of whatever pose it had). */
    abstract void pose(ModelBiped model, float t);
}
