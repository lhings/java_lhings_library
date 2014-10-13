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
 * Model for the actions.
 *
 */
public class Action implements Serializable {

    public static final int NO_PAYLOAD = -1;

    private static final long serialVersionUID = 1L;

    private String name;

    private String description;

    private List<Argument> inputs;

    private boolean payloadNeeded = false;
    
    public Action(String name, String description, List<Argument> inputs,
            List<Argument> outputs) {
        super();
        this.name = name;
        this.description = description;
        this.inputs = inputs;
    }

    public Action() {
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

    public boolean isPayloadNeeded() {
        return payloadNeeded;
    }

    public void setPayloadNeeded(boolean payloadNeeded) {
        this.payloadNeeded = payloadNeeded;
    }

}
