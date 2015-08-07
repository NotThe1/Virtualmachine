package disks;

import java.io.Serializable;
import java.nio.ByteBuffer;

import hardware.Core;
import hardware.Core.TRAP;
import hardware.MemoryAccessErrorEvent;
import hardware.MemoryAccessErrorListener;
import hardware.MemoryTrapEvent;
import hardware.MemoryTrapListener;

import javax.swing.JOptionPane;

public class DiskControlUnit implements MemoryTrapListener, MemoryAccessErrorListener, VDiskErrorListener {

	private int maxNumberOfDrives;
	private Core core;
	private DiskDrive[] drives;
	private int currentDrive;

	private int currentDiskControlByte;
	private byte currentCommand;
	private int currentUnit;
	private int currentHead;
	private int currentTrack;
	private int currentSector;
	private int currentByteCount;
	private int currentDMAAddress;
	private boolean goodOperation;

	public DiskControlUnit(Core core, int maxNumberOfDrives) {
		this.core = core;
		core.addMemoryTrapListener(this);
		core.addMemoryAccessErrorListener(this);
		core.addTrapLocation(DISK_CONTROL_BYTE_5, TRAP.IO);
		core.addTrapLocation(DISK_CONTROL_BYTE_8, TRAP.IO);
		this.maxNumberOfDrives = maxNumberOfDrives;
		drives = new DiskDrive[maxNumberOfDrives];
	}// Constructor

	public DiskControlUnit(Core core) {
		this(core, 4);
	}// Constructor

	public void close() { // remove the core listeners
		core.removeMemoryTrapListener(this);
		core.removeMemoryAccessErrorListener(this);
		core.removeTrapLocation(DISK_CONTROL_BYTE_5, TRAP.IO);
		core.removeTrapLocation(DISK_CONTROL_BYTE_8, TRAP.IO);

		for (int i = 0; i < drives.length; i++) {
			if (drives[i]!=null){
				drives[i].removeVDiskErroListener(this);
				removeDiskDrive(i);
			}//if
		}//for
	}//close

	public void addDiskDrive(int index, String fileName) {
		if (drives[index] != null) {
			JOptionPane.showMessageDialog(null, "Already Mounted", "addDiskDrive",
					JOptionPane.WARNING_MESSAGE);
			return;
		}// if
		drives[index] = new DiskDrive(fileName);
		drives[index].addVDiskErroListener(this);
		return;
	}// addDiskDrive

	public void removeDiskDrive(int index) {
		if (drives[index] == null) {
			JOptionPane.showMessageDialog(null, "Already Dismounted", "removeDiskDrive",
					JOptionPane.WARNING_MESSAGE);
			return;
		}// if
		drives[index].removeVDiskErroListener(this);
		drives[index].dismount();
		drives[index] = null;
	}// removeDiskDrive

	public int getMaxNumberOfDrives() {
		return maxNumberOfDrives;
	}// getMaxNumberOfDrives

	public int getCurrentDrive() {
		return currentDrive;
	}// getCurrentDrive

	public void setCurrentDrive(int currentDrive) {
		if ((currentDrive >= 0) & (currentDrive < maxNumberOfDrives)) {
			this.currentDrive = currentDrive;
		}// if
	}// setCurrentDrive

	public DiskDrive[] getDrives() {
		return drives;
	}// getDrives

	@Override
	public void memoryAccessError(MemoryAccessErrorEvent me) {
		diskErrorReport(ERROR_INVALID_DMA_ADDRESS, me.getMessage());
		return;
	}// memoryAccessError

	@Override
	public void vdiskError(VDiskErrorEvent vdee) {
		diskErrorReport(ERROR_INVALID_SECTOR_DESIGNAMTOR, vdee.getMessage());
		return;
	}// vdiskError

	@Override
	public void memoryTrap(MemoryTrapEvent mte) {
		currentDiskControlByte = mte.getLocation(); // 0X0040 for 8" / 0X0045 for 5.25"

		if ((core.readForIO(currentDiskControlByte) & 0X80) == 0) {
			return; // not a disk activation command
		}// if

		goodOperation = true; // assume all goes well

		System.out.printf("DCU: Location: %04X, Value: %02X%n", mte.getLocation(), core.read(mte.getLocation()));

		// TODO - check there is a disk
		int controlTableLocation = getWordReversed(currentDiskControlByte + 1); // flip order for address

		currentCommand = core.read(controlTableLocation + DCT_COMMAND);
		currentUnit = core.read(controlTableLocation + DCT_UNIT);
		currentHead = core.read(controlTableLocation + DCT_HEAD);
		currentTrack = core.read(controlTableLocation + DCT_TRACK);
		currentSector = core.read(controlTableLocation + DCT_SECTOR);
		currentByteCount = getWordReversed(controlTableLocation + DCT_BYTE_COUNT);
		currentDMAAddress = getWordReversed(controlTableLocation + DCT_DMA_ADDRESS);
		debugShowControlTable();
		currentDrive = (currentDiskControlByte == DISK_CONTROL_BYTE_5) ? 0 : 2; // 5" => A or B
		currentDrive += currentUnit; // First (A-C) or second (B-D);

		if (currentDrive >= maxNumberOfDrives) {
			diskErrorReport(ERROR_NO_DRIVE, String.format("No unit  %d", currentDrive));
			return;
		}
		if (drives[currentDrive] == null) {
			diskErrorReport(ERROR_NO_DISK, String.format(" No disk in unit %d", currentDrive));
			return;
		}// if
		int currentSectorSize = drives[currentDrive].getBytesPerSector();
		if (!drives[currentDrive].setCurrentAbsoluteSector(currentHead, currentTrack, currentSector)) {
			diskErrorReport(ERROR_SECTOR_NOT_SET, "Sector not set properly");
			return;
		}//
		int numberOfSectorsToMove = currentByteCount / currentSectorSize;
		if (numberOfSectorsToMove < 0) {
			diskErrorReport(ERROR_INVALID_BYTE_COUNT, String.format("Invalid Byte Count: %04X", currentByteCount));
			return;
		}// if - bad byte count

		System.out.printf("DCU: Head: %d, Track: %d, Sector: %d AbsoluteSector: %d%n",
				currentHead, currentTrack, currentSector, drives[currentDrive].getCurrentAbsoluteSector());
		if (!goodOperation) {
			return; // return if any problems - don't do any I/O
		}//

		if (currentCommand == COMMAND_READ) {
			ByteBuffer readByteBuffer = ByteBuffer.allocate(numberOfSectorsToMove * currentSectorSize);
			readByteBuffer.put(drives[currentDrive].read());
			for (int i = 0; i < numberOfSectorsToMove - 1; i++) {
				readByteBuffer.put(drives[currentDrive].readNext());
			}// for
			byte[] readBuffer = readByteBuffer.array();
			core.writeDMA(currentDMAAddress, readBuffer);
			System.out.printf("DCU:Value: %02X, length = %d%n", readBuffer[1], readBuffer.length);
		} else { // its a COMMAND_WRITE
			// byte[] writeBuffer = core.readDMA(currentDMAAddress, currentSectorSize);
			// drives[currentDrive].write(writeBuffer);

			byte[] writeSector = new byte[currentSectorSize];
			byte[] readFromCore = core.readDMA(currentDMAAddress, currentByteCount);

			ByteBuffer writeByteBuffer = ByteBuffer.wrap(readFromCore);
			// ByteBuffer writeSectorBuffer = ByteBuffer.allocate(currentSectorSize);
			writeByteBuffer.get(writeSector);
			drives[currentDrive].write(writeSector);
			for (int i = 0; i < numberOfSectorsToMove - 1; i++) {
				writeByteBuffer.get(writeSector);
				drives[currentDrive].writeNext(writeSector);
			}// for

		}//
		if (goodOperation) {
			reportStatus((byte) 00, (byte) 00); // reset - operation is over
		}//

	}// memoryTrap

	private int getWordReversed(int location) {
		int loByte = (core.read(location + 1) << 8) & 0XFF00;
		int hiByte = core.read(location) & 0X00FF;
		return 0XFFFF & (hiByte + loByte);
	}// getWordReversed

	private void reportStatus(byte firstCode, byte secondCode) {
		core.write(DISK_STATUS_BLOCK, firstCode);
		core.write(DISK_STATUS_BLOCK + 1, secondCode);
		core.write(currentDiskControlByte, (byte) 00); // reset - operation is over
	}//

	private void diskErrorReport(int code, String message) {
		// TODO write to error code, and clear ControlByte
		System.err.printf("DCU: DiskError - %d - %s%n", code, message);
		goodOperation = false;
		reportStatus((byte) 00, (byte) code);
		// core.write(DISK_STATUS_BLOCK, (byte) 00);
		// core.write(DISK_STATUS_BLOCK + 1, (byte) code);
		// core.write(currentDiskControlByte, (byte) 00); // reset - operation is over
	}// diskErrorReport

	private void debugShowControlTable() {
		System.out.printf("currentCommand: %02X%n", currentCommand);
		System.out.printf("currentUnit: %02X%n", currentUnit);
		System.out.printf("currentHead: %02X%n", currentHead);
		System.out.printf("currentTrack: %02X%n", currentTrack);
		System.out.printf("currentSector: %02X%n", currentSector);
		System.out.printf("currentByteCount: %04X%n", currentByteCount);
		System.out.printf("currentDMAAddress: %04X%n", currentDMAAddress);
	}

	private static final int ERROR_NO_DISK = 10;
	private static final int ERROR_INVALID_SECTOR_DESIGNAMTOR = 11;
	private static final int ERROR_NO_DRIVE = 12;
	private static final int ERROR_SECTOR_NOT_SET = 13;
	private static final int ERROR_INVALID_DMA_ADDRESS = 14;
	private static final int ERROR_INVALID_BYTE_COUNT = 15;

	private static final byte COMMAND_READ = 01;
	private static final byte COMMAND_WRITE = 02;

	private static final int DISK_CONTROL_BYTE_8 = 0X0040;
	private static final int DISK_CONTROL_BYTE_5 = 0X0045;
	private static final int DISK_STATUS_BLOCK = 0X0043;
	// Disk Control Table
	private static final int DCT_COMMAND = 0; // DB 1
	private static final int DCT_UNIT = 1; // DB 1
	private static final int DCT_HEAD = 2; // DB 1
	private static final int DCT_TRACK = 3; // DB 1
	private static final int DCT_SECTOR = 4; // DB 1
	private static final int DCT_BYTE_COUNT = 5; // DW 1
	private static final int DCT_DMA_ADDRESS = 7; // DW 1
	private static final int DCT_NEXT_STATUS_BLOCK = 9; // DW 1
	private static final int DCT_NEXT_DCT = 11; // DW 1

}// class DiskControlUnit
