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


package com.lhings.java.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to tell the Lhings Java library which methods implement
 * actions, in the <a href="http://support.lhings.com/Getting-started.html">Lhings sense of action</a>.
 * Methods annotated with <code>@Action</code> should have return type void, and may have up to eight
 * parameters, which should be of type int, float, double, boolean, String, java.util.Date and any of
 * the corresponding wrapper types (i. e., they are <a href="http://support.lhings.com/Typed-Parameters.html">typed parameters</a>).
 * 
 * <code>name</code> and <code>description</code> are optional. <code>argumentNames</code> is mandatory and must
 * contain a list of the names of the parameters of the action. They do not need to be the same as the ones of the java
 * method, but they should be in the same order.
 * <code>
{@literal @}Action(name = "speed_up", description = "speed up the car", argumentNames = {"maximum_speed", "gear"})
public void gofaster(float maxspeed, int numberOfGear) {

    // ...

}
 * </code>
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface Action {
	String name() default "";
	String description() default "";
	String[] argumentNames();
}
