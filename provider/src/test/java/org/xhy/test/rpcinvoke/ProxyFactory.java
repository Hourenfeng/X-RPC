package org.xhy.test.rpcinvoke;

import javassist.*;

import java.lang.reflect.Method;

public class ProxyFactory {

    public static <T> T getProxy(Class<T> interfaceClass, Class<?> targetClass, Object target) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(targetClass));

        String proxyClassName = targetClass.getName() + "Proxy";
        CtClass ctClass = pool.makeClass(proxyClassName);

        CtClass superClass = pool.get(targetClass.getName());
        ctClass.setSuperclass(superClass);

        CtClass interfaceType = pool.get(interfaceClass.getName());
        ctClass.addInterface(interfaceType);

        // 添加字段 private Object target;
        CtField field = new CtField(pool.get("java.lang.Object"), "target", ctClass);
        field.setModifiers(java.lang.reflect.Modifier.PRIVATE);
        ctClass.addField(field);

        // 构造函数 public Proxy(Object target) { this.target = target; }
        CtConstructor constructor = new CtConstructor(new CtClass[]{pool.get("java.lang.Object")}, ctClass);
        constructor.setBody("{ this.target = $1; }");
        ctClass.addConstructor(constructor);

        // 添加方法
        for (Method method : targetClass.getDeclaredMethods()) {
            // 获取参数类型数组并转换为 CtClass[]
            Class<?>[] paramTypes = method.getParameterTypes();
            CtClass[] ctParams = new CtClass[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                ctParams[i] = pool.get(paramTypes[i].getName());
            }
            String methodName = method.getName();
            CtMethod ctMethod = new CtMethod(
                    pool.get(method.getReturnType().getName()),
                    methodName,
                    ctParams,
                    ctClass
            );
            ctMethod.setBody("{ return ((" + targetClass.getName() + ")this.target)." + methodName + "($$); }");
            ctClass.addMethod(ctMethod);
        }

        Class<?> proxyClass = ctClass.toClass();
        return (T) proxyClass.getConstructor(Object.class).newInstance(target);
    }

}
