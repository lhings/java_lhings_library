package com.lhings.java.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MethodOrFieldToInstanceMapper {

	private Field field;
	private Method method;
	private Object instance;
	
	public MethodOrFieldToInstanceMapper(Field field, Object instance) {
		this.field = field;
		this.instance = instance;
	}

	public MethodOrFieldToInstanceMapper(Method method, Object instance) {
		this.method = method;
		this.instance = instance;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Object getInstance() {
		return instance;
	}

	public void setInstance(Object instance) {
		this.instance = instance;
	}
	
	
	
}
