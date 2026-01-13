package com.mikm._components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.mikm.rendering.screens.Application;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class TriggerComponent implements Component {
    public static final ComponentMapper<TriggerComponent> MAPPER = ComponentMapper.getFor(TriggerComponent.class);

    public int diameter = Application.TILE_WIDTH;

    @CopyReference public Map<Event, TriggerAction> eventToAction = new HashMap<>();

    //for onEnter
    public boolean playerInsideLastFrame;
    //for onStayAndInputJustPressed. From GameInput.inputActions
    public String inputAction;
    //for a single event (door enter)
    public int goToScreenTriggerActionScreen;

    public TriggerComponent() {

    }
    public TriggerComponent(int diameter, Object... pairs) {
        this.diameter = diameter;
        this.eventToAction = mapOf(pairs);
    }


    public TriggerComponent(int diameter, String inputAction, Object... pairs) {
        this.diameter = diameter;
        this.inputAction = inputAction;
        this.eventToAction = mapOf(pairs);
    }

    private static Map<Event, TriggerAction> mapOf(Object... pairs) {
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "mapOf requires an even number of arguments (Event, TriggerAction pairs)"
            );
        }

        Map<Event, TriggerAction> m = new EnumMap<>(Event.class);

        for (int i = 0; i < pairs.length; i += 2) {
            Object k = pairs[i];
            Object v = pairs[i + 1];

            if (!(k instanceof Event)) {
                throw new IllegalArgumentException(
                        "Argument " + i + " must be an Event, got " +
                                (k == null ? "null" : k.getClass().getSimpleName())
                );
            }

            if (!(v instanceof TriggerAction)) {
                throw new IllegalArgumentException(
                        "Argument " + (i + 1) + " must be a TriggerAction, got " +
                                (v == null ? "null" : v.getClass().getSimpleName())
                );
            }

            m.put((Event) k, (TriggerAction) v);
        }

        return m;
    }

}

