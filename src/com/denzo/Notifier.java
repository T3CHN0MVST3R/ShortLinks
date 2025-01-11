package com.denzo;

import java.util.UUID;

public interface Notifier {
    void notify(UUID userUuid, String message);
}
