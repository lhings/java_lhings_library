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

package com.lhings.java.http;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.lhings.java.LhingsDevice;
import com.lhings.java.exception.BadRequestException;
import com.lhings.java.exception.DeviceDoesNotExistException;
import com.lhings.java.exception.LhingsException;
import com.lhings.java.exception.UnauthorizedException;
import com.lhings.java.logging.LhingsLogger;
import com.lhings.java.model.Device;
import com.lhings.java.model.NameValueBean;

/**
 * This class provides convenience methods to communicate with the Lhings server
 * using http. Check <a href =
 * "http://support.lhings.com/Lhings-API-Documentation.html"> the Lhings API
 * documentation</a> for more details.
 * 
 * @author jose
 *
 */
public final class WebServiceCom {

	static {
		System.setProperty("jsse.enableSNIExtension", "false");
		System.setProperty("javax.net.ssl.trustStore", "./lhings-java.keystore");
	}

	@SuppressWarnings("unused")
	private static Logger log = LhingsLogger.getLogger();
	private static final String LHINGS_V1_API_PREFIX = "https://www.lhings.com/laas/api/v1/";
	private static final int LHINGS_ERROR_HTTP_STATUS = 457;
	private static final int LHINGS_V1_API_BAD_REQUEST_ERROR_CODE = 400;
	private static final int LHINGS_V1_API_UNAUTHORIZED_ERROR_CODE = 401;
	private static final int LHINGS_V1_API_NOT_FOUND_ERROR_CODE = 404;

	/**
	 * Returns the api key of the account, given the username and password.
	 * 
	 * @param username
	 *            The username of the account.
	 * @param password
	 *            The password of the account.
	 * @return The api key of the account.
	 * @throws UnauthorizedException
	 *             If the given username and password combination is not valid.
	 * @throws IOException
	 *             If there is any problem with the underlying HTTP
	 *             communication.
	 */
	public static String getApiKey(String username, String password)
			throws LhingsException, IOException {
		String url = LHINGS_V1_API_PREFIX + "account/" + username
				+ "/apikey?password=" + password;

		String json = executeGet(url, "");
		return new JSONObject(json).getString("value");
	}

	public static void sendEvent(LhingsDevice lhingsDevice, String name,
			String payload) throws IOException, LhingsException {
		String url = LHINGS_V1_API_PREFIX + "devices/" + lhingsDevice.uuid()
				+ "/events/" + name;
		String postBody = payload;
		executePost(lhingsDevice.apiKey(), url, postBody);

	}

	private static String executePost(String apikey, String url, String postBody)
			throws IOException, LhingsException {
		HttpPost postRequest = new HttpPost(url);

		postRequest.setHeader("Content-Type", "application/json");
		HttpEntity entity = new StringEntity(postBody, Charset.forName("utf-8"));
		postRequest.setEntity(entity);
		postRequest.setHeader("X-Api-Key", apikey);
		HttpResponse response = executeRequest(postRequest);
		String json = response.getResponseBody();
		int status = response.getStatusCode();
		if (status == LHINGS_ERROR_HTTP_STATUS) {
			int lhingsErrorCode = new JSONObject(json).getInt("responseStatus");
			String errorMessage = new JSONObject(json).getString("message");
			switch (lhingsErrorCode) {
			case LHINGS_V1_API_BAD_REQUEST_ERROR_CODE:
				throw new BadRequestException(
						"Lhings API error. HTTP status " + status + ". Lhings error code " + lhingsErrorCode + " - "
								+ errorMessage);
			case LHINGS_V1_API_UNAUTHORIZED_ERROR_CODE:
				throw new UnauthorizedException(
						"Lhings API error. HTTP status " + status + ". Lhings error code " + lhingsErrorCode + " - "
								+ errorMessage);
			case LHINGS_V1_API_NOT_FOUND_ERROR_CODE:
				throw new DeviceDoesNotExistException(
						"Lhings API error. HTTP status " + status + ". Lhings error code " + lhingsErrorCode + " - "
								+ errorMessage);
			default:
				throw new LhingsException("Lhings API error. HTTP status " + status + ". Lhings error code " + lhingsErrorCode + " - "
						+ errorMessage);
			}
		} else if (status != 200 && status != 201) {
			// HTTP error
			throw new LhingsException("HTTP error. Code: " + status + " - " + response.getStatusMessage());
		}
		return json;
	}

	private static String executeGet(String url, String apikey)
			throws IOException, LhingsException {
		HttpGet getRequest = new HttpGet(url);
		getRequest.setHeader("X-Api-Key", apikey);
		HttpResponse response = executeRequest(getRequest);
		int status = response.getStatusCode();
		String json = response.getResponseBody();
		if (status == LHINGS_ERROR_HTTP_STATUS) {
			int lhingsErrorCode = new JSONObject(json).getInt("responseStatus");
			String errorMessage = new JSONObject(json).getString("message");
			switch (lhingsErrorCode) {
			case LHINGS_V1_API_BAD_REQUEST_ERROR_CODE:
				throw new BadRequestException(
						"Lhings API error. HTTP status " + status + ". Lhings error code " + lhingsErrorCode + " - "
								+ errorMessage);
			case LHINGS_V1_API_UNAUTHORIZED_ERROR_CODE:
				throw new UnauthorizedException(
						"Lhings API error. HTTP status " + status + ". Lhings error code " + lhingsErrorCode + " - "
								+ errorMessage);
			case LHINGS_V1_API_NOT_FOUND_ERROR_CODE:
				throw new DeviceDoesNotExistException(
						"Lhings API error. HTTP status " + status + ". Lhings error code " + lhingsErrorCode + " - "
								+ errorMessage);
			default:
				throw new LhingsException("Lhings API error. HTTP status " + status + ". Lhings error code " + lhingsErrorCode + " - "
						+ errorMessage);
			}
		} else if (status != 200 && status != 201) {
			// HTTP error
			throw new LhingsException("HTTP error. Code: " + status + " - " + response.getStatusMessage());
		}

		return json;
	}

	public static String registerDevice(LhingsDevice lhingsDevice)
			throws IOException, LhingsException {
		String url = LHINGS_V1_API_PREFIX + "devices/";
		String postBody = "{ \"name\": \"deviceName\", \"value\": \""
				+ lhingsDevice.getName() + "\"}";
		String json = executePost(lhingsDevice.apiKey(), url, postBody);

		String uuid = new JSONObject(json).getString("uuid");
		if (uuid == null)
			throw new LhingsException(
					"ERROR: could not register device in Lhings.");
		return uuid;
	}

	public static List<Device> deviceList(LhingsDevice lhingsDevice)
			throws LhingsException, IOException {
		String url = LHINGS_V1_API_PREFIX + "devices/?verbose";
		String json = executeGet(url, lhingsDevice.apiKey());
		JSONArray deviceArray = new JSONArray(json);
		List<Device> deviceList = new ArrayList<Device>();
		for (int j = 0; j < deviceArray.length(); j++) {
			JSONObject jsonObj = deviceArray.getJSONObject(j);
			Device device = new Device();
			device.setUuidString(jsonObj.getString("uuid"));
			device.setName(jsonObj.getString("name"));
			device.setType(jsonObj.getString("type"));
			device.setIsonline((jsonObj.getBoolean("online")));
			deviceList.add(device);
		}
		return deviceList;
	}

	public static void startSession(LhingsDevice lhingsDevice)
			throws IOException, LhingsException {
		startEndSession(lhingsDevice, true);
	}

	public static void endSession(LhingsDevice lhingsDevice)
			throws IOException, LhingsException {
		startEndSession(lhingsDevice, false);
	}

	private static void startEndSession(LhingsDevice lhingsDevice, Boolean start)
			throws IOException, LhingsException {
		String url = LHINGS_V1_API_PREFIX + "devices/" + lhingsDevice.uuid()
				+ "/states/online";
		String putBody = "{ \"name\": \"online\", \"value\": "
				+ start.toString() + "}";
		executePut(lhingsDevice.apiKey(), url, putBody);
	}

	public static void sendDescriptor(LhingsDevice lhingsDevice,
			String jsonDescriptor) throws IOException, LhingsException {
		String url = LHINGS_V1_API_PREFIX + "devices/" + lhingsDevice.uuid()
				+ "/";
		String putBody = jsonDescriptor;
		executePut(lhingsDevice.apiKey(), url, putBody);
	}

	private static void executePut(String apikey, String url, String requestBody)
			throws IOException, LhingsException {
		HttpPut putRequest = new HttpPut(url);
		putRequest.setHeader("Content-Type", "application/json");
		HttpEntity entity = new StringEntity(requestBody,
				Charset.forName("utf-8"));
		putRequest.setEntity(entity);
		putRequest.setHeader("X-Api-Key", apikey);
		HttpResponse response = executeRequest(putRequest);
		String json = response.getResponseBody();
		int status = response.getStatusCode();
		if (status == LHINGS_ERROR_HTTP_STATUS) {
			int lhingsErrorCode = new JSONObject(json).getInt("responseStatus");
			String errorMessage = new JSONObject(json).getString("message");
			switch (lhingsErrorCode) {
			case LHINGS_V1_API_BAD_REQUEST_ERROR_CODE:
				throw new BadRequestException(
						"Lhings API error. HTTP status " + status + ". Lhings error code " + lhingsErrorCode + " - "
								+ errorMessage);
			case LHINGS_V1_API_UNAUTHORIZED_ERROR_CODE:
				throw new UnauthorizedException(
						"Lhings API error. HTTP status " + status + ". Lhings error code " + lhingsErrorCode + " - "
								+ errorMessage);
			case LHINGS_V1_API_NOT_FOUND_ERROR_CODE:
				throw new DeviceDoesNotExistException(
						"Lhings API error. HTTP status " + status + ". Lhings error code " + lhingsErrorCode + " - "
								+ errorMessage);
			default:
				throw new LhingsException("Lhings API error. HTTP status " + status + ". Lhings error code " + lhingsErrorCode + " - "
						+ errorMessage);
			}
		} else if (status != 200 && status != 201) {
			// HTTP error
			throw new LhingsException("HTTP error. Code: " + status + " - " + response.getStatusMessage());
		}
	}

	private static HttpResponse executeRequest(HttpRequestBase request)
			throws IOException {
		CloseableHttpClient hc = HttpClients.createDefault();
		CloseableHttpResponse response = hc.execute(request);
		HttpResponse httpResponse = new HttpResponse();
		httpResponse.setStatusCode(response.getStatusLine().getStatusCode());
		httpResponse.setStatusMessage(response.getStatusLine()
				.getReasonPhrase());
		// ResponseHandler<String> handler = new BasicResponseHandler();
		// String responseBody = handler.handleResponse(response);
		httpResponse
				.setResponseBody(EntityUtils.toString(response.getEntity()));
		return httpResponse;
	}

	public static Map<String, Object> getStatus(LhingsDevice lhingsDevice,
			String uuid) throws LhingsException, IOException {
		String url = LHINGS_V1_API_PREFIX + "devices/" + uuid + "/states";
		String json = executeGet(url, lhingsDevice.apiKey());
		Map<String, Object> returnValue = new HashMap<String, Object>();
		JSONArray array = new JSONArray(json);
		for (int j = 0; j < array.length(); j++) {
			JSONObject statusComponent = array.getJSONObject(j);
			String statusCompName = statusComponent.getString("name");
			String statusCompType = statusComponent.getString("type");
			Object statusCompValue;
			if (statusCompType.equals("integer"))
				statusCompValue = statusComponent
                                        .getInt("value");
			else if (statusCompType.equals("float"))
				statusCompValue = (float) statusComponent
                                        .getDouble("value");
			else if (statusCompType.equals("timestamp"))
				statusCompValue = new Date(
						(long) statusComponent.getInt("value") * 1000);
			else if (statusCompType.equals("boolean"))
				statusCompValue = statusComponent
                                        .getBoolean("value");
			else
				statusCompValue = statusComponent.getString("value");
			returnValue.put(statusCompName, statusCompValue);
		}
		return returnValue;
	}

	/**
	 * Stores data in Lhings.
	 * 
	 * @param lhingsDevice
	 * @param statusComponentValues
	 * @return true if data was sucessfully stored, false otherwise
	 * @throws IOException
	 * @throws LhingsException
	 */
	public static boolean storeStatus(LhingsDevice lhingsDevice,
			Map<String, Object> statusComponentValues) throws IOException,
			LhingsException {
		String url = LHINGS_V1_API_PREFIX + "devices/" + lhingsDevice.uuid()
				+ "/states";
		String apikey = lhingsDevice.apiKey();
		String requestBody = new JSONObject(statusComponentValues).toString();
		String response = executePost(apikey, url, requestBody);
		int responseStatus = new JSONObject(response).getInt("responseStatus");
		if (responseStatus == 200)
			return true;
		else
			return false;
	}

	public static String requestAction(LhingsDevice deviceRequester,
			String uuidDevicePerformer, String actionName,
			Map<String, Object> arguments) throws IOException, LhingsException {
		String url = LHINGS_V1_API_PREFIX + "devices/" + uuidDevicePerformer
				+ "/actions/" + actionName;
		String apikey = deviceRequester.apiKey();
		String postBody = null;
		if (arguments == null || arguments.size() == 0) {
			// no arguments
			postBody = "[]";
		} else {
			List<NameValueBean> jsonObject = new ArrayList<NameValueBean>();
			for (String key : arguments.keySet()) {
				jsonObject.add(new NameValueBean(key, arguments.get(key)));
			}
			postBody = new JSONArray(jsonObject).toString();
		}
		
		return executePost(apikey, url, postBody);
	}

}
