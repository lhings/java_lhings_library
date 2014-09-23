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
import java.util.List;

/**
 * The persistent class for the ACCOUNT database table.
 * 
 */
public class Account implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long pk;

	private String name;

	private String surname1;

	private String surname2;

	private String gender;

	private Timestamp birthdate;

	private String company;

	private String telephonePersonal;

	private String telephoneWork;

	private String apikey;

	private Timestamp createdAt;

	private String password;

	private String username;

	private Boolean enabled;

	private String registrationToken;

	private Timestamp registrationExpiration;

	private Long devicesQuota;
	
	private String locale;
	
	private Boolean skipedTutorialApps;

	
	private Boolean skipedTutorialDevices;
	
	private Boolean skipedTutorialRules;
	
	private String aboutMe;
	
	// bi-directional many-to-one association to Device
	private List<Device> devices;

	private List<Rule> rules;

	public Account() {
	}

	public Long getPk() {
		return this.pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public String getApikey() {
		return this.apikey;
	}

	public void setApikey(String apikey) {
		this.apikey = apikey;
	}

	public Timestamp getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public List<Device> getDevices() {
		return this.devices;
	}

	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}

	public Device addDevice(Device device) {
		getDevices().add(device);
		return device;
	}

	public Device removeDevice(Device device) {
		getDevices().remove(device);
		return device;
	}

	

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}

	public String getRegistrationToken() {
		return registrationToken;
	}

	public void setRegistrationToken(String registrationToken) {
		this.registrationToken = registrationToken;
	}

	public Timestamp getRegistrationExpiration() {
		return registrationExpiration;
	}

	public void setRegistrationExpiration(Timestamp registrationExpiration) {
		this.registrationExpiration = registrationExpiration;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname1() {
		return surname1;
	}

	public void setSurname1(String surname1) {
		this.surname1 = surname1;
	}

	public String getSurname2() {
		return surname2;
	}

	public void setSurname2(String surname2) {
		this.surname2 = surname2;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Timestamp getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(Timestamp birthdate) {
		this.birthdate = birthdate;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getTelephonePersonal() {
		return telephonePersonal;
	}

	public void setTelephonePersonal(String telephonePersonal) {
		this.telephonePersonal = telephonePersonal;
	}

	public String getTelephoneWork() {
		return telephoneWork;
	}

	public void setTelephoneWork(String telephoneWork) {
		this.telephoneWork = telephoneWork;
	}

	public Long getDevicesQuota() {
		return devicesQuota;
	}

	public void setDevicesQuota(Long devicesQuota) {
		this.devicesQuota = devicesQuota;
	}
	
	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public Boolean getSkipedTutorialApps() {
		return skipedTutorialApps;
	}

	public void setSkipedTutorialApps(Boolean skipedTutorialApps) {
		this.skipedTutorialApps = skipedTutorialApps;
	}

	public Boolean getSkipedTutorialDevices() {
		return skipedTutorialDevices;
	}

	public void setSkipedTutorialDevices(Boolean skipedTutorialDevices) {
		this.skipedTutorialDevices = skipedTutorialDevices;
	}

	public Boolean getSkipedTutorialRules() {
		return skipedTutorialRules;
	}

	public void setSkipedTutorialRules(Boolean skipedTutorialRules) {
		this.skipedTutorialRules = skipedTutorialRules;
	}

	public String getAboutMe() {
		return aboutMe;
	}

	public void setAboutMe(String aboutMe) {
		this.aboutMe = aboutMe;
	}

}
