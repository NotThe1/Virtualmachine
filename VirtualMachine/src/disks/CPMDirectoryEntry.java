package disks;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class CPMDirectoryEntry {
	private byte userNumber;
	private String fileName;
	private String fileType;
	private byte ex;
	private byte s2;
	private byte s1;
	private byte rc;

	private byte[] rawDirectory;

	private boolean readOnly;
	private boolean systemFile;
	String strEntry;
	private HashMap<Integer, Boolean> allocatedBlocks;

	private boolean bigDisk;
	// private int blockNumber;
	private int absoluteEntryNumber;

	public CPMDirectoryEntry() {
		rawDirectory = new byte[DIRECTORY_ENTRY_SIZE];
		strEntry = new String(rawDirectory);
		this.setUserNumber(EMPTY_ENTRY);
		this.setFileName(EMPTY_NAME);
		this.setFileType(EMPTY_TYPE);
		this.setEx(NULL_BYTE);
		this.setS2(NULL_BYTE);
		this.s1 = NULL_BYTE;
		this.setRc(NULL_BYTE);
		allocatedBlocks = new HashMap<Integer, Boolean>();
		this.bigDisk = false;
	}// Constructor

	public CPMDirectoryEntry(byte[] rawEntry) {
		this(rawEntry, false);
	}// Constructor , bigDisk = false

	public CPMDirectoryEntry(byte[] rawEntry, boolean bigDisk) {
		this();
		rawDirectory = rawEntry;
		strEntry = new String(rawDirectory);
		this.bigDisk = bigDisk;

		this.setUserNumber(rawDirectory[0]);
		this.setFileName(convertTo7BitASCII(strEntry.substring(DIR_NAME, DIR_NAME_END)));
		this.setFileType(convertTo7BitASCII(strEntry.substring(DIR_TYPE, DIR_TYPE_END)));
		readOnly = ((rawDirectory[DIR_T1] & 0x80) == 0x80);
		systemFile = ((rawDirectory[DIR_T2] & 0x80) == 0x80);
		this.setEx(rawDirectory[DIR_EX]);
		this.setS2(rawDirectory[DIR_S2]);
		this.setRc(rawDirectory[DIR_RC]);
		setAllocationTable();
		// blocks = 0;
	}

	private void setAllocationTable() {
		int value = 0;

		for (int i = DIR_BLOCKS; i < DIR_BLOCKS_END; i++) {
			value = bigDisk ? (int) ((rawDirectory[i] & 0xff) * 256) + (rawDirectory[++i] & 0xff)
					: rawDirectory[i] & 0xff;
			if (value != 0) {
				allocatedBlocks.put(value, true);
			}// inner if - value
		}// for

	}

	private String convertTo7BitASCII(String source) {
		char[] sourceChars = source.toCharArray();
		for (int i = 0; i < sourceChars.length; i++) {
			sourceChars[i] &= 0x7F;
		}
		return new String(sourceChars);

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
		rawDirectory[DIR_USER] = userNumber;
	}

	// File Name & Type
	public String getFileName() {
		return fileName;
	}

	public String getFileNameTrim() {
		return fileName.trim();
	}

	public void setFileName(String fileName) {
		this.fileName = padEntryField(fileName, NAME_MAX);
		byte[] name;
		try {
			name = this.fileName.getBytes("US-ASCII");
			for (int i = 0; i < 0 + NAME_MAX; i++) {
				rawDirectory[DIR_NAME + i] = name[i];
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
		this.fileType = padEntryField(fileType, TYPE_MAX);
		byte[] type;
		try {
			type = this.fileName.getBytes("US-ASCII");
			for (int i = 0; i < 0 + TYPE_MAX; i++) {
				rawDirectory[DIR_TYPE + i] = type[i];
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getNameAndType11() {
		return getFileName() + getFileType();
	}

	public String getNameAndTypePeriod() {
		String result = getFileName().trim();
		if (getFileType().trim().length() != 0) {
			result += PERIOD + getFileType().trim();
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
		rawDirectory[DIR_EX] = this.ex;
	}

	public byte getS2() {
		return s2;
	}

	public void setS2(byte s2) {
		this.s2 = (byte) (s2 & 0x3F);
		rawDirectory[DIR_S2] = this.s2;
	}

	public int getActualExtentNumber() {
		return (getS2() * 0x20) + getEx(); // ( s2 * 32) + ex
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
		rawDirectory[DIR_RC] = this.rc;
	}

	// Allocation table
	public void addBlock(int blockNumber) {
		allocatedBlocks.put(blockNumber, true);
		
		if (bigDisk) {
			for (int i = DIR_BLOCKS; i < DIR_BLOCKS_END; i+=2) {
				if ((rawDirectory[i] + rawDirectory[i + 1]) == NULL_BYTE) {
					rawDirectory[i] = (byte)( blockNumber/256);
					rawDirectory[i+1]  = (byte)(blockNumber % 256);
					break; // we are done
				}// if
			}// for

		} else {
			for (int i = DIR_BLOCKS; i < DIR_BLOCKS_END; i++) {
				if (rawDirectory[i] == NULL_BYTE) {
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

	public HashMap<Integer, Boolean> getAllocatedBlocks() {
		return allocatedBlocks;
	}

	// Supporting attributes
	public boolean isBigDisk() {
		return bigDisk;
	}

	public void setBigDisk(boolean bigDisk) {
		this.bigDisk = bigDisk;
	}

	// public boolean isReadOnly(){
	// readOnly = this.get & 0x80) == 0x80;
	// }
	// public boolean isSystem(){
	//
	// }
	// class methods

	private static final byte NULL_BYTE = (byte) 0x00;
	private static final byte EMPTY_ENTRY = (byte) 0xE5;
	private static final String EMPTY_NAME = "        ";
	private static final String EMPTY_TYPE = "   ";
	private static final String PERIOD = ".";

	private static final int NAME_MAX = 8;
	private static final int TYPE_MAX = 3;

	private final static int DIRECTORY_ENTRY_SIZE = 32;
	private final static int DIR_USER = 0;
	private final static int DIR_NAME = 1;
	private final static int DIR_NAME_SIZE = 8;
	private final static int DIR_NAME_END = DIR_NAME + DIR_NAME_SIZE;
	private final static int DIR_TYPE = 9;
	private final static int DIR_TYPE_SIZE = 3;
	private final static int DIR_TYPE_END = DIR_TYPE + DIR_TYPE_SIZE;
	private final static int DIR_T1 = 9;
	private final static int DIR_T2 = 10;
	private final static int DIR_EX = 12; // LOW BYTE
	private final static int DIR_S2 = 14;
	private final static int DIR_S1 = 13;
	private final static int DIR_RC = 15;
	private final static int DIR_BLOCKS = 16;
	private final static int DIR_BLOCKS_END = 31;
	private final static int DIR_SMALL_BLOCKS_COUNT = 16;
	private final static int DIR_BIG_BLOCKS_COUNT = 8;

}
