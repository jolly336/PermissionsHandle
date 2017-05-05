package com.nelson.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Grant permission unsuccessfully.
 * Created by Nelson on 17/5/5.
 */

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@Documented
public @interface PermissionDenied {

    int value();
}
