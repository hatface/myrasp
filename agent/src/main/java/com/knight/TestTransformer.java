package com.knight;

import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class TestTransformer implements ClassFileTransformer {

    private ClassPool classPool = new ClassPool(true);

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if(className.equals("java/lang/String")) {
            System.out.println("类 " + className);
            try {
                CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
                for(CtBehavior ctBehavior : ctClass.getDeclaredBehaviors()) {
                    if(ctBehavior.getLongName().equals("java.lang.String.matches(java.lang.String)")) {
                        System.out.println("开始处理方法 " + ctBehavior.getLongName());
                        ctBehavior.insertBefore("java.io.FileWriter fileWriter = new java.io.FileWriter(new java.io.File(\"D://123.txt\"), true);\n" +
                                "                fileWriter.append($1+\"\\n\");\n" +
                                "                fileWriter.flush();\n" +
                                "                fileWriter.close();");
                        ctBehavior.insertAfter("System.out.println(\"后置aop\");");
                    }
                }

                return ctClass.toBytecode();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else
            return null;
    }
}
