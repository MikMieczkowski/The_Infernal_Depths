package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm._components.routine.Transition;
import com.mikm.entities.prefabLoader.PrefabInstantiator;
import com.mikm.utils.ExtraMathUtils;
import com.mikm._components.Copyable;
import com.mikm._components.SpriteComponent;
import com.mikm._components.Transform;
import com.mikm.entities.prefabLoader.Blackboard;
import com.mikm.rendering.sound.SoundEffects;
import com.mikm.rendering.screens.Application;

import java.util.function.Supplier;



public class ChargeAction extends Action {
    @Copyable private Float SPEED;
    @Copyable private int JUMP_HEIGHT;
    @Copyable private float BOUNCE_COEFFICIENT;
    @Copyable private float BOUNCE_FREQUENCY;
    @Copyable private boolean MULTIPLE_BOUNCES = false;
    @Copyable private boolean ACCELERATE = false;
    @Copyable private boolean DECELERATE = false;
    @Copyable private float PROPORTION_OF_TIME_DECELERATING = 1f;
    @Copyable private boolean UPDATES_PLAYER_ANGLE_ON_ENTER = true;

    @Copyable private boolean HAS_AFTERIMAGES = false;
    @Copyable private float TIME_BETWEEN_DASH_EFFECT_IMAGES = .05f;

    @Copyable private String MOVEMENT_DIRECTION_TYPE;
    @Copyable private float ANGLE_OF_APPROACH_FROM_PERPENDICULAR;

    @Copyable private String START_SOUND_EFFECT;
    @Copyable private String END_SOUND_EFFECT;

    private static final ComponentMapper<ChargeActionComponent> MAPPER = ComponentMapper.getFor(ChargeActionComponent.class);
    class ChargeActionComponent implements Component {
        public Supplier<Float> GET_ANGLE_OFFSET;

        public Supplier<Float> getAngleToPlayer;

        private float timeSinceLastDashEffectImage;
        private float bounceTimer;
        private float startHeight = 0;
        private float initialAngle = 0; // Captured angle for CurrentDir mode
    }

    @Override
    public Component createActionComponent() {
        return new ChargeActionComponent();
    }

    public ChargeAction(){}

    @Override
    public void postConfigRead(Entity entity) {
        super.postConfigRead(entity);


        Transform transform = Transform.MAPPER.get(entity);
        ChargeActionComponent data = MAPPER.get(entity);
        if (!transform.ENTITY_NAME.equals("player")) {
            data.getAngleToPlayer = () -> {
                Transform playerTransform = Application.getInstance().getPlayerTransform();
                return MathUtils.atan2(playerTransform.getCenteredY() - transform.getCenteredY(), playerTransform.getCenteredX() - transform.getCenteredX());
            };
        } else {
            data.getAngleToPlayer = () -> 0f;
        }

        if (MOVEMENT_DIRECTION_TYPE == null || MOVEMENT_DIRECTION_TYPE.equals("Player")) {
            data.GET_ANGLE_OFFSET = () -> 0f; //directly towards player
        } else if (MOVEMENT_DIRECTION_TYPE.equals("LeftOfPlayer")) {
            data.GET_ANGLE_OFFSET = () -> MathUtils.PI/2f - ANGLE_OF_APPROACH_FROM_PERPENDICULAR * MathUtils.degRad;
        } else if (MOVEMENT_DIRECTION_TYPE.equals("RightOfPlayer")) {
            data.GET_ANGLE_OFFSET = () -> -MathUtils.PI/2f + ANGLE_OF_APPROACH_FROM_PERPENDICULAR * MathUtils.degRad;
        } else if (MOVEMENT_DIRECTION_TYPE.equals("CurrentDir")) {
            data.GET_ANGLE_OFFSET = () -> MathUtils.atan2(transform.yVel, transform.xVel);
        } else {
            throw new RuntimeException("Undefined AcceleratedMove MOVEMENT_DIRECTION_TYPE " + MOVEMENT_DIRECTION_TYPE);
        }

        if (SPEED == null) {
            SPEED = 1f;
        }
    }

    @Override
    public void enter(Entity entity) {
        super.enter(entity);
        Transform transform = Transform.MAPPER.get(entity);
        ChargeActionComponent data = MAPPER.get(entity);
        data.startHeight = transform.height;
        data.bounceTimer = 0;
        // Capture initial angle for CurrentDir mode before zeroing velocities
        if (MOVEMENT_DIRECTION_TYPE != null && MOVEMENT_DIRECTION_TYPE.equals("CurrentDir")) {
            data.initialAngle = MathUtils.atan2(transform.yVel, transform.xVel);
        }
        transform.xVel = 0;
        transform.yVel = 0;
        SoundEffects.play(START_SOUND_EFFECT);
        if (UPDATES_PLAYER_ANGLE_ON_ENTER) {
            Blackboard.getInstance().bind("currentAngleToPlayer", entity, data.getAngleToPlayer.get());
        }
    }


    @Override
    public void update(Entity entity) {
        super.update(entity);

        Transform transform = Transform.MAPPER.get(entity);
        ChargeActionComponent data = MAPPER.get(entity);
        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);

        float maxTime = MAX_TIME == null ? 0 : MAX_TIME;

        float bounce;
        if (MULTIPLE_BOUNCES) {
            bounce = ExtraMathUtils.bounceLerp(data.bounceTimer, maxTime, JUMP_HEIGHT, BOUNCE_COEFFICIENT, BOUNCE_FREQUENCY);
        } else {
            bounce = ExtraMathUtils.sinLerp(data.bounceTimer, maxTime, JUMP_HEIGHT);
        }
        transform.height = data.startHeight + bounce;
        data.bounceTimer += Gdx.graphics.getDeltaTime();
        data.timeSinceLastDashEffectImage += Gdx.graphics.getDeltaTime();
        float speedMultiplier = 1;
        if (ACCELERATE) {
            speedMultiplier = routineListComponent.timeElapsedInCurrentAction/maxTime;
        }
        if (DECELERATE) {
            float timeSpentDecelerating = PROPORTION_OF_TIME_DECELERATING * maxTime;
            speedMultiplier = Math.max(0, 1 - (routineListComponent.timeElapsedInCurrentAction / timeSpentDecelerating));
        }
        float angle;
        if (MOVEMENT_DIRECTION_TYPE != null && MOVEMENT_DIRECTION_TYPE.equals("CurrentDir")) {
            angle = data.initialAngle;
        } else {
            angle = ((float) Blackboard.getInstance().getVar(entity, "currentAngleToPlayer")) + data.GET_ANGLE_OFFSET.get();
        }
        float globalSpeed = transform.SPEED;
        transform.xVel = MathUtils.cos(angle) * SPEED * globalSpeed * speedMultiplier;
        transform.yVel = MathUtils.sin(angle) * SPEED * globalSpeed * speedMultiplier;

        if (HAS_AFTERIMAGES && data.timeSinceLastDashEffectImage > TIME_BETWEEN_DASH_EFFECT_IMAGES) {
            data.timeSinceLastDashEffectImage -= TIME_BETWEEN_DASH_EFFECT_IMAGES;
            PrefabInstantiator.addAfterImage(entity);
        }
    }



    @Override
    public void onExit(Entity entity) {
        super.onExit(entity);
        ChargeActionComponent data = MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        SoundEffects.playLoud(END_SOUND_EFFECT);
        transform.height = data.startHeight;
        transform.xVel = 0;
        transform.yVel = 0;
    }

    public static ChargeAction simpleMoveTowardsAngle(float speed) {
        ChargeAction output = new ChargeAction();
        output.MOVEMENT_DIRECTION_TYPE = "CurrentDir";
        output.SPEED = speed;
        output.UPDATES_PLAYER_ANGLE_ON_ENTER = false;
        return output;
    }

    public static ChargeAction forParticle(float speed, float maxLifeTime, boolean shouldDecelerate, float proportionOfTimeDecelerating) {
        ChargeAction output = new ChargeAction();
        output.MOVEMENT_DIRECTION_TYPE = "CurrentDir";
        output.SPEED = speed;
        output.MAX_TIME = maxLifeTime;
        output.UPDATES_PLAYER_ANGLE_ON_ENTER = false;
        output.DECELERATE = shouldDecelerate;
        output.PROPORTION_OF_TIME_DECELERATING = proportionOfTimeDecelerating;
        return output;
    }
}
