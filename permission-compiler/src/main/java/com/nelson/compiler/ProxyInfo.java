package com.nelson.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;
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

    JavaFile brewJavaCode() {
        //---manual generate---
       /* StringBuilder builder = new StringBuilder();
        builder.append("// Generated code.Do not modify!\n");
        builder.append("package ").append(mPackageName).append(";\n\n");
        builder.append("import com.nelson.api.*;\n");
        builder.append("\n");

        builder.append("public class ").append(mProxyClassName).append(" implements " + ProxyInfo.PROXY + "<" + mTypeElement.getSimpleName() + ">");
        builder.append(" {\n");

        generateMethods(builder);
        builder.append("\n");

        builder.append("}\n");
        return builder.toString();*/

        //---javapoet---
        ClassName interfaceName = ClassName.get("com.nelson.api", PROXY);
        ClassName superinterface = ClassName.bestGuess(mTypeElement.getQualifiedName().toString());

        TypeSpec.Builder typeSpec = TypeSpec.classBuilder(mProxyClassName)
                .addModifiers(Modifier.PUBLIC)
                // 添加接口，ParameterizedTypeName参数1是接口，参数2是接口的泛型
                .addSuperinterface(ParameterizedTypeName.get(interfaceName, superinterface));


        MethodSpec.Builder needShowRationaleMethod = MethodSpec.methodBuilder("needShowRationale")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.BOOLEAN)
                .addParameter(TypeName.INT, "requestCode");

        needShowRationaleMethod.beginControlFlow("switch(requestCode)");
        for (int code : mRationaleMethodMap.keySet()) {
            needShowRationaleMethod.addCode("case $L:\n", code)
                    .addStatement("$> return true$<");
        }
        needShowRationaleMethod.endControlFlow();

        needShowRationaleMethod.addStatement("return false");

        TypeSpec proxyClass = typeSpec.addMethod(generatMethod("grant", mGrantMethodMap).build())
                .addMethod(generatMethod("denied", mDeniedMethodMap).build())
                .addMethod(generatMethod("rationale", mRationaleMethodMap).build())
                .addMethod(needShowRationaleMethod.build())
                .build();

        return JavaFile.builder(mPackageName, proxyClass)
                .addFileComment("Generated code from PermissionHandle processor. Do not modify!")
                .build();
    }

    //-----Javapoet java library-------
    private MethodSpec.Builder generatMethod(String methodName, Map<Integer, String> map) {

        ClassName parameterName = ClassName.bestGuess(mTypeElement.getQualifiedName().toString());

        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.VOID)
                .addParameter(parameterName, "source")
                .addParameter(TypeName.INT, "requestCode");

        builder.beginControlFlow("switch(requestCode)");
        for (int code : map.keySet()) {
            builder.addCode("case $L:\n", code)
                    .addStatement("$>source.$L" + "()", map.get(code))
                    .addStatement("break$<");
        }
        builder.endControlFlow();
        return builder;
    }

    //=====================================================

    //-----Manually generate java code-----
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
