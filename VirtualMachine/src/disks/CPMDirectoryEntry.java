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
	}//CPMDirectoryEntry

	public void markAsDeleted() {
		this.setUserNumber(Disk.EMPTY_ENTRY);
	}//markAsDeleted

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
		}//try
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
	}// Constructor - rawDirectory bigDisk
	
	public byte[] getRawDirectory(){
		return rawDirectory;
	}//getRawDirectory

	private void setAllocationTable() {
		int value = 0;

		for (int i = Disk.DIR_BLOCKS; i < Disk.DIR_BLOCKS_END; i++) {
			value = bigDisk ? (int) ((rawDirectory[i] & 0xff) * 256) + (rawDirectory[++i] & 0xff)
					: rawDirectory[i] & 0xff;
			if (value != 0) {
				allocatedBlocks.add(value);
			}// inner if - value
		}// for
	}//setAllocationTable

	public String toString() {
		return getNameAndTypePeriod();
	}//toString

	// user number
	public int getUserNumberInt() {
		return (int) userNumber & 0xFF;
	}//getUserNumberInt

	public byte getUserNumber() {
		return userNumber;
	}//getUserNumber

	public void setUserNumber(byte userNumber) {
		this.userNumber = userNumber;
		rawDirectory[Disk.DIR_USER] = userNumber;
	}//setUserNumber

	// File Name & Type
	public String getFileName() {
		return fileName;
	}//getFileName

	public String getFileNameTrim() {
		return fileName.trim();
	}//getFileNameTrim
	
	public void setFilenameAndType(String nameAndType){
		String[] nat = nameAndType.split("\\.");
		String name = nat[0];
		String type = nat.length>1?nat[1]:"";
		
		this.setFileName(name);
		this.setFileType(type);
	}//setFilenameAndType

	public void setFileName(String fileName) {

		this.fileName = padEntryField(fileName, Disk.NAME_MAX);
		byte[] name;
		try {
			name = this.fileName.getBytes("US-ASCII");
			for (int i = 0; i < 0 + Disk.NAME_MAX; i++) {
				rawDirectory[Disk.DIR_NAME + i] = name[i];
			}//fortry
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//
	}//setFileName

	public String getFileType() {
		return fileType;
	}//getFileType

	public String getFileTypeTrim() {
		return fileType.trim();
	}//getFileTypeTrim

	public void setFileType(String fileType) {
		boolean wasReadOnly = this.readOnly;
		boolean wasSystemFile = this.systemFile;

		this.fileType = padEntryField(fileType, Disk.TYPE_MAX);
		byte[] type;
		try {
			type = this.fileType.getBytes("US-ASCII");
			for (int i = 0; i < 0 + Disk.TYPE_MAX; i++) {
				rawDirectory[Disk.DIR_TYPE + i] = type[i];
			}//for
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//try
		if (wasReadOnly) {
			rawDirectory[Disk.DIR_T1] = (byte) (rawDirectory[Disk.DIR_T1] | 0x80);
		}//if
		if (wasSystemFile) {
			rawDirectory[Disk.DIR_T2] = (byte) (rawDirectory[Disk.DIR_T2] | 0x80);
		}//if

		readOnly = ((rawDirectory[Disk.DIR_T1] & 0x80) == 0x80);
		systemFile = ((rawDirectory[Disk.DIR_T2] & 0x80) == 0x80);

	}//setFileType

	public String getNameAndType11() {
		return getFileName() + getFileType();
	}///getNameAndType11

	public String getNameTypeAndExt() {
		return getNameAndType11() + getActualExtNumString();
	}//getNameTypeAndExt

	public String getNameAndTypePeriod() {
		String result = getFileName().trim();
		if (getFileType().trim().length() != 0) {
			result += Disk.PERIOD + getFileType().trim();
		}
		return result;
	}//getNameAndTypePeriod

	private String padEntryField(String field, int fieldLength) {
		String result = field.trim().toUpperCase();
		result = result.length() > fieldLength ? result.substring(0, fieldLength) : result;
		result = String.format("%-" + fieldLength + "s", result);
		return result;
	}//padEntryField

	// Exstents values Ex & s2
	public byte getEx() {
		return ex;
	}//getEx

	public void setEx(byte ex) {
		this.ex = (byte) (ex & 0x1F);
		rawDirectory[Disk.DIR_EX] = this.ex;
	}//setEx

	public byte getS1() {
		return Disk.NULL_BYTE;
	}//getS1

	public byte getS2() {
		return s2;
	}//getS2

	public void setS2(byte s2) {
		this.s2 = (byte) (s2 & 0x3F);
		rawDirectory[Disk.DIR_S2] = this.s2;
	}//setS2

	public int getActualExtentNumber() {
		return (getS2() * 32) + getEx(); // ( s2 * 32) + ex should max at 0x832
	}//getActualExtentNumber
	public void setActualExtentNumber(int actualExtentNumber){
		byte s = (byte) (actualExtentNumber / 32) ;
		byte e = (byte) (actualExtentNumber % 32);
		setS2(s);
		setEx(e);
	}//setActualExtentNumber

	private String getActualExtNumString() {
		return String.format("%03X", getActualExtentNumber());
	}//getActualExtNumString

	// 128-byte record count
	public int getRcInt() {
		return (int) rc & 0xFF;
	}//getRcInt

	public byte getRc() {
		return rc;
	}//getRc

	public void setRc(byte rc) {
		int rcInt = rc & 0xFF;
		this.rc = (byte) (rcInt > 0x80 ? 0x80 : rc); // 0x80 = this extent is full
		rawDirectory[Disk.DIR_RC] = this.rc;
	}//setRc
	
	public void incrementRc(int amount){
		this.setRc((byte) (this.getRc() + amount));
	}//incrementRc

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
	}//addBlock

	public int getBlockCount() {
		return allocatedBlocks.size();
	}//getBlockCount

	public ArrayList<Integer> getAllocatedBlocks() {
		return allocatedBlocks;
	}//getAllocatedBlocks
	
	public void resetEntry(){
		this.setFileName("");
		this.setFileType("");
		this.setActualExtentNumber(0);
		this.setRc((byte) 0);
		allocatedBlocks.clear();
		for (int i = Disk.DIR_BLOCKS; i < Disk.DIR_BLOCKS_END; i++) {
			rawDirectory[i] = Disk.NULL_BYTE;
		}// for

	}

	public boolean isReadOnly() {
		return this.readOnly;
	}//isReadOnly

	public boolean isSystemFile() {
		return this.systemFile;
	}//isSystemFile

	public void setReadOnly(boolean state) {
		if (state) {
			rawDirectory[Disk.DIR_T1] = (byte) (rawDirectory[Disk.DIR_T1] | 0x80);
		} else {
			rawDirectory[Disk.DIR_T1] = (byte) (rawDirectory[Disk.DIR_T1] & 0x7F);
		}//if
	}//setReadOnly

	public void setSystemFile(boolean state) {
		if (state) {
			rawDirectory[Disk.DIR_T2] = (byte) (rawDirectory[Disk.DIR_T2] | 0x80);
		} else {
			rawDirectory[Disk.DIR_T2] = (byte) (rawDirectory[Disk.DIR_T2] & 0x7F);
		}//if
	}//setSystemFile
	
	public boolean isEntryFull(){
		int limit = isBigDisk()?Disk.DIRECTORY_ALLOC_SIZE_BIG:Disk.DIRECTORY_ALLOC_SIZE_SMALL;
		return allocatedBlocks.size()>= limit;
	}//isEntryFull

	public boolean isEmpty() {
		return this.getUserNumber() == Disk.EMPTY_ENTRY;
	}//isEmpty

	// Supporting attributes
	public boolean isBigDisk() {
		return bigDisk;
	}//isBigDisk

	public void setBigDisk(boolean bigDisk) {
		this.bigDisk = bigDisk;
	}//setBigDisk

}//class CPMDirectoryEntry
