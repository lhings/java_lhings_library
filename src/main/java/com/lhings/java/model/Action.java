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
 * The persistent class for the ACTION database table.
 * 
 */

public class Action implements Serializable {
	private static final long serialVersionUID = 1L;


	private Long pk;

//	private List<Rule> rules;
	
	private String name;
	

	private String description;
	

	private List<Argument> inputs;

	
	// the value of this field has to be generated on the fly before JSON serialization
//	private String controlURL;

	public Action(String name, String description, List<Argument> inputs,
			List<Argument> outputs) {
		super();
		this.name = name;
		this.description = description;
		this.inputs = inputs;
	}

	/**
	 * this method sets the value of the transient fields
	 */
//	public void updateTransientFields(){
//		controlURL = "/api/v1/devices/"+this.device.getUuidString()+"/"+this.name;
//	}
	
	public Action() {
	}

	public Long getPk() {
		return this.pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}


	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Argument> getInputs() {
		return inputs;
	}

	public void setInputs(List<Argument> inputs) {
		this.inputs = inputs;
	}


}