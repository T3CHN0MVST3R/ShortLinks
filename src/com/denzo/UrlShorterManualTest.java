package com.denzo;

import java.util.UUID;

public class UrlShorterManualTest {

    public static void main(String[] args) {
        System.out.println("Запуск ручных тестов для UrlShorter...");

        boolean pass = true;
        pass &= testBuildShortUrl();
        pass &= testEditLimit();
        pass &= testDeleteLink();
        pass &= testAdminAccess();
        pass &= testRepeatCreation();
        pass &= testSwitchUser();

        if (pass) {
            System.out.println("\nВсе тесты PASSED.");
        } else {
            System.out.println("\nОдин или несколько тестов FAILED.");
        }
    }

    /**
     * Тестирует создание короткой ссылки:
     * - Проверяет, что короткая ссылка начинается с "denzo.com/"
     */
    public static boolean testBuildShortUrl() {
        Notifier notifier = new ConsoleNotifier();
        UrlShorter urlShorter = new UrlShorter(notifier);
        UUID user = UUID.randomUUID();
        String longUrl = "https://www.baeldung.com/java-9-http-client";

        String shortUrl = urlShorter.buildShortUrl(longUrl, user, 5, 24);
        if (shortUrl != null && shortUrl.startsWith("denzo.com/")) {
            System.out.println("testBuildShortUrl: PASSED");
            return true;
        } else {
            System.out.println("testBuildShortUrl: FAILED");
            return false;
        }
    }

    /**
     * Тестирует редактирование лимита переходов:
     * Проверяет, что владелец может изменить лимит, а новый лимит влияет на поведение ссылки.
     */
    public static boolean testEditLimit() {
        Notifier notifier = new ConsoleNotifier();
        UrlShorter urlShorter = new UrlShorter(notifier);
        UUID user = UUID.randomUUID();
        String longUrl = "https://www.example.com";

        // Создаем ссылку с лимитом 3 перехода
        String shortUrl = urlShorter.buildShortUrl(longUrl, user, 3, 24);
        // Изменим лимит на 5
        boolean edited = urlShorter.editLimit(shortUrl, 5, user);
        if (!edited) {
            System.out.println("testEditLimit: FAILED (не удалось изменить лимит)");
            return false;
        }

        // Выполним 5 переходов; первые 5 должны вернуть URL, 6-й - null.
        boolean passed = true;
        for (int i = 1; i <= 5; i++) {
            String res = urlShorter.restoreLongUrl(shortUrl);
            if (!longUrl.equals(res)) {
                System.out.println("testEditLimit: FAILED на переходе " + i);
                passed = false;
                break;
            }
        }
        String res = urlShorter.restoreLongUrl(shortUrl);
        if (res != null) {
            System.out.println("testEditLimit: FAILED (переход после лимита не заблокирован)");
            passed = false;
        }
        System.out.println("testEditLimit: " + (passed ? "PASSED" : "FAILED"));
        return passed;
    }

    /**
     * Тестирует удаление ссылки:
     * После удаления попытка восстановления должна вернуть null.
     */
    public static boolean testDeleteLink() {
        Notifier notifier = new ConsoleNotifier();
        UrlShorter urlShorter = new UrlShorter(notifier);
        UUID user = UUID.randomUUID();
        String longUrl = "https://www.example.com";

        String shortUrl = urlShorter.buildShortUrl(longUrl, user, 5, 24);
        boolean deleted = urlShorter.deleteLink(shortUrl, user);
        if (!deleted) {
            System.out.println("testDeleteLink: FAILED (ссылка не удалена)");
            return false;
        }
        String restored = urlShorter.restoreLongUrl(shortUrl);
        if (restored != null) {
            System.out.println("testDeleteLink: FAILED (ссылка восстановлена после удаления)");
            return false;
        }
        System.out.println("testDeleteLink: PASSED");
        return true;
    }

    /**
     * Тестирует администрирование – проверяет, что изменение или удаление ссылки не доступно пользователю,
     * который не является её создателем.
     */
    public static boolean testAdminAccess() {
        Notifier notifier = new ConsoleNotifier();
        UrlShorter urlShorter = new UrlShorter(notifier);
        UUID owner = UUID.randomUUID();
        UUID otherUser = UUID.randomUUID();
        String longUrl = "https://www.example.com";

        String shortUrl = urlShorter.buildShortUrl(longUrl, owner, 5, 24);

        // Попытка изменения лимита сторонним пользователем
        boolean editAttempt = urlShorter.editLimit(shortUrl, 10, otherUser);
        // Попытка удаления сторонним пользователем
        boolean deleteAttempt = urlShorter.deleteLink(shortUrl, otherUser);

        if (!editAttempt && !deleteAttempt) {
            System.out.println("testAdminAccess: PASSED");
            return true;
        } else {
            System.out.println("testAdminAccess: FAILED (недопустимые изменения разрешены)");
            return false;
        }
    }

    /**
     * Тестирует повторное создание ссылки:
     * При повторном создании для одного и того же длинного URL генерируются разные короткие URL.
     */
    public static boolean testRepeatCreation() {
        Notifier notifier = new ConsoleNotifier();
        UrlShorter urlShorter = new UrlShorter(notifier);
        UUID user = UUID.randomUUID();
        String longUrl = "https://www.example.com";

        String shortUrl1 = urlShorter.buildShortUrl(longUrl, user, 5, 24);
        String shortUrl2 = urlShorter.buildShortUrl(longUrl, user, 5, 24);

        if (shortUrl1 != null && shortUrl2 != null && !shortUrl1.equals(shortUrl2)) {
            System.out.println("testRepeatCreation: PASSED");
            return true;
        } else {
            System.out.println("testRepeatCreation: FAILED");
            return false;
        }
    }

    /**
     * Тестирует смену пользователя: создаются ссылки для двух разных пользователей.
     * Проверяется, что ссылки принадлежат разным пользователям (ограничения администрирования).
     */
    public static boolean testSwitchUser() {
        Notifier notifier = new ConsoleNotifier();
        UrlShorter urlShorter = new UrlShorter(notifier);

        // Первый пользователь
        UUID user1 = UUID.randomUUID();
        String longUrl = "https://www.example.com";
        String shortUrl1 = urlShorter.buildShortUrl(longUrl, user1, 5, 24);

        // Второй пользователь
        UUID user2 = UUID.randomUUID();
        String shortUrl2 = urlShorter.buildShortUrl(longUrl, user2, 5, 24);

        // Проверим, что ссылки принадлежат разным пользователям
        ShortLink link1 = urlShorter.getShortLinkByShortUrl(shortUrl1);
        ShortLink link2 = urlShorter.getShortLinkByShortUrl(shortUrl2);

        boolean passed = !link1.getOwnerUuid().equals(link2.getOwnerUuid());
        System.out.println("testSwitchUser: " + (passed ? "PASSED" : "FAILED"));
        return passed;
    }
}
