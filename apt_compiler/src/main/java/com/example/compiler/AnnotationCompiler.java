package com.example.compiler;

import com.example.annotation.BindView;
import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;

/**
 * 注解处理器
 */
@AutoService(Processor.class)
public class AnnotationCompiler extends AbstractProcessor {
    //生成文件的对象
    private Filer filer;
    //打印日志工具类
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
    }

    /**
     * 支持的注解
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(BindView.class.getCanonicalName());
        return set;
    }

    /**
     * 支持Java版本
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return processingEnv.getSourceVersion();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //获取所有BindView注解的元素
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);

        //生成Map集合，存储Activity和BindView注解的元素
        Map<String, List<VariableElement>> map = new HashMap<>();
        for (Element e : elements) {
            //获取注解的元素
            VariableElement variableElement = (VariableElement) e;
            //获取外层元素
            Element enclosingElement = variableElement.getEnclosingElement();
            //转为类元素
            TypeElement typeElement = (TypeElement) enclosingElement;
            //获取类名
            String className = typeElement.getSimpleName().toString();

            //存储
            List<VariableElement> variableElements = map.get(className);
            if (variableElements == null) {
                variableElements = new ArrayList<>();
                map.put(className, variableElements);
            }
            variableElements.add(variableElement);
        }

        //生成APT文件
        if (map.size() > 0) {
            Iterator<String> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                String className = iterator.next();
                List<VariableElement> variableElements = map.get(className);
                String packageName = getPackageName(variableElements.get(0));
                //生成新的类名
                String newClassName = className + "ViewBinding";

                try {
                    JavaFileObject sourceFile = filer.createSourceFile(newClassName);
                    try (Writer writer = sourceFile.openWriter()) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(String.format("package %s;", packageName))
                                .append(String.format("public class %s {", newClassName))
                                .append(String.format("public %s(final %s activity) {", newClassName, className));
                        for (VariableElement variableElement : variableElements) {
                            String fieldName = variableElement.getSimpleName().toString();
                            BindView annotation = variableElement.getAnnotation(BindView.class);
                            if (annotation != null) {
                                int viewId = annotation.value();
                                stringBuilder.append(String.format("activity.%s = activity.findViewById(%s);", fieldName, viewId));
                            }
                        }
                        stringBuilder.append("}\n}");
                        writer.write(stringBuilder.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


                //可以使用JavaPoet简化代码
                //$L 子面量
                //$S 字符串
                //$T 类型
                //$N 名称
                /*
                MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ClassName.get(packageName, className), "activity");

                for (VariableElement element : variableElements) {
                    if (element.getKind() == ElementKind.FIELD) {
                        BindView annotation = element.getAnnotation(BindView.class);
                        if (annotation != null) {
                            constructorBuilder.addStatement("activity.$N = activity.findViewById($L)", element.getSimpleName(), annotation.value());
                        }
                    }
                }

                TypeSpec typeSpec = TypeSpec.classBuilder(newClassName)
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(constructorBuilder.build())
                        .build();

                try {
                    JavaFile.builder(packageName, typeSpec)
                            .build()
                            .writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                 */
            }
        }

        return false;
    }

    /**
     * 获取包名
     */
    public String getPackageName(VariableElement variableElement) {
        Element typeElement = variableElement.getEnclosingElement();
        return typeElement.getEnclosingElement().toString();
    }

}
