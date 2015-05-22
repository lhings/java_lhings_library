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
package com.lhings.java;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.lhings.java.annotations.Action;
import com.lhings.java.annotations.DeviceInfo;
import com.lhings.java.annotations.Event;
import com.lhings.java.annotations.Payload;
import com.lhings.java.annotations.StatusComponent;
import com.lhings.java.exception.ActionExecutionException;
import com.lhings.java.exception.DeviceDoesNotExistException;
import com.lhings.java.exception.DeviceUnreachableException;
import com.lhings.java.exception.InitializationException;
import com.lhings.java.exception.LhingsException;
import com.lhings.java.exception.UnauthorizedException;
import com.lhings.java.http.WebServiceCom;
import com.lhings.java.logging.LhingsLogger;
import com.lhings.java.model.Argument;
import com.lhings.java.model.Device;
import com.lhings.java.model.MethodOrFieldToInstanceMapper;
import com.lhings.java.pushprotocol.ListenerThread;
import com.lhings.java.stun.LyncnatProtocol;
import com.lhings.java.stun.STUNMessage;
import com.lhings.java.stun.STUNMessageFactory;

/**
 * This abstract class is the base class for all the Java devices. Any device
 * implemented with this library must extend LhingsDevice.
 *
 * Developers must override the method setup(), called once by the library to
 * perform all initialization tasks required by the implementation, and the
 * method loop(), called repeatedly by the library to execute the logic of the
 * device. Also, methods annotated with null
 * {@link com.lhings.java.annotations.Action},
 * {@link com.lhings.java.annotations.Event} and
 * {@link com.lhings.java.annotations.StatusComponent} must be implemented to
 * define the <a href="http://support.lhings.com/Getting-started.html"> actions,
 * events and status components </a> of the device.
 *
 * The frequency at which the <code>loop()</code> method is executed can be set
 * using the method <code>setLoopFrequency(float frequency)</code>.
 *
 * This class also provides convenience methods to interact with and obtain
 * information about other devices that belong to the same Lhings account as
 * this one. See methods <code>getDevices()</code>, <code>requestAction()</code>
 * , <code>storeStatus()</code>, <code>getStatus()</code>, and
 * <code>statusRetrieve()</code>.
 */
public abstract class LhingsDevice implements Runnable {

	protected static final Logger log = LhingsLogger.getLogger();
	protected static final Properties uuids;
	private static final File fileUuids = new File("uuid.list");
	private static final long TIME_BETWEEN_KEEPALIVES_MILLIS = 30000;
	private static final long INITIAL_TIME_BETWEEN_STARTSESSION_RETRIES_MILLIS = 1000;
	private static final String DEFAULT_DEVICE_TYPE = "lhings-java";
	private static final String VERSION_STRING = "Lhings Java SDK v2.3 - ja010";

	private final Map<String, MethodOrFieldToInstanceMapper> actionMethods = new HashMap<String, MethodOrFieldToInstanceMapper>();
	private final Map<String, com.lhings.java.model.Action> actionDefinitions = new HashMap<String, com.lhings.java.model.Action>();
	private final Map<String, MethodOrFieldToInstanceMapper> statusFields = new HashMap<String, MethodOrFieldToInstanceMapper>();
	private final Map<String, com.lhings.java.model.StatusComponent> statusDefinitions = new HashMap<String, com.lhings.java.model.StatusComponent>();
	private final List<String> eventDefinitions = new ArrayList<String>();

	static {
		System.out.println(VERSION_STRING);
		uuids = new Properties();
		try {
			if (!fileUuids.exists()) {
				log.info("uuid.list file does not exist, creating a new one from scratch.");
				updateProperties();
			} else {
				FileReader reader = new FileReader(fileUuids);
				uuids.load(reader);
			}
		} catch (IOException ex) {
			log.fatal("Device list file could not be opened. Exiting.");
			System.exit(1);
		}
	}

	private String apiKey;

	private int port;

	private String name;

	private String username;

	private ListenerThread postman;

	private String uuid;

	private boolean running = false;

	private String jsonDescriptor;

	private float loopFrequency = 10;

	private List<Feature> features = new ArrayList<Feature>();

	private Map<String, Integer> incrementalCountersForName = new HashMap<String, Integer>();

	/**
	 * Creates a new device in Lhings associated to the account with the given
	 * username and password. Username and password will not be stored in any
	 * way, they will be used only at runtime. If the device is created for the
	 * first time, it will be registered in Lhings and its UUID will be stored
	 * for the next time it is launched.
	 *
	 * @param username
	 *            Username of the Lhings account.
	 * @param apikey
	 *            The apikey of the account, or its password. Any of them will
	 *            work.
	 * @param deviceName
	 *            The name of the device.
	 * @throws IOException
	 *             If a network or connectivity error occurs during
	 *             initialization.
	 * @throws LhingsException
	 */
	public LhingsDevice(String username, String apikey, String deviceName) throws IOException, LhingsException {
		this(username, apikey, (int) (Math.random() * 50000) + 1027, deviceName, null);
	}

	/**
	 * Creates a new device in Lhings associated to the account with the given
	 * username and password. Username and password will not be stored in any
	 * way, they will be used only at runtime. If the device is created for the
	 * first time, it will be registered in Lhings and its UUID will be stored
	 * for the next time it is launched.
	 * 
	 * @param username
	 *            Username of the Lhings account.
	 * @param apikey
	 *            The apikey of the account, or its password. Any of them will
	 *            work.
	 * @param deviceName
	 *            The name of the device.
	 * @param features
	 *            The list of features that must be added to the device before
	 *            starting it.
	 * @throws IOException
	 *             If a network or connectivity error occurs during
	 *             initialization.
	 * @throws LhingsException
	 */
	public LhingsDevice(String username, String apikey, String deviceName, List<Feature> features) throws IOException, LhingsException {
		this(username, apikey, (int) (Math.random() * 50000) + 1027, deviceName, features);
	}

	/**
	 * Creates a new device in Lhings associated to the account with the given
	 * username and password. Username and password will not be stored in any
	 * way, they will be used only at runtime. If the device is created for the
	 * first time, it will be registered in Lhings and its UUID will be stored
	 * for the next time it is launched.
	 *
	 * @param username
	 *            Username of the Lhings account.
	 * @param apikey
	 *            The apikey of the account, or its password. Any of them will
	 *            work.
	 * @param port
	 *            The port the device will use to connect to the Internet for
	 *            non-http communications.
	 * @param deviceName
	 *            The name of the device.
	 * @throws IOException
	 *             If a network or connectivity error occurs during
	 *             initialization.
	 * @throws LhingsException
	 */
	public LhingsDevice(String username, String apikey, int port, String deviceName, List<Feature> features) throws IOException, LhingsException {
		if (apikey.matches("^[0-9abcdef]{8}?-[0-9abcdef]{4}?-[0-9abcdef]{4}?-[0-9abcdef]{4}?-[0-9abcdef]{12}?"))
			this.apiKey = apikey;
		else
			this.apiKey = WebServiceCom.getApiKey(username, apikey);
		this.port = port;
		this.name = deviceName;
		this.username = username;
		for (Feature feature : features) {
			feature.parentDevice = this;
		}
		if (features != null)
			this.features.addAll(features);
		initDevice();
	}

	/**
	 * Performs all operations needed before starting the device
	 *
	 * @throws LhingsException
	 * @throws IOException
	 */
	private void initDevice() throws LhingsException, IOException {
		uuid = uuids.getProperty(name);
		if (uuid == null) {
			// device is not registered, register it
			uuid = WebServiceCom.registerDevice(this);
			uuids.setProperty(name, uuid);
			log.info("Device was successfully registered with name " + name);
			updateProperties();
		}
		autoconfigure();
	}

	/**
	 * This method detects the actions, events and status components of the
	 * device using reflection, builds the descriptor of the device and stores
	 * it in the field jsonDescriptor.
	 *
	 * @throws InitializationException
	 */
	private void autoconfigure() throws InitializationException {
		Class<?> deviceClass = this.getClass();
		Map<String, Object> deviceDescriptor = new HashMap<String, Object>();
		// List<com.lhings.java.model.Action> actionList = new
		// ArrayList<com.lhings.java.model.Action>();
		List<com.lhings.java.model.Event> eventList = new ArrayList<com.lhings.java.model.Event>();
		// inspect class to identify descriptor fields and status components
		deviceDescriptor.put("actionList", actionDefinitions.values());
		deviceDescriptor.put("stateVariableList", statusDefinitions.values());
		deviceDescriptor.put("eventList", eventList);

		DeviceInfo info = deviceClass.getAnnotation(DeviceInfo.class);
		if (info != null) {
			deviceDescriptor.put("modelName", info.modelName());
			deviceDescriptor.put("manufacturer", info.manufacturer());
			deviceDescriptor.put("deviceType", info.deviceType());
			deviceDescriptor.put("serialNumber", info.serialNumber());
		} else {
			deviceDescriptor.put("modelName", "");
			deviceDescriptor.put("manufacturer", "");
			deviceDescriptor.put("deviceType", DEFAULT_DEVICE_TYPE);
			deviceDescriptor.put("serialNumber", "");
		}

		Field[] fields = deviceClass.getDeclaredFields();
		List<MethodOrFieldToInstanceMapper> methodFieldList = new ArrayList<MethodOrFieldToInstanceMapper>();
		for (Field field : fields) {
			methodFieldList.add(new MethodOrFieldToInstanceMapper(field, this));
		}

		for (Feature feature : features) {
			fields = feature.getClass().getDeclaredFields();
			for (Field field : fields) {
				methodFieldList.add(new MethodOrFieldToInstanceMapper(field, feature));
			}
		}

		for (MethodOrFieldToInstanceMapper fieldMapper : methodFieldList) {
			Field field = fieldMapper.getField();
			// discover status components and add them to descriptor
			if (field.isAnnotationPresent(StatusComponent.class)) {
				StatusComponent statusComp = field.getAnnotation(StatusComponent.class);
				String statusComponentName = statusComp.name();
				String statusComponentType;
				try {
					statusComponentType = tellLhingsType(field.getType().getName());
				} catch (InitializationException ex) {
					// rethrow with appropriate message
					throw new InitializationException(
							"Initialization failed for device with uuid "
									+ uuid
									+ ". Type "
									+ field.getType().getName()
									+ " is not allowed for field "
									+ field.getName()
									+ ". Methods annotated with @StatusComponent can only have parameters of the following types: int, float, double, boolean, String and java.util.Date.");
				}

				// if no name was provided for field, it defaults to the
				// name used to declare it in source code
				boolean validStatusComponentName = statusComponentName.matches("^[a-zA-Z0-9_]*$");
				if (!validStatusComponentName) {
					log.warn("\"" + statusComponentName
							+ "\" is not a valid name for a status component. Only alphanumeric and underscore characters are allowed. Taking field name \""
							+ field.getName() + "\" as status component name.");
				}
				if (statusComponentName.isEmpty() || !validStatusComponentName) {
					statusComponentName = field.getName();
				}
				// check if name is not already assigned
				if (statusFields.get(statusComponentName) != null) {
					statusComponentName += incrementAndGetCounterForName(statusComponentName);
				}

				statusDefinitions.put(statusComponentName, new com.lhings.java.model.StatusComponent(statusComponentName, statusComponentType));
				statusFields.put(statusComponentName, fieldMapper);
			}

			// discover events and add them to descriptor
			if (field.isAnnotationPresent(Event.class)) {
				Event event = field.getAnnotation(Event.class);
				String eventName = event.name();
				boolean validEventComponentName = eventName.matches("^[a-zA-Z0-9_]*$");
				if (!validEventComponentName) {
					log.warn("\"" + eventName
							+ "\" is not a valid name for an event. Only alphanumeric and underscore characters are allowed. Taking field name \""
							+ field.getName() + "\" as event name.");
				}
				
				// default event name is the name of the field
				if (eventName.isEmpty() || !validEventComponentName) 
				{
					eventName = field.getName();
				}

				// check event name is not already assigned
				String originalName = null;
				if (eventDefinitions.contains(eventName)) {
					originalName = eventName;
					eventName += incrementAndGetCounterForName(eventName);
				}
				if (fieldMapper.getInstance().getClass() == Feature.class && originalName != null) {
					((Feature) fieldMapper.getInstance()).setAliasForEvent(originalName, eventName);
				}
				com.lhings.java.model.Event eventToAdd = new com.lhings.java.model.Event(eventName);

				String[] componentNames = event.component_names();
				String[] componentTypes = event.component_types();
				if (componentNames.length != componentTypes.length) {
					log.warn("Payload component list for event \"" + eventName + "\" is not valid: wrong number of component types provided.");
				} else {
					for (int j = 0; j < componentNames.length; j++) {
						eventToAdd.getComponents().add(new Argument(componentNames[j], componentTypes[j]));
					}
				}

				eventList.add(eventToAdd);
				eventDefinitions.add(eventName);
			}

		}
		// inspect class methods to identify possible actions
		Method[] methods = deviceClass.getMethods();
		methodFieldList.clear();
		for (Method method : methods) {
			methodFieldList.add(new MethodOrFieldToInstanceMapper(method, this));
		}

		for (MethodOrFieldToInstanceMapper methodMapper : methodFieldList) {
			Method method = methodMapper.getMethod();
			if (method.isAnnotationPresent(Action.class)) {
				com.lhings.java.model.Action modelAction = autoconfigureAction(methodMapper);
				String actionName = modelAction.getName();

				actionDefinitions.put(actionName, modelAction);
			}
		}

		jsonDescriptor = new JSONObject(deviceDescriptor).toString();
	}

	private com.lhings.java.model.Action autoconfigureAction(MethodOrFieldToInstanceMapper methodMapper) throws InitializationException {
		Method actionMethod = methodMapper.getMethod();
		Action action = actionMethod.getAnnotation(Action.class);
		String actionName, actionDescription;
		String[] argumentNames;
		actionName = action.name();
		actionDescription = action.description();
		boolean invalidActionName = !actionName.matches("^[a-zA-Z0-9_]*$");
		if (actionName.isEmpty() || invalidActionName) {
			if (invalidActionName)
				log.warn("\"" + actionName
						+ "\" is not a valid name for an action. Only alphanumeric and underscore characters are allowed. Taking method name \""
						+ actionMethod.getName() + "\" as action name.");
			actionName = actionMethod.getName();
		}

		// check action name is not already assigned
		if (actionMethods.get(actionName) != null) {
			actionName += incrementAndGetCounterForName(actionName);
		}

		Annotation[][] parameterAnnotations = actionMethod.getParameterAnnotations();
		if (parameterAnnotations.length == 1 && parameterAnnotations[0].length == 1) {
			// method has only one parameter and it is annotated, check if
			// annotation is @Payload
			if (parameterAnnotations[0][0].annotationType().equals(Payload.class)) {
				com.lhings.java.model.Action returnAction = new com.lhings.java.model.Action(actionName, "", new ArrayList<Argument>(), null);
				returnAction.setPayloadNeeded(true);
				// store method so that it is easier to access later
				if (!actionMethods.keySet().contains(actionName)) {
					actionMethods.put(actionName, methodMapper);
				} else {
					throw new InitializationException("Duplicated action names: methods " + actionMethod.getName() + " and "
							+ actionMethods.get(actionName).getMethod().getName() + " were both mapped to the same action name " + actionName + ".");
				}
				return returnAction;
			}
		}

		argumentNames = action.argumentNames();
		Class<?>[] arguments = actionMethod.getParameterTypes();
		if (argumentNames.length != arguments.length) {
			throw new InitializationException("Initialization failed for device with uuid " + uuid + ". Names were provided for " + argumentNames.length
					+ " arguments but method " + actionMethod.getName() + " declares " + arguments.length + " arguments.");
		}
		List<Argument> modelArguments = new ArrayList<Argument>();
		for (int j = 0; j < arguments.length; j++) {
			String declaredType = arguments[j].getName();
			String argumentType;
			try {
				argumentType = tellLhingsType(declaredType);
			} catch (InitializationException ex) {
				// rethrow with appropriate message
				throw new InitializationException(
						"Initialization failed for device with uuid "
								+ uuid
								+ ". Type "
								+ declaredType
								+ " is not allowed for parameters in method "
								+ actionMethod.getName()
								+ ". Methods annotated with @Action can only have parameters of the following types: int, float, double, boolean, String and java.util.Date.");
			}

			modelArguments.add(new Argument(argumentNames[j], argumentType));
		}

		// store method so that it is easier to access later
		if (!actionMethods.keySet().contains(actionName)) {
			actionMethods.put(actionName, methodMapper);
		} else {
			throw new InitializationException("Duplicated action names: methods " + actionMethod.getName() + " and "
					+ actionMethods.get(actionName).getMethod().getName() + " were both mapped to the same action name " + actionName + ".");
		}

		com.lhings.java.model.Action returnAction = new com.lhings.java.model.Action(actionName, actionDescription, modelArguments, null);

		return returnAction;
	}

	/**
	 * Returns the Lhings type that corresponds to a given Java type.
	 *
	 * @param declaredType
	 *            String representation of the fully qualified name of the type.
	 * @return The string representation of one of the Lhings types (boolean,
	 *         integer, float, string or timestamp).
	 * @throws InitializationException
	 */
	private String tellLhingsType(String declaredType) throws InitializationException {
		if (declaredType.equals("java.lang.String")) {
			return "string";
		} else if (declaredType.equals("java.lang.Integer") || declaredType.equals("int")) {
			return "integer";
		} else if (declaredType.equals("java.lang.Float") || declaredType.equals("float") || declaredType.equals("java.lang.Double")
				|| declaredType.equals("double")) {
			return "float";
		} else if (declaredType.equals("java.lang.Boolean") || declaredType.equals("boolean")) {
			return "boolean";
		} else if (declaredType.equals("java.util.Date")) {
			return "timestamp";
		} else {
			throw new InitializationException();
		}
	}

	/**
	 * Start the device. The device starts session in Lhings and executes the
	 * loop() method periodically.
	 * 
	 * @throws LhingsException
	 */
	public void start() throws LhingsException {
		setup();
		for (Feature feature : features)
			feature.setup();
		running = true;
		postman = ListenerThread.getInstance(port, this);
		(new Thread(this)).start();
		log.info("Device started");
	}

	/**
	 * Stops the device
	 */
	public void stop() {
		running = false;
		postman.stop();
		try {
			WebServiceCom.endSession(this);
		} catch (IOException e) {
			log.warn("Session could not be ended due to a networking failure");
		} catch (LhingsException e) {
			log.warn("Session could not be ended due to bad credentials, bad request, or the device was deleted on server side while online");
		}
	}

	public void run() {
		Thread.currentThread().setName("thread-main-" + uuid);
		sendDescriptor();
		startSession();
		sendKeepAlive();
		long lastKeepAliveSentTime = System.currentTimeMillis();
		long lastLoopExecutionTime = 0L;
		long timeBetweenConsecutiveLoopExecutionsMillis = (long) (1000 / loopFrequency);
		while (running) {
			long now = System.currentTimeMillis();
			if ((now - lastKeepAliveSentTime) > TIME_BETWEEN_KEEPALIVES_MILLIS) {
				sendKeepAlive();
				lastKeepAliveSentTime = now;
			}

			now = System.currentTimeMillis();
			if (now - lastLoopExecutionTime > timeBetweenConsecutiveLoopExecutionsMillis) {
				lastLoopExecutionTime = now;
				loop();
				for (Feature feature : features){
					feature.loopEvery();
				}
			}

			try {
				processMessage();
			} catch (ActionExecutionException e) {
				e.printStackTrace();
			}
		}
		log.info("Successfully stopped device.");
	}

	private void processMessage() throws ActionExecutionException {
		byte[] rawMessage = postman.receive();
		if (rawMessage == null) {
			return;
		}
		log.debug("Processing message");
		STUNMessage message = STUNMessage.getSTUNMessage(rawMessage);
		if (message == null) {
			return;
		}

		switch (message.getMethod()) {
		case LyncnatProtocol.mAction:
			log.debug("Received action message");
			performAction(message);
			break;
		case LyncnatProtocol.mStatusRequest:
			log.debug("Received status request message");
			answerStatus(message);
			break;
		}
	}

	private void answerStatus(STUNMessage message) {
		// build inputs for STUNMessageFactory.buildArgumentsAttribute
		List<Argument> arguments = new ArrayList<Argument>();
		Map<String, Object> argValues = new HashMap<String, Object>();
		for (String statusCompName : statusDefinitions.keySet()) {
			com.lhings.java.model.StatusComponent statusComponent = statusDefinitions.get(statusCompName);
			Argument arg = new Argument(statusCompName, statusComponent.getType());
			arguments.add(arg);
			// retrieve current value of the corresponding field
			MethodOrFieldToInstanceMapper fieldMapper = statusFields.get(statusCompName);
			Field field = fieldMapper.getField();
			field.setAccessible(true);
			try {
				Object value = field.get(fieldMapper.getInstance());
				argValues.put(statusCompName, value);
			} catch (IllegalArgumentException e) {
				postman.send(STUNMessageFactory.getInstance(apiKey)
						.getErrorResponse(message, LyncnatProtocol.errNotAvailable, "Value for " + statusCompName + " could not be retrieved", username)
						.getBytes());
				log.error("Value for " + statusCompName + " could not be retrieved");
				return;
			} catch (IllegalAccessException e) {
				postman.send(STUNMessageFactory.getInstance(apiKey)
						.getErrorResponse(message, LyncnatProtocol.errNotAvailable, "Value for " + statusCompName + " could not be retrieved", username)
						.getBytes());
				log.error("Value for " + statusCompName + " could not be retrieved");
				return;
			}
		}
		// build arguments attribute of the response
		byte[] argsAttr = STUNMessageFactory.buildArgumentsAttribute(arguments, argValues);
		Map<Integer, byte[]> attrs = new HashMap<Integer, byte[]>();
		attrs.put(LyncnatProtocol.attrArguments, argsAttr);
		STUNMessage response = STUNMessageFactory.getInstance(apiKey).getSuccessResponse(username, message, true, attrs);
		// send response
		postman.send(response.getBytes());
	}

	/**
	 * Sends an event without payload to Lhings.
	 *
	 * @param name
	 *            The name of the event. If no such event is defined for this
	 *            device no event is sent but a warning is shown in the log of
	 *            the application.
	 */
	protected void sendEvent(String name) {
		sendEvent(name, "");
	}

	/**
	 * Sends an event with payload to Lhings.
	 *
	 * @param name
	 *            The name of the event. If no such event is defined for this
	 *            device no event is sent but a warning is shown in the log of
	 *            the application.
	 * @param payload
	 *            The payload of the event.
	 */
	protected void sendEvent(String name, String payload) {
		// check event is an allowed one
		if (!eventDefinitions.contains(name)) {
			log.warn("Device is not capable of sending event named " + name);
			return;
		}

		try {
			WebServiceCom.sendEvent(this, name, payload);
			log.info("Sent event " + name);
		} catch (IOException e) {
			log.warn(e.getMessage());
		} catch (DeviceDoesNotExistException ex) {
			log.fatal("Unable to send event "
					+ name
					+ ". Device with uuid is not recognized by the server. Did you delete it on Lhings? Remove the appropriate entry from file uuid.list and try again. Exiting.");
		} catch (UnauthorizedException ex) {
			log.fatal("Unauthorized. Unable to send event " + name
					+ " to server: provided credentials (either api key or username/password) are not valid. Exiting.");
		} catch (LhingsException e) {
			log.warn(e.getMessage());
		}
	}

	private void performAction(STUNMessage message) throws ActionExecutionException {
		byte[] rawActionName = message.getAttribute(LyncnatProtocol.attrName);
		if (rawActionName == null) {
			log.debug("Action not executed: name of action missing in received message");
			return;
		}
		String actionName = new String(rawActionName, Charset.forName("utf-8"));
		Method actionMethod = actionMethods.get(actionName).getMethod();
		if (actionMethod == null) {
			log.debug("Action not executed: this device has no action named " + actionName + ".");
			return;
		}
		actionMethod.setAccessible(true);
		com.lhings.java.model.Action actionDefinition = actionDefinitions.get(actionName);
		String payload = null;
		if (actionDefinition.isPayloadNeeded()) {
			byte[] rawPayload = message.getAttribute(LyncnatProtocol.attrPayload);
			if (rawPayload == null) {
				log.warn("Action " + actionName + " not executed: action requires a payload but it was not provided.");
				return;
			}
			payload = new String(rawPayload, Charset.forName("utf-8"));
			Object[] args = new Object[1];
			args[0] = payload;

		}
		int numExpectedArguments = actionMethod.getParameterTypes().length;
		if (numExpectedArguments == 0 || (numExpectedArguments == 1 && actionDefinition.isPayloadNeeded())) {
			// action has no arguments, perform action now
			Object[] args;
			if (actionDefinition.isPayloadNeeded()) {
				args = new Object[1];
				args[0] = payload;
			} else {
				args = null;
			}
			try {
				actionMethod.invoke(this, args);
			} catch (IllegalAccessException e) {
				log.error("Error trying to invoke action " + actionName + ".", e);
				return;
			} catch (IllegalArgumentException e) {
				log.error("Error trying to invoke action " + actionName + ", wrong arguments.", e);
				return;
			} catch (InvocationTargetException e) {
				throw new ActionExecutionException(e);
			}
			// send success response
			postman.send(STUNMessageFactory.getInstance(apiKey).getSuccessResponse(message).getBytes());
			return;
		}

		byte[] rawArgs = message.getAttribute(LyncnatProtocol.attrArguments);
		if (rawArgs == null) {
			log.warn("Action " + actionName + " not executed: malformed action message, arguments attribute was missing.");
			return;
		}

		Map<String, Object> argumentValues = STUNMessageFactory.processArgumentsAttribute(rawArgs, actionDefinitions.get(actionName));
		if (argumentValues.keySet().size() != numExpectedArguments) {
			log.warn("Action " + actionName + " not executed: expected " + numExpectedArguments + " but " + argumentValues.keySet().size() + " where provided.");
			return;
		}
		Object[] args = generateArgsForActionInvocation(actionName, actionMethod.getParameterTypes(), argumentValues);
		if (args == null) {
			return;
		}
		try {
			actionMethod.invoke(this, args);
		} catch (IllegalAccessException e) {
			log.error("Error trying to invoke action " + actionName + ".", e);
			return;
		} catch (IllegalArgumentException e) {
			log.error("Error trying to invoke action " + actionName + ", wrong arguments.", e);
			return;
		} catch (InvocationTargetException e) {
			throw new ActionExecutionException(e);
		}

		// send success response
		postman.send(STUNMessageFactory.getInstance(apiKey).getSuccessResponse(message).getBytes());
	}

	private Object[] generateArgsForActionInvocation(String actionName, Class<?>[] parameterTypes, Map<String, Object> argumentValues) {
		com.lhings.java.model.Action action = actionDefinitions.get(actionName);
		if (action == null) {
			log.warn("Action " + actionName + " not executed: no definition for it (check your device descriptor).");
			return null;
		}
		Object[] args = new Object[parameterTypes.length];
		List<Argument> arguments = action.getInputs();
		for (int j = 0; j < arguments.size(); j++) {
			args[j] = argumentValues.get(arguments.get(j).getName());
		}
		return args;
	}

	private void sendKeepAlive() {
		STUNMessage stm = STUNMessageFactory.getInstance(apiKey).getKeepAliveMessage(username, uuid);
		log.debug("Sending keepalive");
		postman.send(stm.getBytes());
	}

	private void sendDescriptor() {
		try {
			WebServiceCom.sendDescriptor(this, jsonDescriptor);
			log.info("Descriptor sent successfully");
		} catch (IOException e) {
			log.warn(e.getMessage());
		} catch (DeviceDoesNotExistException ex) {
			log.fatal("Unable to connect device to Lhings. Device is not recognized by the server. Did you delete it on Lhings? Remove the appropriate entry from file uuid.list and try again. Exiting.");
			this.stop();
		} catch (UnauthorizedException ex) {
			log.fatal("Unauthorized. Unable to connect device to server: provided credentials (either api key or username/password) are not valid. Exiting.");
			this.stop();
		} catch (LhingsException e) {
			log.warn(e.getMessage());
		}
	}

	/**
	 * Starts session. If session cannot be started, the method retries doubling
	 * the waiting time between tries each time. This method blocks until
	 * session is started successfully.
	 *
	 */
	private void startSession() {
		long waitingTime = INITIAL_TIME_BETWEEN_STARTSESSION_RETRIES_MILLIS;
		boolean started = false;
		while (!started) {
			try {
				WebServiceCom.startSession(this);
				started = true;
			} catch (IOException e) {
				log.warn(e.getMessage());
			} catch (DeviceDoesNotExistException ex) {
				log.fatal("Unable to connect device to Lhings. Device is not recognized by the server. Did you delete it on Lhings? Remove the appropriate entry from file uuid.list and try again. Exiting.");
				this.stop();
				return;
			} catch (UnauthorizedException ex) {
				log.fatal("Unauthorized. Unable to connect device with uuid to server: provided credentials (either api key or username/password) are not valid. Exiting.");
				this.stop();
				return;
			} catch (LhingsException e) {
				log.warn(e.getMessage());
			}
			if (!started) {
				// not started but error is recoverable, try again
				log.warn("Unable to start session. Retrying again in " + waitingTime / 1000 + " seconds...");
				waitingTime = 2 * waitingTime;
			}
			try {
				Thread.sleep(waitingTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		log.info("Device started session succesfully.");
	}

	/**
	 * Returns true if the username and password combination provided was
	 * correct and the device was able to start session in Lhings.
	 */
	public Boolean isLogged() {
		return true;
	}

	/**
	 * Returns true if the device was successful in setting up a permanent
	 * communication channel with the Lhings server.
	 */
	public Boolean isDeviceConnected() {
		return true;
	}

	/**
	 * Override this method if you want to specify your own manufacturer.
	 * Default manufacturer is "Lhings"
	 *
	 * @return
	 */
	public String getManufacturer() {
		return "Lhings";
	}

	/**
	 * Override this method if you want to specify your own model name. Default
	 * model name is "prototype".
	 *
	 * @return
	 */
	public String getModelName() {
		return "prototype";
	}

	/**
	 * Override this method if you want to specify your own serial number
	 * Default serial number is "000001"
	 *
	 * @return
	 */
	public String getSerialNumber() {
		return "000001";
	}

	/**
	 * Override this method if you want to specify your own device type Default
	 * device type is "javavirtualdevice"
	 *
	 * @return
	 */
	public String getType() {
		return "javavirtualdevice";
	}

	/**
	 * Override this method if you want to specify your own device version
	 * Default device version is "1"
	 *
	 * @return
	 */
	public String getVersion() {
		return "1";
	}

	/**
	 * This method is called to close the session with lhings.
	 */
	public void logout() {
	}

	/**
	 * Returns the Api-Key the device is using to connect with Lhings.
	 */
	public String apiKey() {
		return apiKey;
	}

	/**
	 * Returns the unique UUID Lhings assigned to this device.
	 */
	public String uuid() {
		return uuid;
	}

	/**
	 * Implementation must override this method and put in it all the
	 * initialization code required by device.
	 */
	public abstract void setup();

	/**
	 * Implementations must override this method and put in it the device logic.
	 * This method will be called periodically by the SDK. The frequency at
	 * which it is called is set by the method setLoopFrequency().
	 */
	public abstract void loop();

	/**
	 * Used to retrieve the port used by this device to communicate with Lhings.
	 *
	 * @return The port number.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Returns the name of the device.
	 *
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the username of the account to which the device belongs.
	 *
	 * @return
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Returns the number of times per second the method <code>loop()</code> is
	 * executed by the SDK.
	 *
	 * @return
	 */
	public float getLoopFrequency() {
		return loopFrequency;
	}

	/**
	 * Sets the number of times per second the method <code>loop()</code> is
	 * executed by the SDK. The SDK will do its best to execute the
	 * <code>loop()</code> at the frequency requested, but it does not guarantee
	 * neither precision nor constant frequency. The default value is 10 Hz.
	 */
	public void setLoopFrequency(float loopFrequency) {
		this.loopFrequency = loopFrequency;
	}

	/**
	 * This method returns a list of all the other devices that belong to the
	 * same account as the calling device. Since this method communicates with
	 * Lhings over the Internet, the call is blocking. If this is an issue
	 * consider executing this call in another thread.
	 *
	 * @return A list of Device instances, with the following information: name,
	 *         uuid and type of the device, and online status.
	 * @throws IOException
	 * @throws LhingsException
	 */
	public List<Device> getDevices() throws LhingsException, IOException {
		return WebServiceCom.deviceList(this);
	}

	/**
	 * Retrieves the status of the given device.
	 *
	 * @param uuid
	 *            The uuid of the device whose status is to be retrieved.
	 * @return A map whose keys are the names of the status components of the
	 *         device, and whose values are the values of those status
	 *         components. If the given uuid is the same as that of the calling
	 *         device, null is returned. If the device whose status is requested
	 *         is offline, only one the online status component is returned.
	 * @throws DeviceDoesNotExistException
	 *             If no device exists with that uuid.
	 * @throws DeviceUnreachableException
	 *             If the device could not be contacted over the internet.
	 * @throws UnauthorizedException
	 *             If the device exists but the calling device is not authorized
	 *             to access its status.
	 * @throws LhingsException
	 * @throws IOException
	 */
	public Map<String, Object> getStatus(String uuid) throws DeviceDoesNotExistException, DeviceUnreachableException, UnauthorizedException, LhingsException,
			IOException {
		if (uuid.equals(this.uuid)) {
			return null;
		}

		return WebServiceCom.getStatus(this, uuid);
	}

	/**
	 * Stores the value of all the status components of this device in Lhings.
	 *
	 * @throws LhingsException
	 * @throws IOException
	 */
	public void storeStatus() throws IOException, LhingsException {
		Map<String, Object> statusComponentValues = new HashMap<String, Object>();
		Set<String> statusComponentNames = statusFields.keySet();
		for (String statusComponentName : statusComponentNames) {
			MethodOrFieldToInstanceMapper fieldMapper = statusFields.get(statusComponentName);
			Field statusComponent = fieldMapper.getField();
			statusComponent.setAccessible(true);
			try {
				Object statusComponentValue = statusComponent.get(fieldMapper.getInstance());
				statusComponentValues.put(statusComponentName, statusComponentValue);
			} catch (IllegalArgumentException e) {
				log.warn("Could not store status for component called " + statusComponentName + ". This device does not have such status component.");
			} catch (IllegalAccessException e) {
				log.warn("Could not store status for component called " + statusComponentName + ". " + e.getMessage());
			}
		}
		if (statusComponentValues.isEmpty()) {
			return;
		}

		if (WebServiceCom.storeStatus(this, statusComponentValues)) {
			log.debug("Successfully stored status");
		} else {
			log.warn("Status could not be stored");
		}
	}

	/**
	 * Request another device to perform one of its actions.Both the action name
	 * and argument names must have been declared by the other device in its
	 * descriptor file.
	 *
	 * @param uuid
	 *            The uuid of the device that will perform the action.
	 * @param actionName
	 *            The name of the action to be performed.
	 * @param arguments
	 *            A Map with the name of the arguments of the action as keys and
	 *            the values of those arguments as values.
	 * @return
	 * @throws IOException
	 * @throws LhingsException
	 */
	public String requestAction(String uuid, String actionName, Map<String, Object> arguments) throws IOException, LhingsException {
		log.debug("Requesting action " + actionName + " to device " + uuid);
		String json = WebServiceCom.requestAction(this, uuid, actionName, arguments);
		return json;
	}

	private static void updateProperties() {
		try {
			FileWriter writer = new FileWriter(fileUuids);
			uuids.store(writer, "Java Lhings SDK device list - last modified " + new Date());
			writer.close();
		} catch (IOException ex) {
			log.error("Device list properties file could not be saved.");
		}
	}

	private int incrementAndGetCounterForName(String name) {
		Integer counter = incrementalCountersForName.get(name);
		if (counter == null) {
			counter = 0;
			incrementalCountersForName.put(name, Integer.valueOf(counter));
		}
		counter++;
		incrementalCountersForName.put(name, counter);
		return counter;
	}

	public String getJsonDescriptor() {
		return jsonDescriptor;
	}

	public void setJsonDescriptor(String jsonDescriptor) {
		this.jsonDescriptor = jsonDescriptor;
	}

}
