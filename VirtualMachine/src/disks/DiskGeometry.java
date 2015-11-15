package disks;

public class DiskGeometry {
	private static final int CPM_SECTOR = 128;		//  0X80
	private static final int EXTENTCAPACITY = 128;		//  0X80
	
	private int numberOfHeads;
	private int tracksPerHead;
	private int sectorsPerTrack;
	private int bytesPerSector;
	private int blockSizeFactor;
	
	
	
//	private int physicalSectorCount;
	
	
	public DiskGeometry() {
		// TODO Auto-generated constructor stub
	}
	public int getPhysicalSectorCount(){
		int psc = getNumberOfHeads() * getTracksPerHead() * getSectorsPerTrack();
		return psc;
	}
	public long getCapacity(){
		long cap = bytesPerSector * getPhysicalSectorCount();
		return cap;
	}	
	public int getLogicalSectorSize(){
		int lss= CPM_SECTOR;
		return lss;
	}
	public int getLogicalTrack(){
		int lt = getNumberOfHeads() * getTracksPerHead();
		return lt;
	}	
	public int getExtentCapacity(){
		int ec = EXTENTCAPACITY;
		return ec;
	}	
	public int getLogicalExtent(){
		int le = getExtentCapacity() * getLogicalSectorSize();
		return le;
	}
	public  int getPhysicalExtent(){
		int pe = getExtentCapacity() * getBlockSize();
		return pe;
	}
	
	


	public int getNumberOfHeads() {
		return numberOfHeads;
	}


	public void setNumberOfHeads(int numberOfheads) {
		this.numberOfHeads = numberOfheads > 0?numberOfheads:1;
	}


	public int getTracksPerHead() {
		return tracksPerHead;
	}
	
	public int getBlockSize(){
		int bs = getLogicalSectorSize() *  getBlockSizeFactor();
		return bs;
	}


	public void setTracksPerHead(int tracksPerHead) {
		this.tracksPerHead = tracksPerHead > 0?tracksPerHead:1;
	}


	public int getSectorsPerTrack() {
		return sectorsPerTrack;
	}


	public void setSectorsPerTrack(int sectorsPerHead) {
		this.sectorsPerTrack = sectorsPerHead > 0?sectorsPerHead:1;
	}


	public int getBytesPerSector() {
		return bytesPerSector;
	}


	public void setBytesPerSector(int bytesPerSector) {
		this.bytesPerSector = bytesPerSector > 0?bytesPerSector:1;
	}


	public int getBlockSizeFactor() {
		return blockSizeFactor;
	}


	public void setBlockSizeFactor(int blockSizeFactor) {
		this.blockSizeFactor = blockSizeFactor >0?blockSizeFactor:1;
	}

}
