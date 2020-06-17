package com.knight;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Agent {

    public static void premain(String agentArgs, Instrumentation inst) throws UnmodifiableClassException {
        LinkedList<Class> retransformClasses = new LinkedList<Class>();
        List<ClassFileTransformer> classFileTransformers = Arrays.asList(/*new TestTransformer(),*/ new PatternTransformer());
        for (ClassFileTransformer classFileTransformer : classFileTransformers)
            inst.addTransformer(classFileTransformer, true);
        Class[] loadedClasses = inst.getAllLoadedClasses();
        for (Class clazz : loadedClasses) {
            if (inst.isModifiableClass(clazz) && !clazz.getName().startsWith("java.lang.invoke.LambdaForm")) {
                retransformClasses.add(clazz);
            }
        }
        // hook已經加載的類
        Class[] classes = new Class[retransformClasses.size()];
        retransformClasses.toArray(classes);
        if (classes.length > 0) {
            inst.retransformClasses(classes);
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
