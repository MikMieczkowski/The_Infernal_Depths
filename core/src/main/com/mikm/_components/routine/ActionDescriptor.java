package com.mikm._components.routine;

import java.util.Map;

public class ActionDescriptor {
    String actionName;            // "move", "wait", "shoot", etc.
    Map<String, Object> configVars; // positions, durations, tags, etc.
}
