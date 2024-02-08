package com.saicone.mcode.module.lang;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DisplayList<SenderT> implements Display<SenderT>, List<Display<SenderT>> {

    private final List<Display<SenderT>> list;

    public DisplayList(@NotNull List<Display<SenderT>> list) {
        this.list = list;
    }
    @Override
    public void sendTo(@NotNull SenderT type, @NotNull Function<String, String> parser) {
        for (Display<SenderT> display : list) {
            display.sendTo(type, parser);
        }
    }

    @Override
    public void sendTo(@NotNull Collection<SenderT> senders, @NotNull Function<String, String> parser, @NotNull BiFunction<SenderT, String, String> playerParser) {
        for (Display<SenderT> display : list) {
            display.sendTo(senders, parser, playerParser);
        }
    }

    // Default list implementation

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @NotNull
    @Override
    public Iterator<Display<SenderT>> iterator() {
        return list.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(Display<SenderT> display) {
        return list.add(display);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return new HashSet<>(list).containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Display<SenderT>> c) {
        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends Display<SenderT>> c) {
        return list.addAll(index, c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public Display<SenderT> get(int index) {
        return list.get(index);
    }

    @Override
    public Display<SenderT> set(int index, Display<SenderT> element) {
        return list.set(index, element);
    }

    @Override
    public void add(int index, Display<SenderT> element) {
        list.add(index, element);
    }

    @Override
    public Display<SenderT> remove(int index) {
        return list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @NotNull
    @Override
    public ListIterator<Display<SenderT>> listIterator() {
        return list.listIterator();
    }

    @NotNull
    @Override
    public ListIterator<Display<SenderT>> listIterator(int index) {
        return list.listIterator(index);
    }

    @NotNull
    @Override
    public List<Display<SenderT>> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }
}
