package com.denzo;

import java.util.UUID;

public class User {
    private UUID uuid;

    public User() {
        this.uuid = UUID.randomUUID();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "User{" +
                "uuid = " + uuid +
                '}';
    }
}
