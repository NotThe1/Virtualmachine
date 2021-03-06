package memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.io.Serializable;

import javax.swing.JOptionPane;

public class Core implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private byte[] storage;
	private int maximumAddress; // maximum address
	private int protectedBoundary; // highest location in protected area
	private boolean debugTrapEnabled;
	public boolean isDebugLocation; // set when reading a debug tagged location
	private boolean fatso;
	private  boolean oldIsDebugLocation;		
	private HashMap<Integer, TRAP> trapLocations; // locations that can be trapped duh!

	// private byte writeValue; // used for IO trap

	public Core(Integer size) {
		this(size, 0);
	}// Constructor

	public Core(Integer size, Integer protectedBoundary) {
		this.debugTrapEnabled = false;
		this.isDebugLocation = false;
		trapLocations = new HashMap<Integer, TRAP>();

		if (size <= 0) {
			System.err.printf("Memory size %d not valid%n", size);
			System.exit(-1);
		}// if

		storage = new byte[size];
		maximumAddress = storage.length - 1;

		if ((protectedBoundary >= maximumAddress) || (protectedBoundary < 0)) {
			String errorMessage = String
					.format("Invalid protected highest location: 0X%1$04X - Decimal %1$d   %n"
							+ "  Memory's highest location is:     0X%2$04X - Decimal %2$d%n"
							+ "   - will set to 0 - No protected Memory",
							protectedBoundary, maximumAddress);
			JOptionPane.showMessageDialog(null, errorMessage,
					"Setting up Main Memory", JOptionPane.ERROR_MESSAGE);

			protectedBoundary = 0;
		}// if
		this.protectedBoundary = protectedBoundary;
	}// Constructor

	public Core() {
		this(MAXIMUM_MEMORY, 0);
	}// Constructor

	public synchronized void write(int location, byte value) {
		// writeValue = value; // save for IO trap
		if (checkAddressAndTraps(location, value) == true) {
			storage[location] = value;
		}// if
	}// setContent- for MM

	public synchronized void writeForIO(int location, byte value) {
		// writeValue = value; // save for IO trap
		if (checkAddress(location) == true) {
			storage[location] = value;
		}// if
	}// setContent- for IO devices

	public synchronized void writeDMA(int location, byte[] values) {
		int numberOfBytes = values.length;
		if (checkAddressDMA(location, numberOfBytes) == true) {
			for (int i = 0; i < numberOfBytes; i++) {
				storage[location + i] = values[i];
			}// for
		}// if
	}// writeDMA

	public synchronized byte read(int location) {
		if (checkAddressAndTraps(location) == true) { // send dummy argument
			isDebugLocation = false;
			return storage[location];
		}// if

		if (isDebugLocation) {
			return 0X30; // Return a fake Halt instruction
		} else {
			return 00;
		}//

	}// getContent- for MM

	public synchronized byte readForIO(int location) {
		if (checkAddress(location) == true) { // send dummy argument
			return storage[location];
		}// if
		return 00;
	}// getContent - for IO devices

	public synchronized byte[] readDMA(int location, int length) {
		byte[] readDMA = new byte[length];
		if (checkAddressDMA(location, length) == true) {
			for (int i = 0; i < length; i++) {
				readDMA[i] = storage[location + i];
			}// for
		} else {
			readDMA = null;
		}
		return readDMA;
	}// readDMA

	public int getSize() {
		return storage.length;
	}// getSize

	public int getProtectedBoundary() {
		return protectedBoundary;
	}// getProtectedBoundary

	public void setDebugTrapEnabled(boolean state) {
		this.debugTrapEnabled = state;
	}// enableTrap

	public boolean isDebugTrapEnabled() {
		return this.debugTrapEnabled;
	}

	// public boolean

	public void addTrapLocation(int location, TRAP trap) {
		if (!trapLocations.containsKey(location)) { // Not trapped yet
			if (checkAddress(location) == false) {
				return; // bad address - get out of here
			} // inner if
		}// if
		trapLocations.put(location, trap); // may be different trap type
	}// addTrapLocation

	public ArrayList<Integer> getTrapLocations() {
		return getTrapLocations(TRAP.DEBUG);
	}// getTrapLocations - DEBUG

	public ArrayList<Integer> getTrapLocations(TRAP trap) {
		ArrayList<Integer> getTrapLocations = new ArrayList<Integer>();
		Set<Integer> locations = trapLocations.keySet();
		for (Integer location : locations) {
			if (trapLocations.get(location).equals(trap)) {
				getTrapLocations.add(location);
			}// inner if
		}// for e
		return getTrapLocations;
	}// getTrapLocations

	public void removeTrapLocation(int location, TRAP trap) {
		// only remove if the trap is the same type
		trapLocations.remove(location, trap);

	}// removeTrapLocation

	private boolean checkAddress(int location) {// just check its in readable memory
		boolean checkAddress = true;
		if (location < protectedBoundary) {
			// protection violation
			checkAddress = false;
			fireAccessError(location, "Protected memory access");
		} else if (location > maximumAddress) {
			// out of bounds error
			checkAddress = false;
			fireAccessError(location, "Invalid memory location");
		}// if
		return checkAddress;
	}// checkAddress

	private boolean checkAddressAndTraps(int location) {
		// send a dummy value for reads
		return checkAddressAndTraps(location, (byte) 00);
	}

	private boolean checkAddressAndTraps(int location, byte value) {
		boolean checkAddressAndTraps = checkAddress(location);
		
		if (trapLocations.containsKey(location)) {
			TRAP thisTrap = trapLocations.get(location);

			if (thisTrap.equals(TRAP.IO)) {
				storage[location] = value; // write so DCU has access to it
				fireMemoryTrap(location, Core.TRAP.IO);
			} else if (thisTrap.equals(TRAP.DEBUG) & debugTrapEnabled) {
				if (!isDebugLocation) { // Is this the first encounter?
					isDebugLocation = true; // set the flag
					//checkAddressAndTraps = false; // force return value
					return false;
				} // inner if
			}// outer if
		}// if
		return checkAddressAndTraps;
	}//checkAddressAndTraps

	private boolean checkAddressDMA(int location, int length) {
		boolean checkAddressDMA = true;
		if ((location < protectedBoundary) | ((location + (length - 1)) > maximumAddress)) {
			checkAddressDMA = false;
			fireAccessError(location, "Invalid DMA memory location");
		}// if
		return checkAddressDMA;
	}

	@SuppressWarnings("unchecked")
	private void fireAccessError(int location, String errorType) {
		Vector<MemoryAccessErrorListener> mael;
		synchronized (this) {
			mael = (Vector<MemoryAccessErrorListener>) memoryAccessErrorListeners.clone();
		}// sync
		int size = mael.size();
		if (0 == size) {
			return; // no listeners
		}// if
		MemoryAccessErrorEvent memoryAccessErrorEvent = new MemoryAccessErrorEvent(
				this, location, errorType);
		for (int i = 0; i < size; i++) {
			MemoryAccessErrorListener listener = (MemoryAccessErrorListener) mael
					.elementAt(i);
			listener.memoryAccessError(memoryAccessErrorEvent);
		}// for
	}// fireProtectedMemoryAccess

	public void resetListeners() {
		memoryAccessErrorListeners = null;
		memoryAccessErrorListeners = new Vector<MemoryAccessErrorListener>();
		memoryTrapListeners = null;
		memoryTrapListeners = new Vector<MemoryTrapListener>();
	}// resetListeners

	private Vector<MemoryAccessErrorListener> memoryAccessErrorListeners = new Vector<MemoryAccessErrorListener>();

	public synchronized void addMemoryAccessErrorListener(
			MemoryAccessErrorListener mael) {
		if (memoryAccessErrorListeners.contains(mael)) {
			return; // Already here
		}// if
		memoryAccessErrorListeners.addElement(mael);
	}// addMemoryListener

	public synchronized void removeMemoryAccessErrorListener(
			MemoryAccessErrorListener mael) {
		memoryAccessErrorListeners.remove(mael);
	}// removeMemoryListener

	@SuppressWarnings("unchecked")
	private void fireMemoryTrap(int location, TRAP trap) {
		Vector<MemoryTrapListener> mtl;
		synchronized (this) {
			mtl = (Vector<MemoryTrapListener>) memoryTrapListeners.clone();

			int size = mtl.size();
			if (0 == size) {
				return; // no listeners
			}// if
			MemoryTrapEvent memoryTrapEvent = new MemoryTrapEvent(this, location, trap);
			for (int i = 0; i < size; i++) {
				MemoryTrapListener listener = (MemoryTrapListener) mtl
						.elementAt(i);
				listener.memoryTrap(memoryTrapEvent);
			}// for
		}// sync
	}// fireProtectedMemoryAccess

	private Vector<MemoryTrapListener> memoryTrapListeners = new Vector<MemoryTrapListener>();

	public synchronized void addMemoryTrapListener(MemoryTrapListener mtl) {
		if (memoryTrapListeners.contains(mtl)) {
			return; // Already here
		}// if
		memoryTrapListeners.addElement(mtl);
	}// addMemoryListener

	public synchronized void removeMemoryTrapListener(MemoryTrapListener mtl) {
		memoryTrapListeners.remove(mtl);
	}// removeMemoryListener

	public enum TRAP {
		IO, DEBUG
	}

	static int K = 1024;
	static int PROTECTED_MEMORY = 0; // 100;
	static int MINIMUM_MEMORY = 8 * K;
	static int MAXIMUM_MEMORY = 64 * K;
	static int DEFAULT_MEMORY = 16 * K;

	static Integer DISK_CONTROL_BYTE_40 = 0X0040;
	static Integer DISK_CONTROL_BYTE_45 = 0X0045;

}// class Mem
