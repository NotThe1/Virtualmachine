package disks;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;

public class TestCPMDirectoryEntry {
	CPMDirectoryEntry de;

	@Before
	public void setUp() throws Exception {
		de = new CPMDirectoryEntry();
	}

	@After
	public void tearDown() throws Exception {
		de = null;
	}

	@Test
	public void testUserNumber() {
		byte un = 0;
		int unInt = un & 0xFF;
		de.setUserNumber(un);
		assertThat("testUserNumber - 1 ", un, equalTo(de.getUserNumber()));
		assertThat("testUserNumber - 2 ", unInt, equalTo(de.getUserNumberInt()));

		un = (byte) 0X8F;
		unInt = (int) un & 0xFF;
		de.setUserNumber(un);
		assertThat("testUserNumber - 1 ", un, equalTo(de.getUserNumber()));
		assertThat("testUserNumber - 2 ", unInt, equalTo(de.getUserNumberInt()));
	}

	@Test
	public void testFileName() {
		String nameIn = " A ";
		String nameOut = nameIn.trim().toUpperCase();
		de.setFileName(nameIn);
		assertThat("testFileName() - 1 ", nameOut, equalTo(de.getFileName().trim()));
		assertThat("testFileName() - 2 ", 8, equalTo(de.getFileName().length()));

		nameIn = "aBcD";
		nameOut = nameIn.trim().toUpperCase();
		de.setFileName(nameIn);
		assertThat("testFileName() - 3 ", nameOut, equalTo(de.getFileName().trim()));
		assertThat("testFileName() - 4 ", 8, equalTo(de.getFileName().length()));

		nameIn = "aBcDeFgHiJ";
		nameOut = nameIn.trim().toUpperCase().substring(0, 8);
		de.setFileName(nameIn);
		assertThat("testFileName() - 5 ", nameOut, equalTo(de.getFileName().trim()));
		assertThat("testFileName() - 6 ", 8, equalTo(de.getFileName().length()));
	}

	@Test
	public void testFileType() {
		String typeIn = " A ";
		String typeOut = typeIn.trim().toUpperCase();
		de.setFileType(typeIn);
		assertThat("testFileType() - 1 ", typeOut, equalTo(de.getFileType().trim()));
		assertThat("testFileType() - 2 ", 3, equalTo(de.getFileType().length()));

		typeIn = "aBc";
		typeOut = typeIn.trim().toUpperCase();
		de.setFileType(typeIn);
		assertThat("testFileType() - 3 ", typeOut, equalTo(de.getFileType().trim()));
		assertThat("testFileType() - 4 ", 3, equalTo(de.getFileType().length()));

		typeIn = "aBcDeFgHiJ";
		typeOut = typeIn.trim().toUpperCase().substring(0, 3);
		de.setFileType(typeIn);
		assertThat("testFileType() - 5 ", typeOut, equalTo(de.getFileType().trim()));
		assertThat("testFileType() - 6 ", 3, equalTo(de.getFileType().length()));
	}

	@Test
	public void testFileNameAndType() {
		String nameIn = " A ";
		String typeIn = "";
		String out11 = "A          ";
		String outPeriod = "A";
		de.setFileName(nameIn);
		de.setFileType(typeIn);
		assertThat("testFileNameAndType() - 1 ", out11, equalTo(de.getNameAndType11()));
		assertThat("testFileNameAndType() - 2 ", 11, equalTo(de.getNameAndType11().length()));
		assertThat("testFileNameAndType() - 3 ", outPeriod, equalTo(de.getNameAndTypePeriod()));

		nameIn = " A ";
		typeIn = "X";
		out11 = "A       X  ";
		outPeriod = "A.X";
		de.setFileName(nameIn);
		de.setFileType(typeIn);
		assertThat("testFileNameAndType() - 4 ", out11, equalTo(de.getNameAndType11()));
		assertThat("testFileNameAndType() - 5 ", 11, equalTo(de.getNameAndType11().length()));
		assertThat("testFileNameAndType() - 6 ", outPeriod, equalTo(de.getNameAndTypePeriod()));

		nameIn = "aBcDeFgHiJk";
		typeIn = "xYz345";
		out11 = "ABCDEFGHXYZ";
		outPeriod = "ABCDEFGH.XYZ";
		de.setFileName(nameIn);
		de.setFileType(typeIn);
		assertThat("testFileNameAndType() - 7 ", out11, equalTo(de.getNameAndType11()));
		assertThat("testFileNameAndType() - 8 ", 11, equalTo(de.getNameAndType11().length()));
		assertThat("testFileNameAndType() - 9 ", outPeriod, equalTo(de.getNameAndTypePeriod()));
	}

	@Test
	public void testExtentsCounters() {
		byte EX = 0x00;
		byte EXout = (byte) (EX & 0x1F);
		byte s2 = 0x00;
		byte s2Out = (byte) (s2 & 0x3F);
		int extent = EXout + (s2Out * 0x20);
		de.setEx(EX);
		de.setS2(s2);
		assertThat("testExtentsCounters() - 1 ", EXout, equalTo(de.getEx()));
		assertThat("testExtentsCounters() - 2 ", s2Out, equalTo(de.getS2()));
		assertThat("testExtentsCounters() - 3 ", extent, equalTo(de.getActualExtentNumber()));

		EX = 0x7F;
		EXout = (byte) (EX & 0x1F);
		s2 = 0x7F;
		s2Out = (byte) (s2 & 0x3F);
		extent = EXout + (s2Out * 0x20);
		de.setEx(EX);
		de.setS2(s2);
		assertThat("testExtentsCounters() - 4 ", EXout, equalTo(de.getEx()));
		assertThat("testExtentsCounters() - 5 ", s2Out, equalTo(de.getS2()));
		assertThat("testExtentsCounters() - 6 ", extent, equalTo(de.getActualExtentNumber()));

		EX = 0x00;
		EXout = (byte) (EX & 0x1F);
		s2 = 0x7F;
		s2Out = (byte) (s2 & 0x3F);
		extent = EXout + (s2Out * 0x20);
		de.setEx(EX);
		de.setS2(s2);
		assertThat("testExtentsCounters() - 7 ", EXout, equalTo(de.getEx()));
		assertThat("testExtentsCounters() - 8 ", s2Out, equalTo(de.getS2()));
		assertThat("testExtentsCounters() - 9 ", extent, equalTo(de.getActualExtentNumber()));

		EX = 0x10;
		EXout = (byte) (EX & 0x1F);
		s2 = 0x00;
		s2Out = (byte) (s2 & 0x3F);
		extent = EXout + (s2Out * 0x20);
		de.setEx(EX);
		de.setS2(s2);
		assertThat("testExtentsCounters() - 10 ", EXout, equalTo(de.getEx()));
		assertThat("testExtentsCounters() - 11 ", s2Out, equalTo(de.getS2()));
		assertThat("testExtentsCounters() - 12 ", extent, equalTo(de.getActualExtentNumber()));
	}

	@Test
	public void testRecordCounter() {
		byte rc = 0x00;
		byte rcOut = (byte) (rc & 0x1F);
		int rcInt = rcOut & 0xFF;
		de.setRc(rc);
		assertThat("testRecordCounter() - 1 ", rcOut, equalTo(de.getRc()));
		assertThat("testRecordCounter() - 2 ", rcInt, equalTo(de.getRcInt()));

		rc = (byte) 0xFF;
		rcOut = (byte) (rc & 0x1F);
		rcInt = rcOut & 0xFF;
		de.setRc(rc);
		assertThat("testRecordCounter() - 3 ", rcOut, equalTo(de.getRc()));
		assertThat("testRecordCounter() - 4 ", rcInt, equalTo(de.getRcInt()));
		assertThat("testRecordCounter() - 5 ", 31, equalTo(de.getRcInt()));
	}

	@Test
	public void testRawDirectory() {
		byte[] rawDirectory = new byte[] { (byte) 00,
				(byte) 0x53, (byte) 0x59, (byte) 0x53, (byte) 0x4D, (byte) 0x41, (byte) 0x4B, (byte) 0x45, (byte) 0x20,
				(byte) 0x43, (byte) 0x4F, (byte) 0x4D,
				(byte) 0x01,
				(byte) 0x05,
				(byte) 0x07,
				(byte) 0x06,
				(byte) 0x02, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
		boolean bigDisk = false;
		CPMDirectoryEntry deRaw = new CPMDirectoryEntry(rawDirectory, bigDisk);
		assertThat("testRawDirectory - 1 ", rawDirectory[0], equalTo(deRaw.getUserNumber()));
		assertThat("testRawDirectory - 2 ", (int) rawDirectory[0], equalTo(deRaw.getUserNumberInt()));

		assertThat("testRawDirectory - 3 ", "SYSMAKE ", equalTo(deRaw.getFileName()));
		assertThat("testRawDirectory - 4 ", "SYSMAKE", equalTo(deRaw.getFileNameTrim()));
		assertThat("testRawDirectory - 5 ", "COM", equalTo(deRaw.getFileType()));
		assertThat("testRawDirectory - 6 ", "COM", equalTo(deRaw.getFileTypeTrim()));

		assertThat("testRawDirectory - 7 ", rawDirectory[12], equalTo(deRaw.getEx()));
		assertThat("testRawDirectory - 8 ", rawDirectory[14], equalTo(deRaw.getS2()));
		assertThat("testRawDirectory - 9 ", rawDirectory[15], equalTo(deRaw.getRc()));
		assertThat("testRawDirectory - 10 ", 2, equalTo(deRaw.getBlockCount()));

		bigDisk = true;
		deRaw = new CPMDirectoryEntry(rawDirectory, bigDisk);
		assertThat("testRawDirectory - 10 ", 1, equalTo(deRaw.getBlockCount()));
	}

	@Test
	public void testAllocation() {
		HashMap<Integer, Boolean> allocatedBlocks = new HashMap<Integer, Boolean>();
		
		byte[] rawDirectory1 = new byte[] { (byte) 00,
				(byte) 0x53, (byte) 0x59, (byte) 0x53, (byte) 0x4D, (byte) 0x41, (byte) 0x4B, (byte) 0x45, (byte) 0x20,
				(byte) 0x43, (byte) 0x4F, (byte) 0x4D,
				(byte) 0x01,
				(byte) 0x05,
				(byte) 0x07,
				(byte) 0x06,
				(byte) 0x02, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
		byte[] rawDirectory2 = new byte[] { (byte) 00,
				(byte) 0x53, (byte) 0x59, (byte) 0x53, (byte) 0x4D, (byte) 0x41, (byte) 0x4B, (byte) 0x45, (byte) 0x20,
				(byte) 0x43, (byte) 0x4F, (byte) 0x4D,
				(byte) 0x01,
				(byte) 0x05,
				(byte) 0x07,
				(byte) 0x06,
				(byte) 0x02, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

		boolean bigDisk = false;
		CPMDirectoryEntry deRaw = new CPMDirectoryEntry(rawDirectory1, bigDisk);
		assertThat("testAllocation - 1 ", 2, equalTo(deRaw.getBlockCount()));
		allocatedBlocks.put(2, true);allocatedBlocks.put(4, true);
		assertThat("testAllocation - 1A ", allocatedBlocks, equalTo(deRaw.getAllocatedBlocks()));
		
		allocatedBlocks.put(6, true);allocatedBlocks.put(8, true);
		deRaw.addBlock(6);
		deRaw.addBlock(8);
		assertThat("testAllocation - 2 ", allocatedBlocks, equalTo(deRaw.getAllocatedBlocks()));
		assertThat("testAllocation - 2A ", 4, equalTo(deRaw.getBlockCount()));
		
		allocatedBlocks.clear();
deRaw = null;
		bigDisk = true;
		deRaw = new CPMDirectoryEntry(rawDirectory1, bigDisk);
		assertThat("testAllocation - 3 ", 1, equalTo(deRaw.getBlockCount()));
//		allocatedBlocks.put(516, true);	allocatedBlocks.put(1554, true);	
//		assertThat("testAllocation - 3A ", allocatedBlocks, equalTo(deRaw.getAllocatedBlocks()));
//		assertThat("testAllocation - 1A ", allocatedBlocks, equalTo(deRaw.getAllocatedBlocks()));
		

//		assertThat("testAllocation - 4 ", 2, equalTo(deRaw.getBlockCount()));
	
	}

	// @BeforeClass
	// public static void setUpBeforeClass() throws Exception {
	// }
	//
	// @AfterClass
	// public static void tearDownAfterClass() throws Exception {
	// }
}
