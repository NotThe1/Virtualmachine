package disks;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Vector;

public class DiskDrive {
	private int heads;
	private int currentHead;
	private int tracksPerHead;
	private int currentTrack;
	private int sectorsPerTrack;
	private int currentSector;
	private int currentAbsoluteSector;
	private int bytesPerSector;
	private int sectorsPerHead;
	private int totalSectorsOnDisk;
	private long totalBytesOnDisk;
	private String fileAbsoluteName;
	private String fileLocalName;

	private FileChannel fileChannel;
	private MappedByteBuffer disk;
	private byte[] readSector;
	private ByteBuffer writeSector;
	
	public DiskDrive(Path path){
		this(path.resolve(path).toString());
	}

	public DiskDrive(String strPathName) {
		//String strPathName = path.resolve(path).toString();
		resolveDiskType(strPathName);

		try {
			File file = new File(strPathName);
			fileChannel = new RandomAccessFile(file,"rw").getChannel();
			disk = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0,fileChannel.size());// this.totalBytesOnDisk);
			fileAbsoluteName = file.getAbsolutePath();
			fileLocalName = file.getName();
		} catch (IOException e) {
			fireVDiskError((long) 1, "Physical I/O error" + e.getMessage());
			System.err.printf("Physical I/O error - %s%n", e.getMessage());
		}// try
		readSector = new byte[bytesPerSector];
		writeSector = ByteBuffer.allocate(bytesPerSector);

	}// Constructor

	private void resolveDiskType(String drive) {
		String[] fileNameComponents = drive.split("\\.");
		if (fileNameComponents.length != 2) {
			fireVDiskError((long) 0, "Bad Disk - " + drive);
			return;
		}//if
		String fileExtension = fileNameComponents[1]; // have the file extension
		boolean validFile = false;
		for (DiskLayout diskLayout : DiskLayout.values()) {
			if (fileNameComponents[1].equalsIgnoreCase(diskLayout.fileExtension)) {
				this.heads = diskLayout.heads;
				this.tracksPerHead = diskLayout.tracksPerHead;
				this.sectorsPerTrack = diskLayout.sectorsPerTrack;
				this.bytesPerSector = diskLayout.bytesPerSector;
				this.sectorsPerHead = diskLayout.getTotalSectorsPerHead();
				this.totalSectorsOnDisk = diskLayout.getTotalSectorsOnDisk();
				this.totalBytesOnDisk = diskLayout.getTotalBytes();
				validFile = true;
				break;
			}// if
		}// for
		if (!validFile) {
			fireVDiskError((long) 2, "Not a Valid disk type " + fileNameComponents[1]);
		}// if
	}// resolveDiskType
	
	public String getFileAbsoluteName(){
		return this.fileAbsoluteName;
	}//getFileAbsoluteName
	public String getFileLocalName(){
		return this.fileLocalName;
	}//getFileLocalName
	
	public void homeHeads(){
		this.currentHead = 0;
		this.currentTrack = 0;
		this.currentSector =1;
		this.currentAbsoluteSector=0;
	}

	private void setSectorPosition() {
		int offset = currentAbsoluteSector * bytesPerSector;
		//System.out.printf("offset = %d, disk size = %d%n",offset,disk.capacity());
		disk.position(offset);
	}// setSectorPosition

	public byte[] read() {
		setSectorPosition();
		disk.get(readSector);
		return readSector;
	}// read

	public byte[] readNext() {
		setCurrentAbsoluteSector(currentAbsoluteSector + 1);
		return read();
	}// readnext

	public void write(byte[] sector) {
		writeSector.clear();

		if (sector.length != bytesPerSector) {
			fireVDiskError((long) sector.length, "Wrong sized sector");
		} else {
			//writeSector.put(sector);
			setSectorPosition();
			//disk.put(writeSector);
			disk.put(sector);
		}// if

	}// write

	public void writeNext(byte[] sector) {
		setCurrentAbsoluteSector(currentAbsoluteSector + 1);
		write(sector);
		return;
	}// readnext
	
	public long getTotalBytes(){
		return this.totalBytesOnDisk;
	}

	public int getCurrentHead() {
		return currentHead;
	}

	public void setCurrentHead(int currentHead) {
		if (validateHead(currentHead)) {
			this.currentHead = currentHead;
		}// if
	}// setCurrentHead

	public int getCurrentTrack() {
		return currentTrack;
	}// getCurrentTrack

	public int getBytesPerSector() {
		return this.bytesPerSector;
	}

	public void setCurrentTrack(int currentTrack) {
		if (validateTrack(currentTrack)) {
			this.currentTrack = currentTrack;
		}// if
	}// setCurrentTrack

	public int getCurrentSector() {
		return currentSector;
	}// getCurrentSector

	public void setCurrentSector(int currentSector) {
		if (validateSector(currentSector)) {
			this.currentSector = currentSector;
		}// if
	}// setCurrentSector

	public int getCurrentAbsoluteSector() {
		return currentAbsoluteSector;
	}// getCurrentAbsoluteSector

	public void setCurrentAbsoluteSector(int currentAbsoluteSector) {
		if (validateAbsoluteSector(currentAbsoluteSector)) {
			this.currentAbsoluteSector = currentAbsoluteSector;
			setCurrentHead((int) currentAbsoluteSector / sectorsPerHead);
			int netTrack = currentAbsoluteSector
					- (currentHead * sectorsPerHead);
			setCurrentTrack(netTrack / sectorsPerTrack);
			setCurrentSector(1 + (currentAbsoluteSector % sectorsPerTrack));
		}
	}// setCurrentAbsoluteSector

	public void setCurrentAbsoluteSector(int head, int track, int sector) {
		if (validateHead(head) & validateSector(sector) & validateTrack(track)) {
			int absoluteSector = ((head) * sectorsPerHead)
					+ (track * sectorsPerTrack) + (sector - 1);
			if (validateAbsoluteSector(absoluteSector)) {
				this.currentAbsoluteSector = absoluteSector;
				this.currentHead = head;
				this.currentTrack = track;
				this.currentSector = sector;
			}// inner if
		}// outer if
	}// setCurrentAbsoluteSector

	private boolean validateHead(int head) {
		boolean validateHead = true;
		// between 0 and heads-1
		if (!((head >= 0) & (head < heads))) {
			homeHeads();		
			fireVDiskError((long) head, "Bad head");
			validateHead = false;
		}// if
		return validateHead;
	}//

	private boolean validateTrack(int track) {
		boolean validateTrack = true;
		// between 0 and tracksPerHead-1
		if (!((track >= 0) & (track < tracksPerHead))) {
			homeHeads();
			fireVDiskError((long) track, "Bad track");
			validateTrack = false;
		}// if
		return validateTrack;
	}//

	private boolean validateSector(int sector) {
		// between 1 andsectorsPerTrack
		boolean validateSector = true;
		if (!((sector > 0) & (sector <= sectorsPerTrack))) {
			homeHeads();
			fireVDiskError((long) sector, "Bad Sector");
			validateSector = false;
		}// if
		return validateSector;
	}//

	private boolean validateAbsoluteSector(long absoluteSector) {
		// between 0 and totalSectorsOnDisk-1
		boolean validateAbsoluteSector = true;
		if (!((absoluteSector >= 0) & (absoluteSector < totalSectorsOnDisk))) {
			homeHeads();
			fireVDiskError((long) absoluteSector, "Bad absoluteSector");
			validateAbsoluteSector = false;
		}// if
		return validateAbsoluteSector;
	}//

	// ---------------------------------Error
	// Events----------------------------------------

	private Vector<VDiskErrorListener> vdiskErrorListeners = new Vector<VDiskErrorListener>();

	public synchronized void addVDiskErroListener(VDiskErrorListener vdel) {
		if (vdiskErrorListeners.contains(vdel)) {
			return; // Already has it
		}// if
		vdiskErrorListeners.addElement(vdel);
	}// addVDiskErroListener

	public synchronized void removeVDiskErroListener(VDiskErrorListener vdel) {
		vdiskErrorListeners.remove(vdel);
	}// addVDiskErroListener

	private void fireVDiskError(long value, String errorMessage) {
		Vector<VDiskErrorListener> vdel;
		synchronized (this) {
			vdel = (Vector<VDiskErrorListener>) vdiskErrorListeners.clone();
		}// sync
		int size = vdel.size();
		if (size == 0) {
			return; // no listeners
		}// if

		VDiskErrorEvent vdiskErrorEvent = new VDiskErrorEvent(this, value,
				errorMessage);
		for (int i = 0; i < size; i++) {
			VDiskErrorListener listener = (VDiskErrorListener) vdel
					.elementAt(i);
			listener.vdiskError(vdiskErrorEvent);
		}// for

	}// fireVDsikError

}// MyDiskDrive
