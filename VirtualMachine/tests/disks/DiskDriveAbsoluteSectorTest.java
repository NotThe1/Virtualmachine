package disks;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DiskDriveAbsoluteSectorTest {
	String diskUnderTest = "C:\\Users\\admin\\git\\Virtualmachine\\VirtualMachine\\Disks\\rat.F3DD";
	DiskDrive diskDrive;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		diskDrive = new DiskDrive(diskUnderTest);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDriveHome() {
		int target;

		target = 0;
		diskDrive.homeHeads();
		assertThat("homeHeads Confirm heads are at HOME - ", diskDrive.getCurrentAbsoluteSector(), equalTo(target));

		target = 256;
		diskDrive.setCurrentAbsoluteSector(target);
		assertThat("Confirm heads are not at HOME", diskDrive.getCurrentAbsoluteSector(), not(equalTo(0)));

		target = 0;
		diskDrive.setCurrentAbsoluteSector(target);
		assertThat("setCurrentAbsoluteSector(0) Confirm heads are at HOME - ", diskDrive.getCurrentAbsoluteSector(),
				equalTo(target));

	}

	@Test
	public void testAbsoluteSector() {
		int absoluteSector, sector, head, track;

		absoluteSector = 0;
		sector = 1;
		head = 0;
		track = 0;
		diskDrive.setCurrentAbsoluteSector(absoluteSector);
		assertThat("testAbsoluteSector - 1", diskDrive.getCurrentSector(), equalTo(sector));
		assertThat("testAbsoluteSector - 2", diskDrive.getCurrentHead(), equalTo(head));
		assertThat("testAbsoluteSector - 3", diskDrive.getCurrentTrack(), equalTo(track));

		absoluteSector = 8;
		sector = 9;
		head = 0;
		track = 0;
		diskDrive.setCurrentAbsoluteSector(absoluteSector);
		assertThat("testAbsoluteSector - 4", diskDrive.getCurrentSector(), equalTo(sector));
		assertThat("testAbsoluteSector - 5", diskDrive.getCurrentHead(), equalTo(head));
		assertThat("testAbsoluteSector - 6", diskDrive.getCurrentTrack(), equalTo(track));

		absoluteSector = 9;
		sector = 1;
		head = 1;
		track = 0;
		diskDrive.setCurrentAbsoluteSector(absoluteSector);
		assertThat("testAbsoluteSector - 7", diskDrive.getCurrentSector(), equalTo(sector));
		assertThat("testAbsoluteSector - 8", diskDrive.getCurrentHead(), equalTo(head));
		assertThat("testAbsoluteSector - 9", diskDrive.getCurrentTrack(), equalTo(track));

		absoluteSector = 18;
		sector = 1;
		head = 0;
		track = 1;
		diskDrive.setCurrentAbsoluteSector(absoluteSector);
		assertThat("testAbsoluteSector - 9A", diskDrive.getCurrentSector(), equalTo(sector));
		assertThat("testAbsoluteSector - 10", diskDrive.getCurrentHead(), equalTo(head));
		assertThat("testAbsoluteSector - 11", diskDrive.getCurrentTrack(), equalTo(track));

		absoluteSector = 20;
		sector = 3;
		head = 0;
		track = 1;
		diskDrive.setCurrentAbsoluteSector(absoluteSector);
		assertThat("testAbsoluteSector - 12", diskDrive.getCurrentSector(), equalTo(sector));
		assertThat("testAbsoluteSector - 13", diskDrive.getCurrentHead(), equalTo(head));
		assertThat("testAbsoluteSector - 14", diskDrive.getCurrentTrack(), equalTo(track));

		absoluteSector = 20;
		sector = 3;
		head = 0;
		track = 1;
		diskDrive.setCurrentAbsoluteSector(absoluteSector);
		assertThat("testAbsoluteSector - 15", diskDrive.getCurrentSector(), equalTo(sector));
		assertThat("testAbsoluteSector - 16", diskDrive.getCurrentHead(), equalTo(head));
		assertThat("testAbsoluteSector - 17", diskDrive.getCurrentTrack(), equalTo(track));

		absoluteSector = 20;
		sector = 3;
		head = 0;
		track = 1;
		diskDrive.setCurrentAbsoluteSector(absoluteSector);
		assertThat("testAbsoluteSector - 18", diskDrive.getCurrentSector(), equalTo(sector));
		assertThat("testAbsoluteSector - 19", diskDrive.getCurrentHead(), equalTo(head));
		assertThat("testAbsoluteSector - 20", diskDrive.getCurrentTrack(), equalTo(track));
	}

	@Test
	public void testTrackHeadSector() {
		int maxAbsoluteSector = (int) (diskDrive.getTotalBytes()/diskDrive.getBytesPerSector());	
		Random random = new Random();
				
		String message,messageHome;
		int absoluteSector, sector, head, track;
		
		int limit = 20;
		for (int i = 0; i < limit; i++) {
			absoluteSector = random.nextInt(maxAbsoluteSector-1);
			diskDrive.setCurrentAbsoluteSector(absoluteSector);
			track = diskDrive.getCurrentTrack();
			head = diskDrive.getCurrentHead();
			sector = diskDrive.getCurrentSector();
			diskDrive.homeHeads();
			messageHome = String.format("testTrackHeadSector @HOME - %d", i);
			assertThat(messageHome, diskDrive.getCurrentAbsoluteSector(), equalTo(0));
			
			message = String.format("testTrackHeadSector Track, head, sector - %d", absoluteSector);
			diskDrive.setCurrentAbsoluteSector(head, track, sector);
			assertThat(message,diskDrive.getCurrentAbsoluteSector(),equalTo(absoluteSector));
			System.out.println(message);
		}

	}

}
