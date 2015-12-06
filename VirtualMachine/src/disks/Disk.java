package disks;

// Contains all the constants used by the classes in the package - disks
public class Disk {

	public Disk() {
		// TODO Auto-generated constructor stub
	}

	// Constants
	// for disk Metrics
	static final String TYPE_3DD ="F3DD";
	static final String TYPE_3HD ="F3HD";
	static final String TYPE_3ED ="F3ED";
	static final String TYPE_5DD ="F5DD";
	static final String TYPE_5HD ="F5HD";
	static final String TYPE_8SS ="F8SS";
	static final String TYPE_8DS ="F8DS";

	
	static final int LOGICAL_SECTOR_SIZE = 128;
	static final int SYSTEM_SIZE = 0X2000;
	static final int SYSTEM_LOGICAL_BLOCKS = SYSTEM_SIZE / LOGICAL_SECTOR_SIZE;
	final static int DIRECTORY_ENTRY_SIZE = 32;
	static final int DIRECTORY_ENTRYS_PER_LOGICAL_SECTOR = LOGICAL_SECTOR_SIZE / DIRECTORY_ENTRY_SIZE;

	static final byte NULL_BYTE = (byte) 0x00;
	static final byte EMPTY_ENTRY = (byte) 0xE5;
	static final String EMPTY_NAME = "        ";
	static final String EMPTY_TYPE = "   ";
	static final String PERIOD = ".";

	static final int NAME_MAX = 8;
	static final int TYPE_MAX = 3;

	final static int DIR_USER = 0;
	final static int DIR_NAME = 1;
	final static int DIR_NAME_SIZE = 8;
	final static int DIR_NAME_END = DIR_NAME + DIR_NAME_SIZE;
	final static int DIR_TYPE = 9;
	final static int DIR_TYPE_SIZE = 3;
	final static int DIR_TYPE_END = DIR_TYPE + DIR_TYPE_SIZE;
	final static int DIR_T1 = 9;
	final static int DIR_T2 = 10;
	final static int DIR_EX = 12; // LOW BYTE
	final static int DIR_S2 = 14;
	final static int DIR_S1 = 13;
	final static int DIR_RC = 15;
	final static int DIR_BLOCKS = 16;
	final static int DIR_BLOCKS_SIZE = 16 ;
	final static int DIR_BLOCKS_END = DIR_BLOCKS + DIR_BLOCKS_SIZE;
	// private final static int DIR_SMALL_BLOCKS_COUNT = 16;
	// private final static int DIR_BIG_BLOCKS_COUNT = 8;

	final static byte[] EMPTY_DIRECTORY_ENTRY = new byte[] { Disk.EMPTY_ENTRY,
			Disk.NULL_BYTE, Disk.NULL_BYTE, Disk.NULL_BYTE, Disk.NULL_BYTE, Disk.NULL_BYTE, Disk.NULL_BYTE,
			Disk.NULL_BYTE, Disk.NULL_BYTE,
			Disk.NULL_BYTE, Disk.NULL_BYTE, Disk.NULL_BYTE,
			Disk.NULL_BYTE,
			Disk.NULL_BYTE,
			Disk.NULL_BYTE,
			Disk.NULL_BYTE,
			Disk.NULL_BYTE, Disk.NULL_BYTE, Disk.NULL_BYTE, Disk.NULL_BYTE, Disk.NULL_BYTE, Disk.NULL_BYTE,
			Disk.NULL_BYTE, Disk.NULL_BYTE,
			Disk.NULL_BYTE, Disk.NULL_BYTE, Disk.NULL_BYTE, Disk.NULL_BYTE, Disk.NULL_BYTE, Disk.NULL_BYTE,
			Disk.NULL_BYTE, Disk.NULL_BYTE };

}
