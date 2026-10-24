package com.nova.emote;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;

/**
 * The emotes. Each one poses the player's limbs at time t (ticks since it started), and some
 * also move the whole body.
 * <p>
 * Limb angles are in radians, like Minecraft's model. For arms and legs: X negative = swing forward,
 * Z positive moves the right arm/leg outward (negative for the left one), Y turns it.
 * Whole-body moves happen in blocks with y pointing up and the feet at y = 0.
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
    FLOSS("Floss", 400, true) {
        // like Fortnite's: both arms swing to the same side, one in front of the body and one
        // behind, swapping every swing, while the hips swing the other way
        private float phase(float t) {
            return t * 0.6f;
        }

        @Override
        void pose(ModelBiped m, float t) {
            float p = phase(t);
            float side = MathHelper.sin(p);
            float frontBack = MathHelper.clamp_float(MathHelper.cos(p) * 3f, -1f, 1f) * 0.42f;
            m.bipedRightArm.rotateAngleZ = side * 0.85f;
            m.bipedLeftArm.rotateAngleZ = side * 0.85f;
            m.bipedRightArm.rotateAngleX = -frontBack;
            m.bipedLeftArm.rotateAngleX = frontBack;
            m.bipedRightLeg.rotateAngleZ = side * 0.22f;
            m.bipedLeftLeg.rotateAngleZ = side * 0.22f;
            m.bipedHead.rotateAngleZ = -side * 0.08f;
        }

        @Override
        void body(float t, float blend) {
            GlStateManager.translate(-MathHelper.sin(phase(t)) * 0.12f * blend, 0f, 0f);
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
    },
    TWERK("Twerk", 400, true) {
        private static final float BEND = 42f;

        @Override
        void pose(ModelBiped m, float t) {
            // the body is bent forward (see body()), which tips the legs back; swing them forward
            // the same amount so they stay upright, hands on the knees, head looking ahead
            float legs = (float) -Math.toRadians(BEND);
            m.bipedRightLeg.rotateAngleX = legs;
            m.bipedLeftLeg.rotateAngleX = legs;
            m.bipedRightLeg.rotateAngleZ = 0.12f;
            m.bipedLeftLeg.rotateAngleZ = -0.12f;
            m.bipedRightArm.rotateAngleX = -0.35f;
            m.bipedLeftArm.rotateAngleX = -0.35f;
            m.bipedRightArm.rotateAngleZ = -0.1f;
            m.bipedLeftArm.rotateAngleZ = 0.1f;
            m.bipedHead.rotateAngleX = -0.65f;
        }

        @Override
        void body(float t, float blend) {
            float bounce = Math.abs(MathHelper.sin(t * 0.9f));
            GlStateManager.translate(0f, bounce * 0.06f * blend, 0f);
            GlStateManager.translate(0f, 0.75f, 0f);                  // bend at the hips
            GlStateManager.rotate(-BEND * blend, 1f, 0f, 0f);
            GlStateManager.rotate(MathHelper.sin(t * 1.8f) * 6f * blend, 0f, 0f, 1f); // hip shake
            GlStateManager.translate(0f, -0.75f, 0f);
        }
    },
    BREAKDANCE("Breakdance", 400, true) {
        @Override
        void pose(ModelBiped m, float t) {
            // legs in a wide V, arms out for balance (upside down: see body())
            float kick = MathHelper.sin(t * 0.4f) * 0.15f;
            m.bipedRightLeg.rotateAngleZ = 0.7f + kick;
            m.bipedLeftLeg.rotateAngleZ = -0.7f + kick;
            m.bipedRightLeg.rotateAngleX = 0f;
            m.bipedLeftLeg.rotateAngleX = 0f;
            m.bipedRightArm.rotateAngleZ = 0.5f;
            m.bipedLeftArm.rotateAngleZ = -0.5f;
            m.bipedRightArm.rotateAngleX = 0f;
            m.bipedLeftArm.rotateAngleX = 0f;
            m.bipedHead.rotateAngleX = 0f;
            m.bipedHead.rotateAngleY = 0f;
        }

        @Override
        void body(float t, float blend) {
            // a headspin: flip upside down around the middle of the body, then spin
            GlStateManager.rotate(t * 22f, 0f, 1f, 0f);
            GlStateManager.translate(0f, 0.95f, 0f);
            GlStateManager.rotate(180f * blend, 1f, 0f, 0f);
            GlStateManager.translate(0f, -0.95f + 0.05f * blend, 0f);
        }
    },
    GROOVE("Groove", 400, true) {
        // a Fortnite-style default dance: arms pump up and down in turn, feet step side to side
        @Override
        void pose(ModelBiped m, float t) {
            float beat = MathHelper.sin(t * 0.55f);
            m.bipedRightArm.rotateAngleX = -1.3f + beat * 0.8f;
            m.bipedLeftArm.rotateAngleX = -1.3f - beat * 0.8f;
            m.bipedRightArm.rotateAngleZ = 0.25f;
            m.bipedLeftArm.rotateAngleZ = -0.25f;
            m.bipedRightLeg.rotateAngleZ = Math.max(0f, beat) * 0.35f;
            m.bipedLeftLeg.rotateAngleZ = Math.min(0f, beat) * 0.35f;
            m.bipedHead.rotateAngleZ = beat * 0.12f;
        }

        @Override
        void body(float t, float blend) {
            GlStateManager.translate(0f, Math.abs(MathHelper.sin(t * 0.55f)) * 0.06f * blend, 0f);
        }
    },
    PUMP_IT("Pump It", 400, true) {
        // fists pumping forward, legs kicking out to the sides in turn
        @Override
        void pose(ModelBiped m, float t) {
            float beat = MathHelper.sin(t * 0.7f);
            m.bipedRightArm.rotateAngleX = -0.9f - beat * 0.7f;
            m.bipedLeftArm.rotateAngleX = -0.9f + beat * 0.7f;
            m.bipedRightArm.rotateAngleY = -0.3f;
            m.bipedLeftArm.rotateAngleY = 0.3f;
            m.bipedRightLeg.rotateAngleZ = Math.max(0f, beat) * 0.55f;
            m.bipedLeftLeg.rotateAngleZ = Math.min(0f, beat) * 0.55f;
            m.bipedRightLeg.rotateAngleX = -Math.max(0f, beat) * 0.3f;
            m.bipedLeftLeg.rotateAngleX = Math.min(0f, beat) * 0.3f;
        }

        @Override
        void body(float t, float blend) {
            GlStateManager.translate(0f, Math.abs(MathHelper.cos(t * 0.7f)) * 0.08f * blend, 0f);
        }
    },
    L_DANCE("L Dance", 400, true) {
        // a hand making an "L" on the forehead while hopping and kicking each leg out
        @Override
        void pose(ModelBiped m, float t) {
            float beat = MathHelper.sin(t * 0.65f);
            m.bipedRightArm.rotateAngleX = -2.55f;
            m.bipedRightArm.rotateAngleY = -0.35f;
            m.bipedRightArm.rotateAngleZ = 0.4f;
            m.bipedLeftArm.rotateAngleZ = -0.35f;
            m.bipedRightLeg.rotateAngleZ = Math.max(0f, beat) * 0.75f;
            m.bipedLeftLeg.rotateAngleZ = Math.min(0f, beat) * 0.75f;
            m.bipedHead.rotateAngleX = -0.15f;
        }

        @Override
        void body(float t, float blend) {
            GlStateManager.translate(0f, Math.abs(MathHelper.sin(t * 0.65f)) * 0.12f * blend, 0f);
        }
    },
    SPIN("Spin", 60, false) {
        @Override
        void pose(ModelBiped m, float t) {
            m.bipedRightArm.rotateAngleZ = 1.45f;
            m.bipedLeftArm.rotateAngleZ = -1.45f;
            m.bipedRightArm.rotateAngleX = 0f;
            m.bipedLeftArm.rotateAngleX = 0f;
            m.bipedHead.rotateAngleX = -0.4f;
        }

        @Override
        void body(float t, float blend) {
            GlStateManager.rotate(t * 24f * blend, 0f, 1f, 0f);
            GlStateManager.translate(0f, 0.06f * blend, 0f);
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

    /** Moves the whole body. blend goes from 0 to 1 as the emote starts (and back as it ends). */
    void body(float t, float blend) {
    }
}
