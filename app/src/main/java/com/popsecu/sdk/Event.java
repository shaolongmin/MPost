package com.popsecu.sdk;

/**
 * 事件类型，增加事件类型需修改EventType
 */

public class Event {
	public enum EventType {
		TYPE_01,
		NONE
	};

	private EventType type = EventType.NONE;
	private int intParam = 0;
	private long longParam = 0;
	private String stringParam = null;
	private Object objectParam = null;

	public Event(EventType type) {
		this.type = type;
	}

	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;

	}

	public void setIntParam(int intParam) {
		this.intParam = intParam;
	}

	public int getIntParam() {
		return intParam;
	}

	public void setLongParam(Long longParam) {
		this.longParam = longParam;
	}

	public long getLongParam() {
		return this.longParam;
	}

	public void setStringParam(String stringParam) {
		this.stringParam = stringParam;
	}

	public String getStringParam() {
		return stringParam;
	}

	public void setObjectParam(Object objectParam) {
		this.objectParam = objectParam;
	}

	public Object getObjectParam() {
		return objectParam;
	}
};