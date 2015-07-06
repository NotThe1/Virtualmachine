package device;

import java.util.HashMap;

import console.Console;

//import terminal.Terminal;	//found in project: terminalSerial


public class DeviceController {
	HashMap<Byte, Device8080> inputDevices;
	HashMap<Byte, Device8080> outputDevices;
	HashMap<Byte, Device8080> statusDevices;
	private String errMessage;
	Console console ;

	public DeviceController() {
		inputDevices = new HashMap<Byte, Device8080>();
		outputDevices = new HashMap<Byte, Device8080>();
		statusDevices = new HashMap<Byte, Device8080>();
		// console setup ++
		console = new Console(CONSOLE_IN,CONSOLE_OUT,CONSOLE_STATUS);

		Byte deviceAddress = console.getAddressIn();
		if (deviceAddress != null) {
			inputDevices.put(deviceAddress, console);
		}// if in device

		deviceAddress = console.getAddressOut();
		if (deviceAddress != null) {
			outputDevices.put(deviceAddress, console);
		}// if in device

		deviceAddress = console.getAddressStatus();
		if (deviceAddress != null) {
			statusDevices.put(deviceAddress, console);
		}// if in device
		// console setup ++
			
	}// Constructor DeviceController()
	
	public void setSerialConnection(){
		console.setSerialConnection();
	}//setSerialConnection
	
	public String getConnectionString(){
		return console.getConnectionString();
	}
	public void closeConnection(){
		console.closeConnection();
	}

	public void byteToDevice(Byte address, Byte value) {
		if (!inputDevices.containsKey(address)) {
			errMessage = String.format("Bad address %02X for byteToDevice operation", address);
			System.err.println(errMessage);
		} else {
			Device8080 device = inputDevices.get(address);
			device.byteFromCPU(address, value);
		}// for
	}// byteToDevice

	public Byte byteFromDevice(Byte address) {
		Device8080 device ;
		if(inputDevices.containsKey(address)){
			device = inputDevices.get(address);
		}else if(statusDevices.containsKey(address)){
			device = statusDevices.get(address);
		}else {
			errMessage = String.format("Bad address %02X for byteToDevice operation", address);
			System.err.println(errMessage);
			return null;
		}//if
			return device.byteToCPU(address);
	}// byteFromDevice
	
	public  final static byte CONSOLE_IN = 01;		//data from the console - To CPU
	public  final static byte CONSOLE_OUT = 01;	// data to the console - From CPU
	public  final static byte CONSOLE_STATUS = 02;	// How many characters in the input buffer

}// class DeviceController
