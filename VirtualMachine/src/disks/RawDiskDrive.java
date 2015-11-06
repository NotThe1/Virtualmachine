package disks;

import java.nio.file.Path;

public class RawDiskDrive extends DiskDrive {

	public RawDiskDrive(Path path) {
		super(path);
		// TODO Auto-generated constructor stub
	}

	public RawDiskDrive(String strPathName) {
		super(strPathName);
		// TODO Auto-generated constructor stub
	}
	
	public int  getHeads(){
		return heads;
	}
	public int getTracksPerHead(){
		return tracksPerHead;
	}
	public int getSectorsPerTrack(){
		return sectorsPerTrack;
	}
	public int getBytesPerSector(){
		return bytesPerSector;
	}
	public int getSectorsPerHead(){
		return sectorsPerHead;
	}
	public int getTotalSectorsOnDisk(){
		return totalSectorsOnDisk;
	}
	public int getTotalTracks(){
		return heads * tracksPerHead;
	}
	public long getTotalBytesOnDisk(){
		return totalBytesOnDisk;
	}
	

}
