package device;

abstract public class Device8080 {
	private boolean input;
	private boolean output;

	private Byte addressIn;
	private Byte addressOut;
	private Byte addressStatus;

	private String name;
	private String type;
	private String errMessage;
	/*
	 * @param	name	device name
	 * @param	type	type of device ie storage
	 * @param	input	is this an input device
	 * @param	addressIn	address of the device for input to CPU
	 * @param	output	is this an output device
	 * @param	addressOut	address of the device for output from CPU
	 * @param	addressStatus	id of status port if different from i or out
	 */

	public Device8080(String name, String type, boolean input, Byte addressIn, boolean output, Byte addressOut,
			Byte addressStatus) {
		this.name = name;
		this.type = type;
		this.input = input;
		this.addressIn = addressIn;
		this.output = output;
		this.addressOut = addressOut;
		this.addressStatus = addressStatus;
		if (this.input & (this.addressIn == null)) {
			errMessage = String.format("Constuctor %s device error - input set true without an input address",
					this.name);
			throw new DeviceException(errMessage);
		}//if - no input address
		if (this.output & (this.addressOut == null)) {
			errMessage = String.format("Constuctor %s device error - output set true without an output address",
					this.name);
			throw new DeviceException(errMessage);
		}//if - no output address
		if (!this.output & !this.input) {
			errMessage = String.format("Constuctor %s device error - need input or output or both",
					this.name);
			throw new DeviceException(errMessage);
		}//if - no input/output address
	}// ConstructorDevice8080(String name, String type, boolean input, int addressIn, boolean output, int addressOut,
	//			int addressStatus) {
	public Device8080(String name, String type, boolean input, Byte addressIn, boolean output, Byte addressOut) {
		this(name,type,input,addressIn,output,addressOut,null);
	}// ConstructorDevice8080(String name, String type, boolean input, int addressIn, boolean output, int addressOut)

	public boolean isInput() {
		return this.input;
	}// isInput

	public boolean isOutput() {
		return this.output;
	}// isOutput

	public void setOutput(boolean output) {
		this.output = output;
	}// setOutput

	public void setIinput(boolean input) {
		this.input = input;
	}// setIinput

	public Byte getAddressIn() {
		return addressIn;
	}// getAddressIn

	public void setAddressIn(Byte addressIn) {
		this.addressIn = addressIn; // only 8 bits wide
	}// setAddressIn

	public Byte getAddressOut() {
		return addressOut;
	}// getAddressOut

	public void setAddressOut(Byte addressOut) {
		this.addressOut = addressOut; // only 8 bits wide
	}// setAddressOut

	public Byte getAddressStatus() {
		return addressStatus;
	}// getAddressStatus

	public void setAddressStatus(Byte addressStatus) {
		this.addressStatus = addressStatus; // only 8 bits wide
	}// setAddressStatus

	public String getName() {
		return this.name;
	}// getName

	public void setName(String name) {
		this.name = name;
	}// setName

	public String getType() {
		return this.type;
	}// getType

	public void setType(String type) {
		this.type = type;
	}// setType

	abstract public void byteFromCPU(Byte address, Byte value);

	abstract public byte byteToCPU(Byte address);

}// class DeviceController
