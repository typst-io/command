plugins {
    `java-library`
    `maven-publish`
    signing
    id("convention-publish")
    kotlin("jvm")
}

dependencies {
    compileOnly(commons.lombok)
    compileOnly("org.jetbrains:annotations:26.0.2-1")
    compileOnly(kotlin("stdlib-jdk8", libs.versions.kotlin.get()))
    annotationProcessor(commons.lombok)
    testImplementation(enforcedPlatform(commons.junit.bom))
    testImplementation(commons.junit.jupiter)
    testImplementation(commons.assertj.core)
    testRuntimeOnly(commons.junit.platform.launcher)
    testCompileOnly(commons.lombok)
    testAnnotationProcessor(commons.lombok)
}

tasks.test {
    useJUnitPlatform()
}

// TODO: use vavr instead
tasks.register("generateFunctions") {
    doLast {
        for (arity in 3 until 9) {
            val file = File(temporaryDir, "Function${arity}.java")
            val alphabets = (0 until arity).map { ('A' + it) }
            val paramDefs = alphabets.map { "$it ${it.lowercaseChar()}" }
            file.writeText("""// This is an auto-generated code by the Gradle task 'generateTuples' in the project 'command-core'.
package io.typst.command.function;

public interface Function${arity}<${alphabets.joinToString(", ")}, R> {
    R apply(${paramDefs.joinToString(", ")});
}
""")
        }
    }
}

tasks.register("generateTuples") {
    doLast {
        for (arity in 1 until 9) {
            val file = File(temporaryDir, "Tuple${arity}.java")
            val alphabets = (0 until arity).map { ('A' + it) }
            val fields = alphabets.joinToString("\n") { "    $it ${it.lowercaseChar()};" }
            val functions = (0 until arity).joinToString("\n\n") { idx ->
                val ch = alphabets[idx]
                val funcTypeParams = alphabets.toMutableList()
                funcTypeParams[idx] = 'T'
                val params = alphabets.mapIndexed { i, c ->
                    if (i == idx) "f.apply(get${c}())" else "get${c}()"
                }
                """    public <T> Tuple${arity}<${funcTypeParams.joinToString(", ")}> map${idx + 1}(Function<? super ${ch}, ? extends T> f) {
        return new Tuple${arity}<>(${params.joinToString(", ")});
    }"""
            }
            file.writeText("""// This is an auto-generated code by the Gradle task 'generateTuples' in the project 'command-core'.
package io.typst.command.algebra;

import lombok.Value;
import lombok.With;

import java.util.function.Function;

@Value
@With
public class Tuple${arity}<${alphabets.joinToString(", ")}> implements Tuple {
${fields}

${functions}

    @Override
    public int arity() {
        return ${arity};
    }
}
""")
        }
    }
}
