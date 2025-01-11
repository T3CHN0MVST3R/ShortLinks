package com.denzo;

import java.util.UUID;

public class ConsoleNotifier implements Notifier {
    @Override
    public void notify(UUID userUuid, String message) {
        System.out.println("Уведомление пользователю " + userUuid + ": " + message);
    }
}
