package com.denzo;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class App {
    private static UrlShorter urlShorter = new UrlShorter();
    private static Scanner scanner = new Scanner(System.in);
    private static UUID currentUserUuid;

    public static void main(String[] args) {
        initializeUser();
        boolean exit = false;

        while (!exit) {
            showMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    createShortLink();
                    break;
                case "2":
                    listShortLinks();
                    break;
                case "3":
                    useShortLink();
                    break;
                case "4":
                    deleteLink();
                    break;
                case "5":
                    // Новая функция смены пользователя
                    switchUser();
                    break;
                case "6":
                    exit = true;
                    System.out.println("Выход из приложения. До свидания!");
                    break;
                default:
                    System.out.println("Некорректный выбор. Пожалуйста, попробуйте снова.");
            }
        }

        scanner.close();
    }

    // Инициализация или смена текущего пользователя
    private static void initializeUser() {
        System.out.println("Добро пожаловать в сервис сокращения ссылок!");
        chooseUser();
        System.out.println("\nФункционал:");
        System.out.println("- Создание коротких ссылок с ограничениями по времени и количеству переходов.");
        System.out.println("- Просмотр ваших коротких ссылок и их статистики.");
        System.out.println("- Переход по коротким ссылкам.");
        System.out.println("- Удаление ссылки.");
        System.out.println("- Смена пользователя.\n");
    }

    private static void chooseUser() {
        while (true) {
            System.out.println("1. Ввести существующий UUID");
            System.out.println("2. Сгенерировать новый UUID");
            System.out.print("Выберите опцию (1 или 2): ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                System.out.print("Введите ваш UUID: ");
                String uuidInput = scanner.nextLine().trim();
                try {
                    currentUserUuid = UUID.fromString(uuidInput);
                    System.out.println("UUID успешно установлен.");
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println("Некорректный формат UUID. Попробуйте снова.");
                }
            } else if (choice.equals("2")) {
                currentUserUuid = UUID.randomUUID();
                System.out.println("Сгенерирован новый UUID: " + currentUserUuid);
                break;
            } else {
                System.out.println("Некорректный выбор. Пожалуйста, выберите 1 или 2.");
            }
        }
    }

    // Новая функция для смены пользователя
    private static void switchUser() {
        System.out.println("\n--- Смена пользователя ---");
        chooseUser();
        System.out.println("Пользователь успешно изменён.\n");
    }

    private static void showMenu() {
        System.out.println("Меню:");
        System.out.println("1. Создать короткую ссылку");
        System.out.println("2. Просмотреть мои короткие ссылки");
        System.out.println("3. Перейти по короткой ссылке");
        System.out.println("4. Удалить ссылку");
        System.out.println("5. Сменить пользователя");
        System.out.println("6. Выйти");
        System.out.print("Выберите опцию: ");
    }

    private static void createShortLink() {
        System.out.print("Введите длинный URL: ");
        String longUrl = scanner.nextLine().trim();

        if (!isValidUrl(longUrl)) {
            System.out.println("Некорректный URL. Пожалуйста, попробуйте снова.");
            return;
        }

        int limit = readPositiveInt("Введите лимит переходов (целое число): ");
        int lifetimeHours = readPositiveInt("Введите время жизни ссылки в часах (целое число): ");

        String shortUrl = urlShorter.buildShortUrl(longUrl, currentUserUuid, limit, lifetimeHours);
        ShortLink createdLink = urlShorter.getShortLinkByShortUrl(shortUrl);

        System.out.println("\nКороткая ссылка успешно создана!");
        System.out.println("Короткий URL: " + shortUrl);
        System.out.println("Длинный URL: " + createdLink.getLongUrl());
        System.out.println("Лимит переходов: " + createdLink.getLimit());
        System.out.println("Время жизни (часов): " + lifetimeHours);
        System.out.println("Ссылка будет истекать: " + createdLink.getExpireAt() + "\n");
    }

    private static int readPositiveInt(String prompt) {
        int value;
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                value = Integer.parseInt(input);
                if (value <= 0) {
                    System.out.println("Значение должно быть положительным числом.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Некорректный ввод. Пожалуйста, введите целое число.");
            }
        }
    }

    private static void listShortLinks() {
        List<ShortLink> userLinks = urlShorter.getShortLinksByUserUuid(currentUserUuid);
        if (userLinks.isEmpty()) {
            System.out.println("У вас пока нет созданных коротких ссылок.\n");
            return;
        }
        System.out.println("\nВаши короткие ссылки:");
        for (ShortLink link : userLinks) {
            // Добавляем префикс при выводе
            System.out.println("Короткий URL: denzo.com/" + link.getShortUrl());
            System.out.println("Длинный URL: " + link.getLongUrl());
            System.out.println("Лимит переходов: " + link.getLimit());
            System.out.println("Текущее количество переходов: " + link.getCurrentCount());
            System.out.println("Срок действия: " + link.getExpireAt());
            String status = link.isExpired() ? "Истекла" : (link.isLimitExceeded() ? "Лимит переходов исчерпан" : "Активна");
            System.out.println("Статус: " + status);
            System.out.println("---------------------------");
        }
        System.out.println();
    }

    private static void useShortLink() {
        System.out.print("Введите короткий URL: ");
        String shortUrl = scanner.nextLine().trim();
        if (shortUrl.isEmpty()) {
            System.out.println("Короткий URL не может быть пустым.");
            return;
        }
        try {
            String longUrl = urlShorter.restoreLongUrl(shortUrl);
            if (longUrl == null) {
                System.out.println("Ссылка недоступна или была удалена.");
                return;
            }
            System.out.println("Открытие URL: " + longUrl);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(longUrl));
                System.out.println("Ссылка открыта в браузере.\n");
            } else {
                System.out.println("Функция открытия браузера не поддерживается на вашей системе.");
            }
        } catch (Exception e) {
            System.out.println("Произошла ошибка при попытке открыть ссылку: " + e.getMessage());
        }
    }

    // Пример метода удаления ссылки (только владелец может удалить)
    private static void deleteLink() {
        System.out.print("Введите короткий URL для удаления: ");
        String shortUrl = scanner.nextLine().trim();
        if (shortUrl.isEmpty()) {
            System.out.println("Короткий URL не может быть пустым.");
            return;
        }
        boolean success = urlShorter.deleteLink(shortUrl, currentUserUuid);
        if (!success) {
            System.out.println("Удаление ссылки не удалось.");
        }
    }

    private static boolean isValidUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }
}
