package com.nexusnode.launcher.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Mark if instances of the class are immutable.
 *
 */
@Target(ElementType.TYPE)
public @interface Immutable {
}
