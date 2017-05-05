package com.nelson.compiler;

import com.nelson.annotation.PermissionDenied;
import com.nelson.annotation.PermissionGrant;
import com.nelson.annotation.ShowRequestPermissionRationale;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.FileObject;

/**
 * Annotation processor
 * Created by Nelson on 17/5/5.
 */

public class PermissionProcessor extends AbstractProcessor {

    private Messager mMessager;
    private Elements mElementUtils;
    // 保存生成的代理类(.Java源码)，key为类全路径，value为生成的代理类信息
    private final Map<String, ProxyInfo> mProxyMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        mElementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(PermissionGrant.class.getCanonicalName());
        supportTypes.add(PermissionDenied.class.getCanonicalName());
        supportTypes.add(ShowRequestPermissionRationale.class.getCanonicalName());
        return supportTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        mProxyMap.clear();
        mMessager.printMessage(Diagnostic.Kind.NOTE, "process begin...");
        if (!processAnnotations(roundEnv, PermissionGrant.class)) {
            return false;
        }

        if (!processAnnotations(roundEnv, PermissionDenied.class)) {
            return false;
        }

        if (!processAnnotations(roundEnv, ShowRequestPermissionRationale.class)) {
            return false;
        }

        for (Map.Entry<String, ProxyInfo> entry : mProxyMap.entrySet()) {
            String key = entry.getKey();
            ProxyInfo proxyInfo = entry.getValue();
            try {
                FileObject javaFileObj = processingEnv.getFiler().createSourceFile(
                        proxyInfo.getProxyClassFullName(),
                        proxyInfo.getTypeElement());
                Writer writer = javaFileObj.openWriter();
                writer.write(proxyInfo.brewJavaCode());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                error(proxyInfo.getTypeElement(),
                        "Uable to write injector for type %s : %s",
                        proxyInfo.getTypeElement(), e.getMessage());
            }
        }


        return true;
    }

    private boolean processAnnotations(RoundEnvironment roundEnv, Class<? extends Annotation> clazz) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(clazz)) {
            if (!checkMethodValid(annotatedElement, clazz))
                return false;
            ExecutableElement annotatedMethod = (ExecutableElement) annotatedElement;
            // class type
            TypeElement classElement = (TypeElement) annotatedMethod.getEnclosingElement();
            // full class name
            String fullPathClassName = classElement.getQualifiedName().toString();

            ProxyInfo proxyInfo = mProxyMap.get(fullPathClassName);
            if (proxyInfo == null) {
                proxyInfo = new ProxyInfo(mElementUtils, classElement);
                mProxyMap.put(fullPathClassName, proxyInfo);
                proxyInfo.setTypeElement(classElement);
            }

            Annotation annotation = annotatedElement.getAnnotation(clazz);
            if (annotation instanceof PermissionGrant) {
                int requestCode = ((PermissionGrant) annotation).value();
                proxyInfo.mGrantMethodMap.put(requestCode, annotatedMethod.getSimpleName().toString());
            } else if (annotation instanceof PermissionDenied) {
                int requestCode = ((PermissionDenied) annotation).value();
                proxyInfo.mDeniedMethodMap.put(requestCode, annotatedMethod.getSimpleName().toString());
            } else if (annotation instanceof ShowRequestPermissionRationale) {
                int requestCode = ((ShowRequestPermissionRationale) annotation).value();
                proxyInfo.mRationaleMethodMap.put(requestCode, annotatedMethod.getSimpleName().toString());
            } else {
                error(annotatedElement, "%s is has not supported.", clazz.getSimpleName());
                return false;
            }

        }
        return true;
    }

    private boolean checkMethodValid(Element element, Class<? extends Annotation> clazz) {
        if (element.getKind() != ElementKind.METHOD) {
            error(element, "%s must be declared on method.", clazz.getSimpleName());
            return false;
        }

        if (ClassValidator.isPrivate(element) || ClassValidator.isAbstract(element)) {
            error(element, "%s() must can not be private or abstract!!!", element.getSimpleName());
            return false;
        }

        return true;
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        mMessager.printMessage(Diagnostic.Kind.NOTE, message, element);
    }

}
