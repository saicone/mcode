package com.saicone.mcode.env.asm;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AnnotationMapper extends AnnotationVisitor {

    private static final String DEFAULT_ATTRIBUTE = "value";

    private final Map<String, Object> map;

    private Consumer<Map<String, Object>> consumer;

    public AnnotationMapper() {
        this(new HashMap<>());
    }

    public AnnotationMapper(@NotNull Map<String, Object> map) {
        this(map, null);
    }

    public AnnotationMapper(@NotNull Map<String, Object> map, @Nullable AnnotationVisitor annotationVisitor) {
        super(Opcodes.ASM9, annotationVisitor);
        this.map = map;
    }

    @NotNull
    @Contract("_ -> this")
    public AnnotationMapper consumer(@Nullable Consumer<Map<String, Object>> consumer) {
        this.consumer = consumer;
        return this;
    }

    @NotNull
    public Map<String, Object> getMap() {
        return map;
    }

    @Override
    public void visit(String name, Object value) {
        this.map.put(attribute(name), value);
        super.visit(name, value);
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        this.map.put(attribute(name), value); // Do not convert String value into provided descriptor class
        super.visitEnum(name, descriptor, value);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        final Map<String, Object> annotation = new HashMap<>();
        this.map.put(attribute(name), annotation);
        return new AnnotationMapper(annotation, super.visitAnnotation(name, descriptor));
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        final List<Object> array = new ArrayList<>();
        this.map.put(attribute(name), array);
        return new Array(array, super.visitArray(name));
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if (this.consumer != null) {
            this.consumer.accept(this.map);
        }
    }

    @NotNull
    private static String attribute(@Nullable String name) {
        return name == null ? DEFAULT_ATTRIBUTE : name;
    }

    public static class Array extends AnnotationVisitor {

        private final List<Object> array;

        public Array() {
            this(new ArrayList<>());
        }

        public Array(@NotNull List<Object> array) {
            this(array, null);
        }

        public Array(@NotNull List<Object> array, @Nullable AnnotationVisitor annotationVisitor) {
            super(Opcodes.ASM9, annotationVisitor);
            this.array = array;
        }

        @NotNull
        public List<Object> getArray() {
            return array;
        }

        @Override
        public void visit(String name, Object value) {
            this.array.add(value);
            super.visit(name, value);
        }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            this.array.add(value); // Do not convert String value into provided descriptor class
            super.visitEnum(name, descriptor, value);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            final Map<String, Object> annotation = new HashMap<>();
            this.array.add(annotation);
            return new AnnotationMapper(annotation, super.visitAnnotation(name, descriptor));
        }
    }
}
