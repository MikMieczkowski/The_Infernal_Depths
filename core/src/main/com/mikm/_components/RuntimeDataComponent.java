package com.mikm._components;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)   // mark classes
//Marks that a class should not be copied because it consists of all runtime data. The default behaviour is that all fields are copied in the prefab instantiator.
public @interface RuntimeDataComponent {
}