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


public class StateVar implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	
	private Boolean modifiable;

	private String type;

	private Device device;

	
	public StateVar() {
	}


	public StateVar(String name, Boolean modifiable, String type) {
		super();
		this.name = name;
		this.modifiable = modifiable;
		this.type = type;
	}


	public Boolean getModifiable() {
		return this.modifiable;
	}

	public void setModifiable(Boolean modifiable) {
		this.modifiable = modifiable;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Device getDevice() {
		return this.device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


}