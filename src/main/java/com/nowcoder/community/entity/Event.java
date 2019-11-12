package com.nowcoder.community.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author qhhu
 * @date 2019/11/12 - 18:04
 */
public class Event {

    private String topic; // 事件 类型
    private int userId; // 触发事件的人
    private int entityType; // 触发事件的实体
    private int entityId;
    private int entityOwnerId; // 触发事件实体的所有者
    private Map<String, Object> exts = new HashMap<>(); // 额外信息

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityOwnerId() {
        return entityOwnerId;
    }

    public Event setEntityOwnerId(int entityOwnerId) {
        this.entityOwnerId = entityOwnerId;
        return this;
    }

    public Map<String, Object> getExts() {
        return exts;
    }

    public Event setExts(Map<String, Object> exts) {
        this.exts = exts;
        return this;
    }

    public Object getExt(String key) {
        return this.exts.get(key);
    }

    public Event setExt(String key, Object value) {
        this.exts.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return "Event{" +
                "topic='" + topic + '\'' +
                ", userId=" + userId +
                ", entityType=" + entityType +
                ", entityId=" + entityId +
                ", entityOwnerId=" + entityOwnerId +
                ", exts=" + exts +
                '}';
    }
}
