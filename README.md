

## 实现Butterknife功能

### 基于反射实现

反射比较消耗资源，一般不推荐使用。

#### 创建注解

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface BindView {
    int value();
}
```

#### 创建绑定类

```java
public class MyButterknife {
    public static void bind(Activity activity) {
        Field[] fields = activity.getClass().getDeclaredFields();
        for (Field field : fields) {
            BindView annotation = field.getAnnotation(BindView.class);
            if (annotation != null) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                try {
                    field.set(activity, activity.findViewById(annotation.value()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```

#### 使用

```java
public class MainActivity extends AppCompatActivity {
    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.imageView)
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyButterknife.bind(this);
        textView.setText("hello");
        imageView.setImageResource(R.mipmap.ic_launcher);
    }
}
```



### 基于APT实现

反射比较消耗资源，一般不推荐这样使用，可以使用APT实现相同功能。

一个APT项目需要至少两个Java Library模块组成，一个模块负责提供注解，另外一个模块负责注解处理。

#### 创建注解模块

```java
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface BindView {
    int value();
}
```

#### 创建注解处理模块

```groovy
dependencies {
    //编译时期进行注解处理
    annotationProcessor 'com.google.auto.service:auto-service:1.0-rc4'
    compileOnly 'com.google.auto.service:auto-service:1.0-rc4'
    //生成Java代码
    implementation 'com.squareup:javapoet:1.10.0'
    // 依赖于注解
    implementation project(':apt_annotation')
}
```

```java
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
```

**可以使用JavaPoet简化代码**

```java
//$L 子面量
//$S 字符串
//$T 类型
//$N 名称
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
```

#### 创建依赖库

```groovy
dependencies {
    implementation project(':apt_annotation')
    annotationProcessor project(':apt_compiler')
}
```

```java
public class MyButterKnife {
    public static void bind(Activity activity) {
        Class<?> aClass = activity.getClass();
        String binderName = aClass.getName() + "ViewBinding";
        try {
            Class<?> viewBinderClz = Class.forName(binderName);
            Constructor<?> constructor = viewBinderClz.getConstructor(activity.getClass());
            constructor.newInstance(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

#### 使用

```java
public class MainActivity extends AppCompatActivity {
    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.imageView)
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyButterKnife.bind(this);
        textView.setText("hello");
        imageView.setImageResource(R.mipmap.ic_launcher);
    }
}
```

