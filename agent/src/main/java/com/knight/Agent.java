package com.knight;

import java.lang.instrument.Instrumentation;

public class Agent {

    public static void premain(String agentArgs, Instrumentation inst)
    {
//        inst.addTransformer(new TestTransformer());
        System.out.println("agent start");
        new RedosPatternInstaller().install(inst);

    }

    public static void agentmain(String agentArgs, Instrumentation inst)
    {

    }
}
