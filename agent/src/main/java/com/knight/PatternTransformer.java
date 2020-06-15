package com.knight;

import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class PatternTransformer implements ClassFileTransformer {

    private ClassPool classPool = new ClassPool(true);

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        System.out.println(className);
        if(className.equals("java/util/regex/Pattern")) {
            System.out.println("类 " + className);
            try {
                CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
                for(CtBehavior ctBehavior : ctClass.getDeclaredBehaviors()) {
                    if(ctBehavior.getLongName().equals("java.util.regex.Pattern(java.lang.String, int)")) {
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
