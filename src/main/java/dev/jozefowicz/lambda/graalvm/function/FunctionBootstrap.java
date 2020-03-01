package dev.jozefowicz.lambda.graalvm.function;

import dev.jozefowicz.lambda.graalvm.runtime.GraalVMRuntime;

public class FunctionBootstrap {

    public static void main(String[] args) {
        new GraalVMRuntime(new NativePutItemHandler()).execute();
    }

}
