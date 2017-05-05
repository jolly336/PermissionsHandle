package com.nelson.compiler;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Created by Nelson on 17/5/5.
 */

final public class ClassValidator {

    static boolean isPublic(Element element) {
        return element.getModifiers().contains(Modifier.PUBLIC);
    }

    static boolean isPrivate(Element element) {
        return element.getModifiers().contains(Modifier.PRIVATE);
    }

    static boolean isAbstract(Element element) {
        return element.getModifiers().contains(Modifier.ABSTRACT);
    }

    static String getClassName(TypeElement typeElement, String packageName) {
        int len = packageName.length() + 1;
        return typeElement.getQualifiedName().toString().substring(len).replace(".", "$");
    }

}
