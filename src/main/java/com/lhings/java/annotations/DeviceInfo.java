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
 * This annotation is used to provide generic meta information about
 * the device, like its manufacturer, model, serial number, and type of
 * device. The type of device is a special field, because depending on 
 * its value a different icon is shown in Lhings for the device. Currently
 * the Lhings changes its default device icon for a customized one for the
 * following device types:
 * <ul>
 * <li><code>flyport</code>: for <a href="http://www.openpicus.com/site/products">OpenPicus Flyport</a> devices.</li>
 * <li><code>raspberrypi</code>: for <a href="http://www.raspberrypi.org/">Raspberry Pi</a> devices.</li>
 * <li><code>javavirtualdevice</code>: for Java based devices.</li>
 * <li><code>arduino</code>: for <a href="http://www.arduino.cc/">Arduino</a> devices.</li>
 * <li><code>android</code>: for <a href="http://www.android.com/">Android</a> devices.</li>
 * <li><code>waspmote</code>: for <a href="http://www.libelium.com/en/products/waspmote/">Libelium Waspmote</a> devices.</li>
 * <li><code>pluglhings</code>: our PlugLhings mobile app (<a href="https://play.google.com/store/apps/details?id=com.lyncos.lhings&hl=es">Android</a> and <a href="https://itunes.apple.com/us/app/pluglhings/id880028058">iOS</a>).</li>
 * <li><code>TSmoTe</code>: for <a href="http://www.tst-sistemas.es/en/products-2/">TSmoTe</a> devices.</li>
 * <li><code>TSgaTe</code>: for <a href="http://www.tst-sistemas.es/en/products-2/">TSgaTe</a> devices.</li>
 * </ul>
 *
 * If you want the icon of your device added to this list, do not hesitate
 * to <a href="https://lhings.com/laas/contact">contact us</a>.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface DeviceInfo {
	String manufacturer() default "";
	String modelName() default "";
	String serialNumber() default "";
	String deviceType() default "";
}
