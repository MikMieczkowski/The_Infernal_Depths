Iterator<Map.Entry<String, Object>> it = transitionData.ON_CONDITION.entrySet().iterator();
        Map.Entry<String, Object> onlyEntry = it.next();
        if (it.hasNext()) {
            throw new RuntimeException("Only one child allowed in ROUTINES.TRANSITIONS.ON_CONDITION in " + fileName);
        }



        String orAndOrCondition = onlyEntry.getKey();
        if (orAndOrCondition.equals("OR") || orAndOrCondition.equals("AND")) {
            if (!(onlyEntry.getValue() instanceof Map)) {
                throw new RuntimeException("Should be a Map under AND: in ON_CONDITION in " + fileName);
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> subConditions = (Map<String, Object>) onlyEntry.getValue();
            Predicate<Entity> output = e -> false;
            for (Map.Entry<String, Object> subCondition : subConditions.entrySet()) {
                Predicate<Entity> subConditionPredicate = loadSimpleCondition(subCondition.getKey(), subCondition.getValue());
                if (orAndOrCondition.equals("AND")) {
                    output = output.and(subConditionPredicate);
                } else {
                    output = output.or(subConditionPredicate);
                }
            }
            return output;
        } else {
            return loadSimpleCondition(orAndOrCondition, onlyEntry.getValue());
        }


    @SuppressWarnings("unchecked")
    private static Predicate<Entity> loadSimpleCondition(String type, Object conditionData) {
        switch (type) {
            case "IN_RADIUS_OF_PLAYER":
                if (!(conditionData instanceof Number)) {
                    throw new IllegalArgumentException("Expected a number, got: " + conditionData.getClass() + " in routine transition condition type " + type + " in " + fileName);
                }
                float detectionPlayerRadius = ((Number) conditionData).floatValue();
                return e -> Intersector.overlaps(new Circle(e.x, e.y, detectionPlayerRadius), Application.player.getHitbox());
            case "BEHAVIOUR_READY":
                if (!(conditionData instanceof Map)) {
                    throw new IllegalArgumentException(
                            "Expected a map, got: " + conditionData.getClass() + " in routine transition condition type " + type + " in " + fileName
                    );
                }
                Map<String, Object> behaviourReadyData = (Map<String, Object>) conditionData;

                return e -> {
                    String behaviourName = (String) behaviourReadyData.get("BEHAVIOUR");
                    float timeSince = ((Number) behaviourReadyData.get("TIME_SINCE_IS_GREATER_THAN")).floatValue();

                    return e.routineHandler.getTimeSinceBehaviour(nameToBehaviour.get(behaviourName)) > timeSince;
                };
            case "FINISHED":
                if (!(conditionData instanceof String)) {
                    throw new IllegalArgumentException("Expected a string, got: " + conditionData.getClass() + " in routine transition condition type " + type + " in " + fileName);
                }
                String behaviourName = (String) conditionData;
                return e -> e.routineHandler.getBehaviourJustCompleted().equals(behaviourName);
            case "RANDOM":
                float f = ((Number) conditionData).floatValue();
                return e -> RandomUtils.getPercentage((int) (f * 100));
            case "BUTTON_PRESSED":
                return e -> GameInput.isActionPressed((String)conditionData);
            case "BUTTON_JUST_PRESSED":
                return e-> GameInput.isActionJustPressed((String)conditionData);
            case "ENDS_WITHIN":
                //Must be on Roll action for now
                return e -> {
                    Action a = nameToBehaviour.get(e.routineHandler.getBehaviourJustCompleted());
                    if (!(a instanceof RollAction)) {
                        throw new IllegalArgumentException("ENDS_WITHIN must be on RollAction, got: " + conditionData.getClass() + " in routine transition condition type " + type + " in " + fileName);
                    }
                    return ((RollAction)a).endsWithin(((Number)conditionData).floatValue());
                };
            case "IS_MOVING":
                return e -> {
                    boolean b = (boolean) conditionData;
                    if (b) {
                        return !(e.xVel == 0 && e.yVel == 0);
                    } else {
                        return e.xVel == 0 && e.yVel == 0;
                    }
                    //b ^ (e.xVel == 0 && e.yVel == 0)
                };
            case "HAS_BEEN_STILL_FOR":
                //Must be on AcceleratedMove action for now
                return e -> {
                    return false;
//                    Action a = e.routineHandler.currentRoutine.cycle.currentAction;
//                    return ((AcceleratedMoveAction)a).hasBeenStillFor(((Number)conditionData).floatValue());
                };
            case "CAN_FALL":
                //must be used on player
                return e -> {
                    if (!(e instanceof Player)) {
                        throw new IllegalArgumentException("CAN_FALL must be on Player, got: " + conditionData.getClass() + " in routine transition condition type " + type + " in " + fileName);
                    }
                    Player p = (Player) e;
                    return p.canFall;
                };
            case "BOW_RELEASED":
                //must be used on player
                return e -> {
                    if (!(e instanceof Player)) {
                        throw new IllegalArgumentException("BOW_RELEASED must be on Player, got: " + conditionData.getClass() + " in routine transition condition type " + type + " in " + fileName);
                    }
                    Player p = (Player) e;
                    return p.currentHeldItem instanceof Bow && ((Bow)p.currentHeldItem).isReleased();
                };
            default:
                throw new RuntimeException("Unimplemented routine transition condition type " + type + " in " + fileName);
        }
    }


//TEXTURE MASKING

private TextureRegion dark = Assets.getInstance().getTextureRegion("dark", 583, 426);
private static SpriteBatch batch = new SpriteBatch();
        // 1. Draw mask into framebuffer
        FrameBuffer maskBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, dark.getRegionWidth(), dark.getRegionHeight(), false);

//TODO Does drawing stuff over other stuff cause lag?
//            Application.batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ZERO);
//            Application.batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_SRC_ALPHA);
//            Application.batch.draw(Assets.light, Camera.x - Assets.light.getRegionWidth()/2f, Camera.y - Assets.light.getRegionHeight()/2f);
//            Application.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA); // Normal rendering

//
//            Application.batch.end();
//
//
//            maskBuffer.begin();
//
//            Gdx.gl.glClearColor(.2f, .2f, .2f, 1f); // black = no light
//            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//
//            Application.batch.begin();
//            Application.batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_COLOR);
//            Application.batch.draw(Assets.light, Camera.x - dark.getRegionWidth()/2 + Assets.light.getRegionWidth()/2, Camera.y- dark.getRegionHeight()/2 + Assets.light.getRegionHeight()/2, dark.getRegionWidth(), dark.getRegionHeight());
//            Application.batch.draw(Assets.light, 250, 250, dark.getRegionWidth(), dark.getRegionHeight());
//            Application.batch.draw(Assets.light, 0, 0, dark.getRegionWidth(), dark.getRegionHeight());
//            Application.batch.end();
//
//            maskBuffer.end();
//
//// 2. Multiply mask brightness into your content
//            Texture maskTex = maskBuffer.getColorBufferTexture();
//            TextureRegion textureRegion = new TextureRegion(maskTex);
//            textureRegion.flip(false, true);
//
//            Application.batch.begin();
//
//// Multiply destination (maskBuffer) * source (contentTexture)
//            // Multiply colors, but keep content’s alpha
//            Application.batch.setBlendFunctionSeparate(
//                    GL20.GL_DST_COLOR, GL20.GL_ZERO,   // RGB multiply
//                    GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA // preserve alpha blending
//            );
//            Application.batch.draw(textureRegion, Camera.x - dark.getRegionWidth()/2f, Camera.y - dark.getRegionHeight()/2f);
//
//
//
//// Reset blending
//            Application.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);