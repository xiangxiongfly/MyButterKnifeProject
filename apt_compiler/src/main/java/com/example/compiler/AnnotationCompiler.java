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
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
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


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
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
        //Map集合存储Activity和BindView注解的元素
        Map<String, List<VariableElement>> map = new HashMap<>();

        for (Element e : elements) {
            //获取注解的元素
            VariableElement variableElement = (VariableElement) e;

            //获取外层元素
            Element enclosingElement = variableElement.getEnclosingElement();
            TypeElement typeElement = (TypeElement) enclosingElement;

            String activityName = typeElement.getSimpleName().toString();
            List<VariableElement> variableElements = map.get(activityName);
            if (variableElements == null) {
                variableElements = new ArrayList<>();
                map.put(activityName, variableElements);
            }
            variableElements.add(variableElement);
        }

        if (map.size() > 0) {
            Writer writer = null;
            Iterator<String> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                String activityName = iterator.next();
                List<VariableElement> variableElements = map.get(activityName);

                //生成Java文件
                String packageName = getPackageName(variableElements.get(0));
                String newName = activityName + "_ViewBinding";
                try {
                    JavaFileObject sourceFile = filer.createSourceFile(newName);
                    writer = sourceFile.openWriter();

                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append(String.format("package %s;" +
                            "public class %s {\n" +
                            "public %s(final %s target) {\n", packageName, newName, newName, packageName + "." + activityName));

                    for (VariableElement variableElement : variableElements) {
                        String fieldName = variableElement.getSimpleName().toString();
                        BindView annotation = variableElement.getAnnotation(BindView.class);
                        int viewId = annotation.value();
                        stringBuffer.append(String.format("target.%s = target.findViewById(%s);", fieldName, viewId));
                    }

                    stringBuffer.append("}\n}");

                    writer.write(stringBuffer.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return false;
    }

    public String getPackageName(VariableElement variableElement) {
        Element typeElement = variableElement.getEnclosingElement();
        PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(typeElement);
        Name qualifiedName = packageElement.getQualifiedName();
        return qualifiedName.toString();
    }

}
