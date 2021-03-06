package disks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CPMDirectoryTest {
	private CPMDirectory dir;

	private boolean bigDisk = false;
	private int maxEntries = 128;
	private int sectorsPerBlock = 4;
	private int bytesPerSector = 512;
	private int trackOffset = 72;
	private int maxBlocks = 175;

	private int entriesPerSector = bytesPerSector / Disk.DIRECTORY_ENTRY_SIZE;
	private int entriesPerBlock = entriesPerSector * sectorsPerBlock;

	@Before
	public void setUp() throws Exception {
		// dir = new CPMDirectory(); // 5 "DD disk
		// dir = new CPMDirectory(bigDisk,maxEntries,sectorsPerBlock,bytesPerSector,trackOffset,maxBlocks); // 5 "DD
		// disk
		dir = new CPMDirectory("F5DD", true);
	}

	@After
	public void tearDown() throws Exception {
		// dir = null;
	}

	@Test
	public void testConstructorDIskType() {
		String fileExtension = "F5DD";
		Boolean bootDisk = true;
		CPMDirectory dir1 = new CPMDirectory(fileExtension, bootDisk);
		assertThat("testConstructorDIskType -1 ", 0, equalTo(dir.getDirectoryBlockNumber(0)));
	}

	@Test
	public void testGetBlockNumber() {
		for (int testEntryNumber = 0; testEntryNumber < 127; testEntryNumber++) {
			assertThat("getBlockNumber - " + testEntryNumber, ((testEntryNumber) / entriesPerBlock),
					equalTo(dir.getDirectoryBlockNumber(testEntryNumber)));
		}// for
	}

	@Test
	public void testGetPhysicalSectorNumber() {
		for (int testEntryNumber = 0; testEntryNumber < 127; testEntryNumber++) {
			assertThat("getPhysicalSectorNumber - " + testEntryNumber, (testEntryNumber / entriesPerSector)
					+ trackOffset,
					equalTo(dir.getPhysicalSectorNumber(testEntryNumber)));
		}// for
	}

	@Test
	public void testGetLogicalRecordIndex() {
		for (int testEntryNumber = 0; testEntryNumber < 127; testEntryNumber++) {
			assertThat("getLogicalRecordIndex - " + testEntryNumber, (testEntryNumber % entriesPerSector),
					equalTo(dir.getLogicalRecordIndex(testEntryNumber)));
		}// for
	}

	@Test
	public void testConstructor() {
		assertThat("testConstructor - 1A", maxEntries, equalTo(dir.getAvailableEntryCount()));
		assertThat("testConstructor - 1B", 0, equalTo(dir.getActiveEntryCount()));
		assertThat("testConstructor - 1C", maxEntries,
				equalTo(dir.getActiveEntryCount() + dir.getAvailableEntryCount()));
	}

	@Test
	public void testAddDirectoryEntry() {

		final byte[] rawDirectory1 = new byte[] { (byte) 00,
				(byte) 0x53, (byte) 0x59, (byte) 0x53, (byte) 0x4D, (byte) 0x41, (byte) 0x4B, (byte) 0x45, (byte) 0x20,
				(byte) 0x43, (byte) 0x4F, (byte) 0x4D,
				(byte) 0x00,
				(byte) 0x00,
				(byte) 0x00,
				(byte) 0x06,
				(byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

		final byte[] rawDirectory2 = new byte[] { (byte) 00,
				(byte) 0x44, (byte) 0x55, (byte) 0x4D, (byte) 0x50, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x43, (byte) 0x4F, (byte) 0x4D,
				(byte) 0x00,
				(byte) 0x00,
				(byte) 0x00,
				(byte) 0x04,
				(byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

		byte[] rawDirectory3 = new byte[] { (byte) 00,
				(byte) 0x54, (byte) 0x45, (byte) 0x58, (byte) 0x54, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x54, (byte) 0x58, (byte) 0x54,
				(byte) 0x00,
				(byte) 0x00,
				(byte) 0x00,
				(byte) 0x1C,
				(byte) 0x04, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

		int blockCount = 2;
		int dirCount = 0;
		assertThat("testAddDirectoryEntry - 1A", maxBlocks - blockCount, equalTo(dir.getAvailableBlockCount()));
		assertThat("testAddDirectoryEntry - 1B", blockCount, equalTo(dir.getAllocatedBlockCount()));
		assertThat("testAddDirectoryEntry - 1C", maxBlocks,
				equalTo(dir.getAllocatedBlockCount() + dir.getAvailableBlockCount()));
		assertThat("testAddDirectoryEntry - 1D", dirCount, equalTo(dir.getActiveEntryCount()));
		assertThat("testAddDirectoryEntry - 1E", maxEntries - dirCount, equalTo(dir.getAvailableEntryCount()));
		int loc;

		loc = dir.addEntry(rawDirectory1);
		blockCount = 3;
		dirCount = 1;
		assertThat("testAddDirectoryEntry - 2A", maxBlocks - blockCount, equalTo(dir.getAvailableBlockCount()));
		assertThat("testAddDirectoryEntry - 2B", blockCount, equalTo(dir.getAllocatedBlockCount()));
		assertThat("testAddDirectoryEntry - 2C", maxBlocks,
				equalTo(dir.getAllocatedBlockCount() + dir.getAvailableBlockCount()));
		assertThat("testAddDirectoryEntry - 2D", dirCount, equalTo(dir.getActiveEntryCount()));
		assertThat("testAddDirectoryEntry - 2E", maxEntries - dirCount, equalTo(dir.getAvailableEntryCount()));

		loc = dir.addEntry(rawDirectory2);
		blockCount = 4;
		dirCount = 2;
		assertThat("testAddDirectoryEntry - 3A", maxBlocks - blockCount, equalTo(dir.getAvailableBlockCount()));
		assertThat("testAddDirectoryEntry - 3B", blockCount, equalTo(dir.getAllocatedBlockCount()));
		assertThat("testAddDirectoryEntry - 3C", maxBlocks,
				equalTo(dir.getAllocatedBlockCount() + dir.getAvailableBlockCount()));
		assertThat("testAddDirectoryEntry - 3D", dirCount, equalTo(dir.getActiveEntryCount()));
		assertThat("testAddDirectoryEntry - 3E", maxEntries - dirCount, equalTo(dir.getAvailableEntryCount()));

		loc = dir.addEntry(rawDirectory3);
		blockCount = 6;
		dirCount = 3;
		assertThat("testAddDirectoryEntry - 4A", maxBlocks - blockCount, equalTo(dir.getAvailableBlockCount()));
		assertThat("testAddDirectoryEntry - 4B", blockCount, equalTo(dir.getAllocatedBlockCount()));
		assertThat("testAddDirectoryEntry - 4C", maxBlocks,
				equalTo(dir.getAllocatedBlockCount() + dir.getAvailableBlockCount()));
		assertThat("testAddDirectoryEntry - 4D", dirCount, equalTo(dir.getActiveEntryCount()));
		assertThat("testAddDirectoryEntry - 4E", maxEntries - dirCount, equalTo(dir.getAvailableEntryCount()));

		dir.deleteFile(2);// 3
		blockCount = 4;// 4
		dirCount = 2;// 2
		// assertThat("testAddDirectoryEntry - 5AAA",0, equalTo(dir.getActiveEntryCount()));
		assertThat("testAddDirectoryEntry - 5A", maxBlocks - blockCount, equalTo(dir.getAvailableBlockCount()));
		assertThat("testAddDirectoryEntry - 5B", blockCount, equalTo(dir.getAllocatedBlockCount()));
		assertThat("testAddDirectoryEntry - 5C", maxBlocks,
				equalTo(dir.getAllocatedBlockCount() + dir.getAvailableBlockCount()));
		assertThat("testAddDirectoryEntry - 5D", dirCount, equalTo(dir.getActiveEntryCount()));
		assertThat("testAddDirectoryEntry - 5E", maxEntries - dirCount, equalTo(dir.getAvailableEntryCount()));
	}

	@Test
	public void testExtents() {
		String fileName = "SYSMAKE.COM";

		final byte[] rawDirectory1 = new byte[] { (byte) 00,
				(byte) 0x53, (byte) 0x59, (byte) 0x53, (byte) 0x4D, (byte) 0x41, (byte) 0x4B, (byte) 0x45, (byte) 0x20,
				(byte) 0x43, (byte) 0x4F, (byte) 0x4D,
				(byte) 0x00,
				(byte) 0x00,
				(byte) 0x00,
				(byte) 0x80,
				(byte) 0x02, (byte) 0x04, (byte) 0x06, (byte) 0x08, (byte) 0x0A, (byte) 0x0C, (byte) 0x0E, (byte) 0x10,
				(byte) 0x012, (byte) 0x14, (byte) 0x16, (byte) 0x18, (byte) 0x1A, (byte) 0x1C, (byte) 0x1E, (byte) 0x20 };

		final byte[] rawDirectory2 = new byte[] { (byte) 00,
				(byte) 0x53, (byte) 0x59, (byte) 0x53, (byte) 0x4D, (byte) 0x41, (byte) 0x4B, (byte) 0x45, (byte) 0x20,
				(byte) 0x43, (byte) 0x4F, (byte) 0x4D,
				(byte) 0x01,
				(byte) 0x00,
				(byte) 0x00,
				(byte) 0x40,
				(byte) 0x11, (byte) 0x0F, (byte) 0x0D, (byte) 0x0B, (byte) 0x09, (byte) 0x07, (byte) 0x05, (byte) 0x03,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

		byte[] rawDirectory3 = new byte[] { (byte) 00,
				(byte) 0x53, (byte) 0x59, (byte) 0x53, (byte) 0x4D, (byte) 0x41, (byte) 0x4B, (byte) 0x45, (byte) 0x20,
				(byte) 0x45, (byte) 0x45, (byte) 0x45,
				(byte) 0x02,
				(byte) 0x00,
				(byte) 0x00,
				(byte) 0x1C,
				(byte) 0x24, (byte) 0x26, (byte) 0x28, (byte) 0x2A, (byte) 0x29, (byte) 0x27, (byte) 0x25, (byte) 0x23,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

		ArrayList<Integer> allocatedDirEntries = new ArrayList<Integer>();
		ArrayList<Integer> allocatedBlocks = new ArrayList<Integer>();

		dir.resetDirectory();
		assertThat("testExtents - 1A", maxEntries, equalTo(dir.getAvailableEntryCount()));
		assertThat("testExtents - 1B", maxBlocks - 2, equalTo(dir.getAvailableBlockCount()));
		assertThat("testExtents - 1C", allocatedDirEntries, equalTo(dir.getDirectoryEntries("FRED")));
		assertThat("testExtents - 1D", allocatedBlocks, equalTo(dir.getFilesBlocks(allocatedDirEntries)));
		assertThat("testExtents - 1E", allocatedBlocks, equalTo(dir.getFilesBlocks("SYSMAKE.COM")));

		dir.addEntry(rawDirectory1);
		allocatedDirEntries.add(0);
		allocatedBlocks.add(2);
		allocatedBlocks.add(4);
		allocatedBlocks.add(6);
		allocatedBlocks.add(8);
		allocatedBlocks.add(10);
		allocatedBlocks.add(12);
		allocatedBlocks.add(14);
		allocatedBlocks.add(16);
		allocatedBlocks.add(18);
		allocatedBlocks.add(20);
		allocatedBlocks.add(22);
		allocatedBlocks.add(24);
		allocatedBlocks.add(26);
		allocatedBlocks.add(28);
		allocatedBlocks.add(30);
		allocatedBlocks.add(32);

		assertThat("testExtents - 2A", maxEntries - 1, equalTo(dir.getAvailableEntryCount()));
		assertThat("testExtents - 2B", maxBlocks - (2 + 16), equalTo(dir.getAvailableBlockCount()));
		assertThat("testExtents - 2C", allocatedDirEntries, equalTo(dir.getDirectoryEntries(fileName)));
		assertThat("testExtents - 2D", allocatedBlocks, equalTo(dir.getFilesBlocks(allocatedDirEntries)));
		assertThat("testExtents - 2E", allocatedBlocks, equalTo(dir.getFilesBlocks("SYSMAKE.COM")));

		dir.addEntry(rawDirectory2);
		allocatedDirEntries.add(1);
		allocatedBlocks.add(17);
		allocatedBlocks.add(15);
		allocatedBlocks.add(13);
		allocatedBlocks.add(11);
		allocatedBlocks.add(9);
		allocatedBlocks.add(7);
		allocatedBlocks.add(5);
		allocatedBlocks.add(3);

		assertThat("testExtents - 3A", maxEntries - 2, equalTo(dir.getAvailableEntryCount()));
		assertThat("testExtents - 3B", maxBlocks - (2 + 16 + 8), equalTo(dir.getAvailableBlockCount()));
		assertThat("testExtents - 3C", allocatedDirEntries, equalTo(dir.getDirectoryEntries(fileName)));
		assertThat("testExtents - 3D", allocatedBlocks, equalTo(dir.getFilesBlocks(allocatedDirEntries)));
		assertThat("testExtents - 3E", allocatedBlocks, equalTo(dir.getFilesBlocks("SYSMAKE.COM")));

		dir.addEntry(rawDirectory3);
		ArrayList<Integer> allocatedBlocks3 = new ArrayList<Integer>();

		allocatedDirEntries.add(2);
		allocatedBlocks3.add(36);
		allocatedBlocks3.add(38);
		allocatedBlocks3.add(40);
		allocatedBlocks3.add(42);
		allocatedBlocks3.add(41);
		allocatedBlocks3.add(39);
		allocatedBlocks3.add(37);
		allocatedBlocks3.add(35);
		assertThat("testExtents - 4A", maxEntries - 3, equalTo(dir.getAvailableEntryCount()));
		assertThat("testExtents - 4B", maxBlocks - (2 + 16 + 8 + 8), equalTo(dir.getAvailableBlockCount()));
		// assertThat("testExtents - 4C",allocatedDirEntries , equalTo(dir.getDirectoryEntries("SYSMAKE.EEE")));
		assertThat("testExtents - 4D", allocatedBlocks3, equalTo(dir.getFilesBlocks(2)));
		assertThat("testExtents - 4E", allocatedBlocks3, equalTo(dir.getFilesBlocks("SYSMAKE.EEE")));
		
		dir.deleteFile(fileName);
		assertThat("testExtents - 5A", maxEntries - 1, equalTo(dir.getAvailableEntryCount()));
		assertThat("testExtents - 5B", maxBlocks - (2 + 0 + 0 + 8), equalTo(dir.getAvailableBlockCount()));


	}

}
