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
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * 注解处理器
 */
@AutoService(Processor.class)
public class AnnotationCompiler extends AbstractProcessor {
    //生成Java代码
    private Filer mFiler;
    //打印日志工具类
    private Messager mMessager;

    /**
     * 初始化
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
    }

    /**
     * 支持的注解
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        //添加需要处理的注解
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
        //用于存储Activity的全类名和BindView注解的元素
        Map<String, List<VariableElement>> map = new HashMap<>();

        for (Element e : elements) {
            //获取注解的元素
            VariableElement variableElement = (VariableElement) e;
            //获取外层元素，也就是Activity，并转为TypeElement
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            //获取Activity全类名
            String activityFullName = typeElement.getQualifiedName().toString();
            mMessager.printMessage(Diagnostic.Kind.NOTE, "TypeElement:" + activityFullName + ",VariableElement:" + variableElement);

            //存储
            List<VariableElement> variableElements = map.get(activityFullName);
            if (variableElements == null) {
                variableElements = new ArrayList<>();
                map.put(activityFullName, variableElements);
            }
            variableElements.add(variableElement);
        }

        //生成APT文件
        if (map.size() > 0) {
            Iterator<String> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                //获取Activity全类名
                String activityFullName = iterator.next();
                //获取Activity下的注解元素
                List<VariableElement> variableElements = map.get(activityFullName);
                //获取类元素
                TypeElement typeElement = (TypeElement) variableElements.get(0).getEnclosingElement();
                //获取Activity名
                String activitySimpleName = typeElement.getSimpleName().toString();
                //获取包元素
                PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(typeElement);
                //获取包名
                String packageName = packageElement.getQualifiedName().toString();

                //生成新类名
                String newClassName = activitySimpleName + "_ViewBinding";

                try {
                    JavaFileObject javaFileObject = mFiler.createSourceFile(newClassName);
                    try (Writer writer = javaFileObject.openWriter()) {
                        StringBuilder stringBuilder = new StringBuilder();
                        // package com.example.app_apt;
                        stringBuilder.append("package " + packageName + ";\n")
                                // import android.view.View;
                                .append("import android.view.View;\n")
                                // public class MainActivity_ViewBinding {
                                .append("public class " + newClassName + " {\n")
                                // public MainActivity_ViewBinding(MainActivity target) {
                                .append("public " + newClassName + "(" + activitySimpleName + " target){\n");
                        for (VariableElement variableElement : variableElements) {
                            //获取注解元素，也就是成员变量名
                            String fieldName = variableElement.getSimpleName().toString();
                            //获取注解
                            BindView annotation = variableElement.getAnnotation(BindView.class);
                            if (annotation != null) {
                                int viewId = annotation.value();
                                stringBuilder.append("target." + fieldName + " = target.findViewById(" + viewId + ");\n");
                            }
                        }
                        stringBuilder.append("}\n");
                        stringBuilder.append("}\n");
                        writer.write(stringBuilder.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}
