package disks;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedMap;

public class CPMDirectoryEntry {
	private byte userNumber;
	private String fileName;
	private String fileType;
	private byte ex;
	private byte s2;
	// private byte s1;
	private byte rc;

	private byte[] rawDirectory;

	private boolean readOnly;
	private boolean systemFile;
	String strEntry;
	private ArrayList<Integer> allocatedBlocks;

	private boolean bigDisk;

	public static CPMDirectoryEntry emptyDirectoryEntry() {
		return new CPMDirectoryEntry(Disk.EMPTY_DIRECTORY_ENTRY);
	}

	public void markAsDeleted() {
		this.setUserNumber(Disk.EMPTY_ENTRY);
	}

	public CPMDirectoryEntry() {
		rawDirectory = new byte[Disk.DIRECTORY_ENTRY_SIZE];
		strEntry = new String(rawDirectory);
		this.setUserNumber(Disk.EMPTY_ENTRY);
		this.setFileName(Disk.EMPTY_NAME);
		this.setFileType(Disk.EMPTY_TYPE);
		this.setEx(Disk.NULL_BYTE);
		this.setS2(Disk.NULL_BYTE);
		// this.s1 = Disk.NULL_BYTE;
		this.setRc(Disk.NULL_BYTE);
		allocatedBlocks = new ArrayList<Integer>();
		this.bigDisk = false;
	}// Constructor

	public CPMDirectoryEntry(byte[] rawEntry) {
		this(rawEntry, false);
	}// Constructor , bigDisk = false

	public CPMDirectoryEntry(byte[] rawEntry, boolean bigDisk) {
		this();
		rawDirectory = rawEntry.clone();
		try {
			strEntry = new String(rawDirectory, "US-ASCII");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.bigDisk = bigDisk;

		this.setUserNumber(rawDirectory[0]);

		this.setFileName(strEntry.substring(Disk.DIR_NAME, Disk.DIR_NAME_END));
		this.setFileType(strEntry.substring(Disk.DIR_TYPE, Disk.DIR_TYPE_END));

		readOnly = ((rawDirectory[Disk.DIR_T1] & 0x80) == 0x80);
		systemFile = ((rawDirectory[Disk.DIR_T2] & 0x80) == 0x80);
		this.setEx(rawDirectory[Disk.DIR_EX]);
		this.setS2(rawDirectory[Disk.DIR_S2]);
		this.setRc(rawDirectory[Disk.DIR_RC]);
		setAllocationTable();
		// blocks = 0;
	}
	public byte[] getRawDirectory(){
		return rawDirectory;
	}

	private void setAllocationTable() {
		int value = 0;

		for (int i = Disk.DIR_BLOCKS; i < Disk.DIR_BLOCKS_END; i++) {
			value = bigDisk ? (int) ((rawDirectory[i] & 0xff) * 256) + (rawDirectory[++i] & 0xff)
					: rawDirectory[i] & 0xff;
			if (value != 0) {
				allocatedBlocks.add(value);
			}// inner if - value
		}// for
	}

	public String toString() {
		return getNameAndTypePeriod();
	}

	// user number
	public int getUserNumberInt() {
		return (int) userNumber & 0xFF;
	}

	public byte getUserNumber() {
		return userNumber;
	}

	public void setUserNumber(byte userNumber) {
		this.userNumber = userNumber;
		rawDirectory[Disk.DIR_USER] = userNumber;
	}

	// File Name & Type
	public String getFileName() {
		return fileName;
	}

	public String getFileNameTrim() {
		return fileName.trim();
	}

	public void setFileName(String fileName) {

		this.fileName = padEntryField(fileName, Disk.NAME_MAX);
		byte[] name;
		try {
			name = this.fileName.getBytes("US-ASCII");
			for (int i = 0; i < 0 + Disk.NAME_MAX; i++) {
				rawDirectory[Disk.DIR_NAME + i] = name[i];
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getFileType() {
		return fileType;
	}

	public String getFileTypeTrim() {
		return fileType.trim();
	}

	public void setFileType(String fileType) {
		boolean wasReadOnly = this.readOnly;
		boolean wasSystemFile = this.systemFile;

		this.fileType = padEntryField(fileType, Disk.TYPE_MAX);
		byte[] type;
		try {
			type = this.fileName.getBytes("US-ASCII");
			for (int i = 0; i < 0 + Disk.TYPE_MAX; i++) {
				rawDirectory[Disk.DIR_TYPE + i] = type[i];
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (wasReadOnly) {
			rawDirectory[Disk.DIR_T1] = (byte) (rawDirectory[Disk.DIR_T1] | 0x80);
		}
		if (wasSystemFile) {
			rawDirectory[Disk.DIR_T2] = (byte) (rawDirectory[Disk.DIR_T2] | 0x80);
		}

		readOnly = ((rawDirectory[Disk.DIR_T1] & 0x80) == 0x80);
		systemFile = ((rawDirectory[Disk.DIR_T2] & 0x80) == 0x80);

	}

	public String getNameAndType11() {
		return getFileName() + getFileType();
	}

	public String getNameTypeAndExt() {
		return getNameAndType11() + getActualExtNumString();
	}

	public String getNameAndTypePeriod() {
		String result = getFileName().trim();
		if (getFileType().trim().length() != 0) {
			result += Disk.PERIOD + getFileType().trim();
		}
		return result;
	}

	private String padEntryField(String field, int fieldLength) {
		String result = field.trim().toUpperCase();
		result = result.length() > fieldLength ? result.substring(0, fieldLength) : result;
		result = String.format("%-" + fieldLength + "s", result);
		return result;
	}

	// Exstents values Ex & s2
	public byte getEx() {
		return ex;
	}

	public void setEx(byte ex) {
		this.ex = (byte) (ex & 0x1F);
		rawDirectory[Disk.DIR_EX] = this.ex;
	}

	public byte getS1() {
		return Disk.NULL_BYTE;
	}

	public byte getS2() {
		return s2;
	}

	public void setS2(byte s2) {
		this.s2 = (byte) (s2 & 0x3F);
		rawDirectory[Disk.DIR_S2] = this.s2;
	}

	public int getActualExtentNumber() {
		return (getS2() * 0x20) + getEx(); // ( s2 * 32) + ex should max at 0x832
	}

	private String getActualExtNumString() {
		return String.format("%03X", getActualExtentNumber());
	}

	// 128-byte record count
	public int getRcInt() {
		return (int) rc & 0xFF;
	}

	public byte getRc() {
		return rc;
	}

	public void setRc(byte rc) {
		int rcInt = rc & 0xFF;
		this.rc = (byte) (rcInt > 0x1F ? 0x1F : rc); // 0x80 = this extent is full
		rawDirectory[Disk.DIR_RC] = this.rc;
	}

	// Allocation table
	public void addBlock(int blockNumber) {
		allocatedBlocks.add(blockNumber);

		if (bigDisk) {
			for (int i = Disk.DIR_BLOCKS; i < Disk.DIR_BLOCKS_END; i += 2) {
				if ((rawDirectory[i] + rawDirectory[i + 1]) == Disk.NULL_BYTE) {
					rawDirectory[i] = (byte) (blockNumber / 256);
					rawDirectory[i + 1] = (byte) (blockNumber % 256);
					break; // we are done
				}// if
			}// for

		} else {
			for (int i = Disk.DIR_BLOCKS; i < Disk.DIR_BLOCKS_END; i++) {
				if (rawDirectory[i] == Disk.NULL_BYTE) {
					rawDirectory[i] = (byte) blockNumber;
					break; // we are done
				}// inner if
			}// for
		}// if
		return;
	}

	public int getBlockCount() {
		return allocatedBlocks.size();
	}

	public ArrayList<Integer> getAllocatedBlocks() {
		return allocatedBlocks;
	}

	public boolean isReadOnly() {
		return this.readOnly;
	}

	public boolean isSystemFile() {
		return this.systemFile;
	}

	public void setReadOnly(boolean state) {
		if (state) {
			rawDirectory[Disk.DIR_T1] = (byte) (rawDirectory[Disk.DIR_T1] | 0x80);
		} else {
			rawDirectory[Disk.DIR_T1] = (byte) (rawDirectory[Disk.DIR_T1] & 0x7F);
		}
	}

	public void setSystemFile(boolean state) {
		if (state) {
			rawDirectory[Disk.DIR_T2] = (byte) (rawDirectory[Disk.DIR_T2] | 0x80);
		} else {
			rawDirectory[Disk.DIR_T2] = (byte) (rawDirectory[Disk.DIR_T2] & 0x7F);
		}
	}
	
	public boolean isEntryFull(){
		int limit = isBigDisk()?Disk.DIRECTORY_ALLOC_SIZE_BIG:Disk.DIRECTORY_ALLOC_SIZE_SMALL;
		return allocatedBlocks.size()>= limit;
	}

	public boolean isEmpty() {
		return this.getUserNumber() == Disk.EMPTY_ENTRY;
	}

	// Supporting attributes
	public boolean isBigDisk() {
		return bigDisk;
	}

	public void setBigDisk(boolean bigDisk) {
		this.bigDisk = bigDisk;
	}

}
