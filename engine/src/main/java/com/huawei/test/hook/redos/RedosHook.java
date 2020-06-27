package com.huawei.test.hook.redos;

import com.huawei.test.hook.AbstractClassHook;
import com.huawei.test.tool.annotation.HookAnnotation;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@HookAnnotation
public class RedosHook extends AbstractClassHook {

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
    @Override
    public boolean isClassMatched(String className) {
        boolean flag = false;
        if (className.startsWith("java/util/regex/Pattern$")) {
            String tmpClassName = className.replace("java/util/regex/Pattern$", "");
            if (nameSet.contains(tmpClassName)) {
                flag = true;
            }
        }
        return flag;
    }

    @Override
    public String getType() {
        return "redos";
    }

    @Override
    protected void hookMethod(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        String invokeStaticSrc = getInvokeStaticSrc(RedosHook.class, "printRedos", "$1.pattern().pattern()", String.class);
        for (CtBehavior ctBehavior : ctClass.getDeclaredBehaviors()) {
            if (ctBehavior.getLongName().contains("match(java.util.regex.Matcher,int,java.lang.CharSequence)")) {
                insertBefore(ctBehavior, invokeStaticSrc );
            }
        }

    }

    private static Set<String> redosSet = new HashSet<String>();

    public static  void printRedos(String str)
    {
        if (!redosSet.contains(str))
        {
            redosSet.add(str);
            System.out.println(str);
        }
    }
}
