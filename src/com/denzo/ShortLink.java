package com.denzo;

import java.time.LocalDateTime;
import java.util.UUID;

public class ShortLink {
    private String longUrl;
    private String shortUrl;
    private int limit;
    private LocalDateTime creationTime;
    private LocalDateTime expireAt;
    private int currentCount;
    private UUID ownerUuid;

    public ShortLink(String longUrl, String shortUrl, int limit, LocalDateTime creationTime, LocalDateTime expireAt, UUID ownerUuid) {
        this.longUrl = longUrl;
        this.shortUrl = shortUrl;
        this.limit = limit;
        this.creationTime = creationTime;
        this.expireAt = expireAt;
        this.ownerUuid = ownerUuid;
        this.currentCount = 0;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public void incrementCount() {
        this.currentCount++;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    @Override
    public String toString() {
        return "ShortLink{" +
                "longUrl='" + longUrl + '\'' +
                ", shortUrl='" + shortUrl + '\'' +
                ", limit=" + limit +
                ", creationTime=" + creationTime +
                ", expireAt=" + expireAt +
                ", currentCount=" + currentCount +
                ", ownerUuid=" + ownerUuid +
                '}';
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireAt);
    }

    public boolean isLimitExceeded() {
        return currentCount >= limit;
    }
}
