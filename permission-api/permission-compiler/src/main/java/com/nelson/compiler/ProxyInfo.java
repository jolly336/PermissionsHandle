package com.nelson.compiler;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Permission proxy class
 * Created by Nelson on 17/5/5.
 */

public class ProxyInfo {

    public static final String PROXY = "PermissionProxy";

    private String mPackageName;
    private String mProxyClassName;
    private TypeElement mTypeElement;

    final Map<Integer, String> mGrantMethodMap = new HashMap<>();
    final Map<Integer, String> mDeniedMethodMap = new HashMap<>();
    final Map<Integer, String> mRationaleMethodMap = new HashMap<>();

    public ProxyInfo(Elements elementUtils, TypeElement classElement) {
        PackageElement packageElement = elementUtils.getPackageOf(classElement);
        String packageName = packageElement.getQualifiedName().toString();
        String className = ClassValidator.getClassName(classElement, packageName);
        this.mPackageName = packageName;
        this.mProxyClassName = className + "$$" + PROXY;
    }

    String getProxyClassFullName() {
        return mPackageName + "." + mProxyClassName;
    }

    String brewJavaCode() {
        StringBuilder builder = new StringBuilder();
        builder.append("// Generated code.Do not modify!\n");
        builder.append("package ").append(mPackageName).append(";\n\n");
        builder.append("import com.nelson.api.*;\n");
        builder.append("\n");

        builder.append("public class ").append(mProxyClassName).append(" implements " + ProxyInfo.PROXY + "<" + mTypeElement.getSimpleName() + ">");
        builder.append(" {\n");

        generateMethods(builder);
        builder.append("\n");

        builder.append("}\n");
        return builder.toString();
    }

    private void generateMethods(StringBuilder builder) {
        generateGrantMethod(builder);
        generateDeniedMethod(builder);
        generateRationaleMethod(builder);
    }

    private void generateGrantMethod(StringBuilder builder) {
        builder.append("@Override\n");
        builder.append("public void grant(" + mTypeElement.getSimpleName() + " source, int requestCode) {\n");
        builder.append("switch(requestCode) {");
        for (int code : mGrantMethodMap.keySet()) {
            builder.append("case " + code + ":");
            builder.append("source." + mGrantMethodMap.get(code) + "();");
            builder.append("break;");
        }

        builder.append("}");
        builder.append(" }\n");
    }

    private void generateDeniedMethod(StringBuilder builder) {
        builder.append("@Override\n");
        builder.append("public void denied(" + mTypeElement.getSimpleName() + " source, int requestCode) {\n");
        builder.append("switch(requestCode) {");
        for (int code : mDeniedMethodMap.keySet()) {
            builder.append("case " + code + ":");
            builder.append("source." + mDeniedMethodMap.get(code) + "();");
            builder.append("break;");
        }

        builder.append("}");
        builder.append(" }\n");
    }

    private void generateRationaleMethod(StringBuilder builder) {
        builder.append("@Override\n");
        builder.append("public void rationale(" + mTypeElement.getSimpleName() + " source, int requestCode) {\n");
        builder.append("switch(requestCode) {");
        for (int code : mRationaleMethodMap.keySet()) {
            builder.append("case " + code + ":");
            builder.append("source." + mRationaleMethodMap.get(code) + "();");
            builder.append("break;");
        }

        builder.append("}");
        builder.append(" }\n");

        builder.append("@Override\n");
        builder.append("public boolean needShowRationale(int requestCode) {\n");
        builder.append("switch(requestCode) {");
        for (int code : mRationaleMethodMap.keySet()) {
            builder.append("case " + code + ":");
            builder.append("return true;");
        }

        builder.append("}\n");
        builder.append("return false;");

        builder.append(" }\n");
    }

    void setTypeElement(TypeElement typeElement) {
        this.mTypeElement = typeElement;
    }

    TypeElement getTypeElement() {
        return mTypeElement;
    }

}
