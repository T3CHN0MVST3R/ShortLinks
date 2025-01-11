package com.denzo;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class UrlShorter {
    private final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final int BASE = ALPHABET.length();
    private final int SHORT_URL_LENGTH = 6; // Фиксированная длина короткой ссылки

    // Хранение коротких ссылок: ключ (код) -> ShortLink
    private final Map<String, ShortLink> shortUrlMap = new HashMap<>();
    // Хранение списка ссылок для каждого пользователя
    private final Map<UUID, List<ShortLink>> userLinksMap = new HashMap<>();

    private final Random random = new Random();
    private final Notifier notifier;

    // Конструктор с параметром Notifier
    public UrlShorter(Notifier notifier) {
        this.notifier = notifier;
    }

    // Конструктор по умолчанию, использующий ConsoleNotifier
    public UrlShorter() {
        this.notifier = new ConsoleNotifier();
    }

    /**
     * Генерирует короткую ссылку с префиксом "denzo.com/".
     */
    public String buildShortUrl(String longUrl, UUID userUuid, int limit, int lifetimeHours) {
        String shortUrl;
        do {
            shortUrl = generateRandomShortUrl();
        } while (shortUrlMap.containsKey(shortUrl));

        LocalDateTime creationTime = LocalDateTime.now();
        LocalDateTime expireAt = creationTime.plusHours(lifetimeHours);
        ShortLink shortLink = new ShortLink(longUrl, shortUrl, limit, creationTime, expireAt, userUuid);
        shortUrlMap.put(shortUrl, shortLink);
        userLinksMap.computeIfAbsent(userUuid, k -> new ArrayList<>()).add(shortLink);
        return "denzo.com/" + shortUrl;
    }

    /**
     * Восстанавливает длинный URL по короткой ссылке.
     */
    public String restoreLongUrl(String shortUrl) {
        shortUrl = normalizeShortUrl(shortUrl);
        ShortLink shortLink = shortUrlMap.get(shortUrl);
        if (shortLink == null) {
            System.out.println("Short URL не существует.");
            return null;
        }
        if (shortLink.isExpired()) {
            shortUrlMap.remove(shortUrl);
            userLinksMap.get(shortLink.getOwnerUuid()).remove(shortLink);
            notifier.notify(shortLink.getOwnerUuid(), "Ссылка " + shortUrl + " истекла.");
            return null;
        }
        if (shortLink.isLimitExceeded()) {
            notifier.notify(shortLink.getOwnerUuid(), "Ссылка " + shortUrl + " достигла лимита переходов.");
            return null;
        }
        shortLink.incrementCount();
        return shortLink.getLongUrl();
    }

    /**
     * Редактирует лимит переходов для ссылки.
     * Изменение допустимо только владельцем ссылки.
     */
    public boolean editLimit(String shortUrl, int newLimit, UUID requesterUuid) {
        if (newLimit <= 0) {
            System.out.println("Новый лимит должен быть положительным числом.");
            return false;
        }
        shortUrl = normalizeShortUrl(shortUrl);
        ShortLink link = shortUrlMap.get(shortUrl);
        if (link == null) {
            System.out.println("Короткая ссылка не найдена.");
            return false;
        }
        if (!link.getOwnerUuid().equals(requesterUuid)) {
            System.out.println("Изменять лимит может только владелец ссылки.");
            return false;
        }
        link.setLimit(newLimit);
        System.out.println("Лимит для ссылки " + shortUrl + " успешно изменён на " + newLimit + ".");
        return true;
    }

    /**
     * Удаляет ссылку. Удаление доступно только владельцу.
     */
    public boolean deleteLink(String shortUrl, UUID requesterUuid) {
        shortUrl = normalizeShortUrl(shortUrl);
        ShortLink link = shortUrlMap.get(shortUrl);
        if (link == null) {
            System.out.println("Короткая ссылка не найдена.");
            return false;
        }
        if (!link.getOwnerUuid().equals(requesterUuid)) {
            System.out.println("Удалять ссылку может только её владелец.");
            return false;
        }
        shortUrlMap.remove(shortUrl);
        List<ShortLink> userLinks = userLinksMap.get(link.getOwnerUuid());
        if (userLinks != null) {
            userLinks.remove(link);
        }
        System.out.println("Ссылка " + shortUrl + " успешно удалена.");
        return true;
    }

    /**
     * Нормализует короткий URL, удаляя префикс "denzo.com/", если он присутствует.
     */
    private String normalizeShortUrl(String shortUrl) {
        String prefix = "denzo.com/";
        if (shortUrl.startsWith(prefix)) {
            return shortUrl.substring(prefix.length());
        }
        return shortUrl;
    }

    /**
     * Генерирует случайную строку фиксированной длины, которая используется как код короткой ссылки.
     */
    private String generateRandomShortUrl() {
        StringBuilder sb = new StringBuilder(SHORT_URL_LENGTH);
        for (int i = 0; i < SHORT_URL_LENGTH; i++) {
            int index = random.nextInt(BASE);
            sb.append(ALPHABET.charAt(index));
        }
        return sb.toString();
    }

    /**
     * Очищает просроченные ссылки.
     */
    public void cleanupExpiredLinks() {
        LocalDateTime now = LocalDateTime.now();
        Iterator<Map.Entry<String, ShortLink>> iterator = shortUrlMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ShortLink> entry = iterator.next();
            ShortLink link = entry.getValue();
            if (link.getExpireAt().isBefore(now)) {
                notifier.notify(link.getOwnerUuid(), "Ссылка " + link.getShortUrl() + " истекла и была удалена.");
                iterator.remove();
                userLinksMap.get(link.getOwnerUuid()).remove(link);
            }
        }
    }

    /**
     * Возвращает объект ShortLink по короткому URL.
     */
    public ShortLink getShortLinkByShortUrl(String shortUrl) {
        return shortUrlMap.get(normalizeShortUrl(shortUrl));
    }

    /**
     * Возвращает список коротких ссылок для заданного пользователя.
     */
    public List<ShortLink> getShortLinksByUserUuid(UUID userUuid) {
        return userLinksMap.getOrDefault(userUuid, Collections.emptyList());
    }
}
