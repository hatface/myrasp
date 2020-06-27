package com.knight;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarFile;

public class Agent {


    /**
     * 添加jar文件到jdk的跟路径下，优先加载
     *
     * @param inst {@link Instrumentation}
     */
    public static void addJarToBootstrap(Instrumentation inst) throws IOException {
        String localJarPath = getLocalJarPath();
//        inst.appendToBootstrapClassLoaderSearch(new JarFile(localJarPath));
//        inst.appendToSystemClassLoaderSearch(new JarFile(localJarPath));
    }

    /**
     * 获取当前所在jar包的路径
     *
     * @return jar包路径
     */
    public static String getLocalJarPath() {
        URL localUrl = Agent.class.getProtectionDomain().getCodeSource().getLocation();
        String path = null;
        try {
            path = URLDecoder.decode(
                    localUrl.getFile().replace("+", "%2B"), "UTF-8");
            System.out.println(path);
        } catch (UnsupportedEncodingException e) {
            System.err.println("[OpenRASP] Failed to get jarFile path.");
            e.printStackTrace();
        }
        return path;
    }


    public static void premain(String agentArgs, Instrumentation inst) throws UnmodifiableClassException, IOException {

        addJarToBootstrap(inst);


        LinkedList<Class> retransformClasses = new LinkedList<Class>();
        List<ClassFileTransformer> classFileTransformers = Arrays.asList(/*new TestTransformer(),*/ new PatternTransformer());
        for (ClassFileTransformer classFileTransformer : classFileTransformers)
            inst.addTransformer(classFileTransformer, true);
        Class[] loadedClasses = inst.getAllLoadedClasses();
        for (Class clazz : loadedClasses) {
//            System.out.println(clazz.getName());
            if (inst.isModifiableClass(clazz) && !clazz.getName().startsWith("java.lang.invoke.LambdaForm") ) {
                retransformClasses.add(clazz);
            }
        }
        // hook已經加載的類
        Class[] classes = new Class[retransformClasses.size()];
        Class[] classes1 = retransformClasses.toArray(classes);
        System.out.println();
        if (classes1.length > 0) {
            for (Class clazz : retransformClasses)
            {
                if(clazz.getName().startsWith("java.util.regex.Pattern$"))
                inst.retransformClasses(clazz);
            }
        }
    }

    public static void initTransform(Instrumentation instrumentation) {
        LinkedList<Class> retransformClasses = new LinkedList<Class>();
        Class[] loadedClasses = instrumentation.getAllLoadedClasses();//获取已经加载的类
        instrumentation.addTransformer(new TestTransformer());
        for (Class clazz : loadedClasses) {
            try {
                instrumentation.retransformClasses(clazz);
            } catch (UnmodifiableClassException e) {
                e.printStackTrace();
            }
        }
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {

    }
}
