package com.knight;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class TestClassAdapter extends ClassVisitor implements Opcodes {

    public TestClassAdapter(ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String methodName, String argTypeDesc,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, methodName, argTypeDesc, signature, exceptions);
        String format = String.format("\t%s\t%s\t%s", methodName, argTypeDesc, signature);
        System.out.println(format);
//        System.out.println(Arrays.asList(methodName, argTypeDesc, signature).stream().collect(Collectors.joining("\t")));
        return mv;
    }
}
