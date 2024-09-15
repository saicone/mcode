package com.saicone.mcode.env.asm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class AnnotationConsumer extends ClassVisitor {

    private final Predicate<String> predicate;
    private final BiConsumer<String, Map<String, Object>> consumer;

    public AnnotationConsumer(@NotNull Predicate<String> predicate, @NotNull BiConsumer<String, Map<String, Object>> consumer) {
        this(predicate, consumer, null);
    }

    public AnnotationConsumer(@NotNull Predicate<String> predicate, @NotNull BiConsumer<String, Map<String, Object>> consumer, @Nullable ClassVisitor classVisitor) {
        super(Opcodes.ASM9, classVisitor);
        this.predicate = predicate;
        this.consumer = consumer;
    }

    @NotNull
    public Predicate<String> getPredicate() {
        return predicate;
    }

    @NotNull
    public BiConsumer<String, Map<String, Object>> getConsumer() {
        return consumer;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (this.predicate.test(descriptor)) {
            return new AnnotationMapper(new HashMap<>(), super.visitAnnotation(descriptor, visible)).consumer(map -> this.consumer.accept(null, map));
        }
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        return new FieldVisitor(Opcodes.ASM9, super.visitField(access, name, descriptor, signature, value)) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                if (AnnotationConsumer.this.predicate.test(descriptor)) {
                    return new AnnotationMapper(new HashMap<>(), super.visitAnnotation(descriptor, visible)).consumer(map -> AnnotationConsumer.this.consumer.accept("#" + name, map));
                }
                return super.visitAnnotation(descriptor, visible);
            }
        };
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                if (AnnotationConsumer.this.predicate.test(descriptor)) {
                    return new AnnotationMapper(new HashMap<>(), super.visitAnnotation(descriptor, visible)).consumer(map -> AnnotationConsumer.this.consumer.accept("#" + name + "()", map));
                }
                return super.visitAnnotation(descriptor, visible);
            }
        };
    }
}
