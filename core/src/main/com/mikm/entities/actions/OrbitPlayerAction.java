package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm._components.Transform;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;

public class OrbitPlayerAction extends Action {

    public static final ComponentMapper<OrbitActionComponent> MAPPER = ComponentMapper.getFor(OrbitActionComponent.class);
    class OrbitActionComponent implements Component {
        float weaponRotation;
        float angleOffset;
        int orbitDistance = 15;
        boolean mouseIsLeftOfPlayer = false;
        boolean shouldSwingRight = true;
        float angleToMouse;
        private float weaponAngle;
    }
    @Override
    public Component createActionComponent() {
        return new OrbitActionComponent();
    }

    @Override
    public void update(Entity entity) {
        super.update(entity);

        Transform transform = Transform.MAPPER.get(entity);
        Vector2 playerPositionOrbitedAroundMouse = getPlayerPositionOrbitedAroundMouse(entity);
        transform.x = playerPositionOrbitedAroundMouse.x;
        transform.y = playerPositionOrbitedAroundMouse.y;

        setWeaponZIndex(entity);
        transform.rotation = MAPPER.get(entity).weaponRotation * MathUtils.radDeg;

    }

    private void setWeaponZIndex(Entity entity) {
        Transform transform = Transform.MAPPER.get(entity);
        OrbitActionComponent data = MAPPER.get(entity);

        if (data.weaponAngle > MathUtils.PI) {
            transform.Z_ORDER=1;
        } else {
            transform.Z_ORDER=0;
        }
    }

    private Vector2 getPlayerPositionOrbitedAroundMouse(Entity entity) {
        float x, y;
        OrbitActionComponent data = MAPPER.get(entity);

        data.angleToMouse = GameInput.getAttackingAngle();
        data.mouseIsLeftOfPlayer = -MathUtils.HALF_PI < data.angleToMouse && data.angleToMouse < MathUtils.HALF_PI;

        data.weaponAngle = data.angleToMouse - MathUtils.HALF_PI;
        data.weaponRotation = data.angleToMouse + MathUtils.PI/2;
        final float weaponArcRotationProportion = (3+MathUtils.PI/2)/3;
        if (data.mouseIsLeftOfPlayer) {
            data.weaponAngle += MathUtils.PI;
            data.weaponRotation -= MathUtils.PI/2;
        }
        if (data.mouseIsLeftOfPlayer) {
            data.weaponRotation -= data.angleOffset*weaponArcRotationProportion;
            data.weaponAngle -= data.angleOffset;
        } else {
            data.weaponRotation += data.angleOffset*weaponArcRotationProportion;
            data.weaponAngle += data.angleOffset;
        }

        Transform playerTransform = Application.getInstance().getPlayerTransform();

        x = playerTransform.getCenteredX() + data.orbitDistance * MathUtils.cos(data.weaponAngle) - playerTransform.getFullBounds().width/2;
        y = playerTransform.getCenteredY() + data.orbitDistance * MathUtils.sin(data.weaponAngle) - playerTransform.getFullBounds().height/2 - 6;
        return new Vector2(x, y);
    }
}
