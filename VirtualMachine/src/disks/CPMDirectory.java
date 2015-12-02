package disks;

import java.util.ArrayList;
import java.util.HashMap;

public class CPMDirectory {
	private HashMap<Integer,CPMDirectoryEntry> dirEntries;
	private boolean bigDisk;
	private int maxEntries;
	private int sectorsPerBlock;
	private int bytesPerSector;
	
	private int absoluteEntryNumber;
	
	int entriesPerSector ;
	
	

	public CPMDirectory() {
		this(false,128,4,512);  // Default to 3.5" and 5" disks
	}
	public CPMDirectory(boolean bigDisk,int maxEntries,int sectorsPerBlock,int bytesPerSector){
		this.bigDisk = bigDisk;
		this.maxEntries = maxEntries;
		this.sectorsPerBlock = sectorsPerBlock;
		this.bytesPerSector = bytesPerSector;
		 entriesPerSector = bytesPerSector/DIRECTORY_ENTRY_SIZE;
	}
	private boolean parseRawEntry(byte[] rawEntry ,int sectorNumber,int index){
		boolean result = false;
		String strEntry = new String(rawEntry);
		CPMDirectoryEntry cpmEntry = new CPMDirectoryEntry();
		cpmEntry.setUserNumber(rawEntry[0]);
		
		cpmEntry.setFileName(convertTo7BitASCII(strEntry.substring(DIR_NAME,  DIR_NAME_END)));
		cpmEntry.setFileType(convertTo7BitASCII(strEntry.substring( DIR_TYPE, DIR_TYPE_END)));
		
//		readOnly = (rawDirectory.get(bias + DIR_T1) & 0x80) == 0x80;
//		systemFile = (rawDirectory.get(bias + DIR_T2) & 0x80) == 0x80;
//		cpmEntry.setS2(rawEntry[DIR_S2]);
//		cpmEntry.setRc(rawEntry[DIR_RC]);
//		count = rawDirectory.get(bias + DIR_RC);
//		blocks = 0;


		return result;
	}
	private String convertTo7BitASCII(String source) {
		char[] sourceChars = source.toCharArray();
		for (int i = 0; i < sourceChars.length; i++) {
			sourceChars[i] &= 0x7F;
		}
		return new String(sourceChars);

	}
	
	public boolean addEntries(int sectorNumber, byte[] aSector){
		boolean result = false;
		byte[] rawEntry = new byte[DIRECTORY_ENTRY_SIZE];
		for (int i =0; i < this.sectorsPerBlock;i ++){
			for (int j = 0; j <DIRECTORY_ENTRY_SIZE; j ++){
				rawEntry[j] = aSector[(i * DIRECTORY_ENTRY_SIZE) + i];	
			}//for - j - now we have 1 raw entry
			parseRawEntry(rawEntry,sectorNumber,i);
		}// for - i
		return result;
	}
	
	public boolean removeEntry(int absoluteEntryNumber){
		boolean result = true;
		if (dirEntries.remove(absoluteEntryNumber) == null){
			result = false;		// nothing there to remove;
		}
		return result;
	}
	
	public boolean addEntry(int absoluteEntryNumber,CPMDirectoryEntry entry){
		boolean result = false;
		if (!(dirEntries.containsKey(absoluteEntryNumber))){
			dirEntries.put(absoluteEntryNumber, entry);
			result = true;  // nothing got wiped out
		}
		return result;
	}
	public boolean addEntry(int absoluteEntryNumber,byte[] rawData){
			boolean result = false;
		if (!(dirEntries.containsKey(absoluteEntryNumber))){
//			dirEntries.put(absoluteEntryNumber, entry);
			result = true;  // nothing got wiped out
		}
		return result;
	}

	private final static int DIRECTORY_ENTRY_SIZE = 32;
	private final static byte EMPTY_ENTRY = (byte) 0xE5;
	
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
	private final static int DIR_SMALL_BLOCKS_COUNT = 16;
	private final static int DIR_BIG_BLOCKS_COUNT = 8;


}
