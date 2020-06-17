package com.knight;

import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

public class PatternTransformer implements ClassFileTransformer {

    private ClassPool classPool = new ClassPool(true);
    private Set nameSet = new HashSet() {{
        this.add("Curly");
        this.add("Node");
        this.add("LastNode");
        this.add("Start");
        this.add("StartS");
        this.add("Begin");
        this.add("End");
        this.add("Caret");
        this.add("UnixCaret");
        this.add("LastMatch");
        this.add("Dollar");
        this.add("UnixDollar");
        this.add("LineEnding");
        this.add("CharProperty");
        this.add("BmpCharProperty");
        this.add("Slice");
        this.add("SliceI");
        this.add("SliceU");
        this.add("SliceS");
        this.add("SliceIS");
        this.add("Ques");
        this.add("GroupCurly");
        this.add("BranchConn");
        this.add("Branch");
        this.add("GroupHead");
        this.add("GroupRef");
        this.add("GroupTail");
        this.add("Prolog");
        this.add("Loop");
        this.add("LazyLoop");
        this.add("BackRef");
        this.add("CIBackRef");
        this.add("First");
        this.add("Conditional");
        this.add("Pos");
        this.add("Neg");
        this.add("Behind");
        this.add("BehindS");
        this.add("NotBehind");
        this.add("NotBehindS");
        this.add("Bound");
        this.add("BnM");
        this.add("BnMS");
    }};


    private Set a = new HashSet();

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.startsWith("java/util/regex/Pattern$")) {
            String tmpClassName = className.replace("java/util/regex/Pattern$", "");
            if (!nameSet.contains(tmpClassName)) {
                return classfileBuffer;
            }
            try {
                CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));

                CtField regexPatterns = CtField.make("private java.util.Set regexPatterns = new java.util.HashSet();", ctClass);
                ctClass.addField(regexPatterns);

                for (CtBehavior ctBehavior : ctClass.getDeclaredBehaviors()) {
                    if (ctBehavior.getLongName().contains("match(java.util.regex.Matcher,int,java.lang.CharSequence)")) {
                        ctBehavior.insertBefore(
                                "if (!regexPatterns.contains($1.pattern().pattern())){\n" +
                                        "regexPatterns.add($1.pattern().pattern());\n" +
                                        "java.io.FileWriter fileWriter = new java.io.FileWriter(new java.io.File(\"/tmp/123.txt\"), true);\n" +
                                        "fileWriter.append($1.pattern().pattern()+\"\\n\");\n" +
                                        "fileWriter.flush();\n" +
                                        "fileWriter.close();\n" +
                                        "}");
                    }
                }
                return ctClass.toBytecode();
            } catch (Exception e) {
                e.printStackTrace();
                return classfileBuffer;
            }
        } else
            return classfileBuffer;
    }
}
