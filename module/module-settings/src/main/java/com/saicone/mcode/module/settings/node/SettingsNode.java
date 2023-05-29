package com.saicone.mcode.module.settings.node;

import com.saicone.mcode.module.settings.Settings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SettingsNode extends NodeValue {

    public static final SettingsNode EMPTY = new SettingsNode("@empty", null);

    private SettingsNode parent;
    private String key;
    private List<String> topComment;
    private List<String> sideComment;

    public SettingsNode(@NotNull String key, @Nullable Object value) {
        this(null, key, value);
    }

    public SettingsNode(@Nullable SettingsNode parent, @NotNull String key, @Nullable Object value) {
        super(value);
        this.parent = parent;
        this.key = key;
    }

    @Nullable
    public SettingsNode getParent() {
        return parent;
    }

    @NotNull
    public String getKey() {
        return key;
    }

    @Nullable
    public List<String> getTopComment() {
        return topComment;
    }

    @Nullable
    public List<String> getSideComment() {
        return sideComment;
    }

    public void replaceValue(@Nullable Object value) {
        if (getValue() == null || value == null) {
            return;
        }
        if (value.getClass() == getValue().getClass()) {
            setValue(value);
        }
    }

    public void setParent(@Nullable SettingsNode parent) {
        this.parent = parent;
    }

    public void setKey(@NotNull String key) {
        this.key = key;
    }

    public void setTopComment(@Nullable List<String> topComment) {
        this.topComment = topComment;
    }

    public void setSideComment(@Nullable List<String> sideComment) {
        this.sideComment = sideComment;
    }

    public void addTopComment(@Nullable List<String> topComment) {
        if (topComment != null) {
            if (this.topComment == null) {
                this.topComment = topComment;
            } else {
                this.topComment.addAll(topComment);
            }
        }
    }

    public void addSideComment(@Nullable List<String> sideComment) {
        if (sideComment != null) {
            if (this.sideComment == null) {
                this.sideComment = sideComment;
            } else {
                this.sideComment.addAll(sideComment);
            }
        }
    }

    public boolean replace(@Nullable Object object) {
        if (exists() && parent instanceof Settings) {
            return ((Settings) parent).set(key, object, true);
        }
        return false;
    }

    public boolean delete() {
        return delete(false);
    }

    public boolean delete(boolean deep) {
        if (exists() && parent instanceof Settings) {
            return ((Settings) parent).remove(key, deep);
        }
        return false;
    }

    public boolean move(@NotNull String... path) {
        if (exists() && parent instanceof Settings) {
            if (((Settings) parent).remove(key)) {
                SettingsNode settings = parent;
                while (settings.getParent() instanceof Settings) {
                    settings = settings.getParent();
                }
                return ((Settings) settings).set(path, this, true);
            }
        }
        return false;
    }

    public boolean exists() {
        return !equals(EMPTY) || parent != null || !key.equals("@empty") || getValue() != null;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean hasTopComment() {
        return topComment != null && !topComment.isEmpty();
    }

    public boolean hasSideComment() {
        return sideComment != null && !sideComment.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SettingsNode objects = (SettingsNode) o;

        return key.equals(objects.key);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + key.hashCode();
        return result;
    }
}
