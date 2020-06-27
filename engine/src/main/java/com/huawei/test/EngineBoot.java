/*
 * Copyright 2017-2020 Baidu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.test;

import com.huawei.test.transformer.CustomClassTransformer;
import org.apache.log4j.Logger;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * Created by tyy on 18-1-24.
 *
 * OpenRasp 引擎启动类
 */
public class EngineBoot implements Module {

    private CustomClassTransformer transformer;

    @Override
    public void start(String mode, Instrumentation inst) throws Exception {
        System.out.println("\n\n" +
                "Anonymous@AnonymousMBP ~ % whoami\n" +
                "--/-.--/.-./.-/.../.--.\n");

        Agent.readVersion();
        initTransformer(inst);
        String message = "[OpenRASP] Engine Initialized [" + Agent.projectVersion + " (build: GitCommit="
                + Agent.gitCommit + " date=" + Agent.buildTime + ")]";
        System.out.println(message);
        Logger.getLogger(EngineBoot.class.getName()).info(message);
    }

    @Override
    public void release(String mode) {

    }

    private void deleteTmpDir() {

    }

    /**
     * 初始化配置
     *
     * @return 配置是否成功
     */
    private boolean loadConfig() throws Exception {

        return true;
    }

    /**
     * 初始化类字节码的转换器
     *
     * @param inst 用于管理字节码转换器
     */
    private void initTransformer(Instrumentation inst) throws UnmodifiableClassException {
        transformer = new CustomClassTransformer(inst);
        transformer.retransform();
    }

}
