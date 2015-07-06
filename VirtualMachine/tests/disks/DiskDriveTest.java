package disks;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DiskDriveTest {
	DiskDrive diskDrive;
	static String testDisk;
	int targetSector;
	byte value;
	byte[] readBuffer;
	byte[] writeBuffer;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testDisk = MakeNewDisk.makeNewDisk();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		diskDrive = new DiskDrive(testDisk);
		readBuffer = new byte[diskDrive.getBytesPerSector()];
		writeBuffer = new byte[diskDrive.getBytesPerSector()];
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void simpleSectorMovement() {
		targetSector = 0;
		diskDrive.setCurrentAbsoluteSector(targetSector);
		assertThat("direct check tos sector  0", diskDrive.getCurrentAbsoluteSector(), equalTo(targetSector));

		Random random = new Random();
		int boundary = 700; // will fit on a small disk
		for (int i = 0; i < 15; i++) {
			targetSector = random.nextInt(boundary);
			diskDrive.setCurrentAbsoluteSector(targetSector);
			assertThat("direct check random sector", diskDrive.getCurrentAbsoluteSector(), equalTo(targetSector));
		}//
			// fail("Not yet implemented");
	}// simpleSectorMovement

	@Test
	public void testHomeDiskBothDirectAndWithBadValues() {

		targetSector = 5; // known good sector
		diskDrive.setCurrentAbsoluteSector(targetSector);
		assertThat("Confirm heads not at HOME", diskDrive.getCurrentAbsoluteSector(), equalTo(targetSector));
		diskDrive.homeHeads();
		assertThat("Confirm homeHeads() ", diskDrive.getCurrentAbsoluteSector(), equalTo(0));

		targetSector = 5; // known good sector
		diskDrive.setCurrentAbsoluteSector(targetSector);
		assertThat("Confirm heads not at HOME", diskDrive.getCurrentAbsoluteSector(), equalTo(targetSector));
		targetSector = -1; // known bad sector
		diskDrive.setCurrentAbsoluteSector(targetSector);
		assertThat("Confirm bad sector", diskDrive.getCurrentAbsoluteSector(), equalTo(0));

		targetSector = 5; // known good sector
		diskDrive.setCurrentAbsoluteSector(targetSector);
		assertThat("Confirm heads not at HOME", diskDrive.getCurrentAbsoluteSector(), equalTo(targetSector));

		diskDrive.setCurrentAbsoluteSector(-1, 0, 1); // bad head
		assertThat("Confirm bad sector", diskDrive.getCurrentAbsoluteSector(), equalTo(0));

		targetSector = 5; // known good sector
		diskDrive.setCurrentAbsoluteSector(targetSector);
		assertThat("Confirm heads not at HOME", diskDrive.getCurrentAbsoluteSector(), equalTo(targetSector));

		diskDrive.setCurrentAbsoluteSector(0, -1, 1); // bad trrack
		assertThat("Confirm bad sector", diskDrive.getCurrentAbsoluteSector(), equalTo(0));

		targetSector = 5; // known good sector
		diskDrive.setCurrentAbsoluteSector(targetSector);
		assertThat("Confirm heads not at HOME", diskDrive.getCurrentAbsoluteSector(), equalTo(targetSector));

		diskDrive.setCurrentAbsoluteSector(0, 0, -1); // bad sector
		assertThat("Confirm bad sector", diskDrive.getCurrentAbsoluteSector(), equalTo(0));

	}// testHomeDiskBothDirectAndWithBadValues

	@Test
	public void simpleReadWriteTest() {
		for (int i = 0; i < writeBuffer.length; i++) {
			writeBuffer[i] = (byte) (i & 0XFF);
		}// for

		targetSector = 9; // known good sector
		diskDrive.setCurrentAbsoluteSector(targetSector);
		diskDrive.write(writeBuffer);
		diskDrive.writeNext(writeBuffer);

		targetSector = 9; // known good sector
		diskDrive.setCurrentAbsoluteSector(targetSector);
		readBuffer = diskDrive.read();
		assertThat("simple read", readBuffer, equalTo(writeBuffer));

		targetSector = 9; // known good sector
		readBuffer = diskDrive.readNext();
		assertThat("simple readNext", readBuffer, equalTo(writeBuffer));

	}// simpleReadWriteTest

	@Test
	public void randomReadWriteTest() {

		Random random = new Random();
		int boundary = 700; // will fit on a small disk
		for (int i = 0; i < 15; i++) {
			targetSector = random.nextInt(boundary);
			diskDrive.setCurrentAbsoluteSector(targetSector);
			random.nextBytes(writeBuffer);
			diskDrive.write(writeBuffer);
			// readBuffer = diskDrive.read();
			assertThat("direct check random sector & data", diskDrive.read(), equalTo(writeBuffer));
		}//

	}// randomReadWriteTest

}
