/* Copyright 2014 Lyncos Technologies S. L.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */


package com.lhings.java.model;

import java.io.Serializable;
import java.sql.Timestamp;


/**
 * The persistent class for the RULE database table.
 * 
 */
public class Rule implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long pk;

	private Boolean enabled;

	private String name;
	
	private String sourceDeviceName;

	private Long sourceDevicePK;
	
	private String targetDeviceName;

	private Long targetDevicePK;

	private Account account;

	private Timestamp createdAt;

	private Timestamp updatedAt= new Timestamp(System.currentTimeMillis());
	
	//bi-directional many-to-one association to Action
	private Action action;

	private String actionName;
	
	//bi-directional many-to-one association to Event
	private Event event;

	private String eventName;
	
	public Rule() {
	}

	public Long getPk() {
		return this.pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public Boolean getEnabled() {
		return this.enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Action getAction() {
		return this.action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public Event getEvent() {
		return this.event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public String getSourceDeviceName() {
		return sourceDeviceName;
	}

	public void setSourceDeviceName(String sourceDeviceName) {
		this.sourceDeviceName = sourceDeviceName;
	}

	public Long getSourceDevicePK() {
		return sourceDevicePK;
	}

	public void setSourceDevicePK(Long sourceDevicePK) {
		this.sourceDevicePK = sourceDevicePK;
	}

	public String getTargetDeviceName() {
		return targetDeviceName;
	}

	public void setTargetDeviceName(String targetDeviceName) {
		this.targetDeviceName = targetDeviceName;
	}

	public Long getTargetDevicePK() {
		return targetDevicePK;
	}

	public void setTargetDevicePK(Long targetDevicePK) {
		this.targetDevicePK = targetDevicePK;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Timestamp getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Timestamp updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

}