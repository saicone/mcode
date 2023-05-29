package com.saicone.mcode.module.delivery;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.saicone.mcode.util.CacheSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public abstract class AbstractMessenger {

    protected DeliveryClient deliveryClient;

    protected final Map<String, Set<Consumer<String>>> incomingConsumers = new HashMap<>();

    private final Set<Integer> cachedIds = new CacheSet<>();

    @Nullable
    public DeliveryClient getDeliveryClient() {
        return deliveryClient;
    }

    @NotNull
    public Set<String> getSubscribedChannels() {
        return incomingConsumers.keySet();
    }

    @NotNull
    public Map<String, Set<Consumer<String>>> getIncomingConsumers() {
        return incomingConsumers;
    }

    @NotNull
    protected abstract DeliveryClient loadDeliveryClient();

    public void start() {
        start(loadDeliveryClient());
    }

    public void start(@NotNull DeliveryClient deliveryClient) {
        close();

        deliveryClient.getSubscribedChannels().addAll(getSubscribedChannels());
        deliveryClient.setConsumer(this::receive);
        deliveryClient.setLogConsumer(this::log);

        this.deliveryClient = deliveryClient;
        this.deliveryClient.start();
    }

    public void close() {
        if (deliveryClient != null) {
            deliveryClient.close();
        }
    }

    public void clear() {
        if (deliveryClient != null) {
            deliveryClient.clear();
        }
        incomingConsumers.clear();
        cachedIds.clear();
    }

    public void subscribe(@NotNull String channel, @NotNull Consumer<String> incomingConsumer) {
        if (!incomingConsumers.containsKey(channel)) {
            incomingConsumers.put(channel, new HashSet<>());
        }
        incomingConsumers.get(channel).add(incomingConsumer);
        if (deliveryClient != null) {
            deliveryClient.subscribe(channel);
        }
    }

    public boolean send(@NotNull String channel, @NotNull String message) {
        if (deliveryClient == null) {
            return false;
        }

        final int id = genId();
        cachedIds.add(id);

        final ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeInt(id);
        out.writeUTF(message);
        deliveryClient.send(channel, out.toByteArray());
        return true;
    }

    public boolean receive(@NotNull String channel, byte[] bytes) {
        final Set<Consumer<String>> consumers = incomingConsumers.get(channel);
        if (consumers == null || consumers.isEmpty()) {
            return false;
        }

        final ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        try {
            if (cachedIds.contains(in.readInt())) {
                return false;
            }

            final String message = in.readUTF();
            for (Consumer<String> consumer : consumers) {
                consumer.accept(message);
            }
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    protected void log(int level, @NotNull String msg) {
    }

    protected int genId() {
        return ThreadLocalRandom.current().nextInt(0, 999999 + 1);
    }
}