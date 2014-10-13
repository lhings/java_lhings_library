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

import java.util.List;

/**
 * This class provides information about a given device.
 * 
 */

public class Device implements Serializable {
	private static final long serialVersionUID = 1L;

	private String description;

	private Boolean isonline;

	private String name;
	
	private String type;

	private String uuidString;

	private List<Event> events;

	private List<StateVar> stateVariables;

	private List<Action> actions;

	public Device() {
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getIsonline() {
		return this.isonline;
	}

	public void setIsonline(Boolean isonline) {
		this.isonline = isonline;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUuidString() {
		return this.uuidString;
	}

	public void setUuidString(String uuidString) {
		this.uuidString = uuidString;
	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

	public List<StateVar> getStateVariables() {
		return stateVariables;
	}

	public void setStateVariables(List<StateVar> stateVariables) {
		this.stateVariables = stateVariables;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
