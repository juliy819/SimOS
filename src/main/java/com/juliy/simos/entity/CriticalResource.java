package com.juliy.simos.entity;

/**
 * @author JuLiy
 * @date 2022/10/24
 */
public class CriticalResource extends Resource {
    private boolean isLocked;

    public CriticalResource() {
        this.isLocked = false;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }
}
