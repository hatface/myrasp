package com.knight;

public class RedosPatternInstaller {
//
//    public void install(Instrumentation inst) {
//        AgentBuilder.Transformer transformer = (builder, typeDescription, classLoader) -> {
//            return builder
//                    .method(ElementMatchers.<MethodDescription>any()) // 拦截任意方法
//                    .intercept(MethodDelegation.to(new TimeInterceptor())); // 委托
//        };
//
//        AgentBuilder.Listener listener = new AgentBuilder.Listener() {
//            @Override
//            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, DynamicType dynamicType) {
//            }
//
//            @Override
//            public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
//            }
//
//            @Override
//            public void onError(String typeName, ClassLoader classLoader, JavaModule module, Throwable throwable) {
//            }
//
//            @Override
//            public void onComplete(String typeName, ClassLoader classLoader, JavaModule module) {
//            }
//        };
//
//        new AgentBuilder
//                .Default()
//                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
//                .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
//                .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
//                .type(ElementMatchers.any()) // 指定需要拦截的类
//                .transform(transformer)
//                .with(listener)
//                .installOn(inst);
//    }
}
