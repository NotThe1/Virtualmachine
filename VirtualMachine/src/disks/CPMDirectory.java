package disks;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class CPMDirectory {

	private HashMap<Integer, CPMDirectoryEntry> dirEntries;
	private boolean bigDisk;
	private int maxEntries;
	private int directoryBlockCount;
	private int sectorsPerBlock;
	private int bytesPerSector;
	private int trackOffset; // ofs
	public int sectorsOffset;
	private int maxBlocks;

	private int directoryEntryNumber;

	private HashMap<Integer, Boolean> allocationTable;

	int entriesPerSector;
	int entriesPerBlock;

	public CPMDirectory() {
		this("F5DD", true); // Default to 3.5" and 5" disks
	}

	public CPMDirectory(String diskExtention, boolean bootDisk) {
		DiskMetrics diskMetric = DiskMetrics.diskMetric(diskExtention);
		diskMetric.setBootDisk(bootDisk);
		this.bigDisk = diskMetric.isBigDisk();
		this.maxEntries = diskMetric.getDRM() + 1;
		this.sectorsPerBlock = diskMetric.sectorsPerBlock;
		this.bytesPerSector = diskMetric.bytesPerSector;
		this.trackOffset = diskMetric.getOFS();
		this.maxBlocks = diskMetric.getDSM() + 1;
		this.directoryBlockCount = diskMetric.directoryBlockCount;
		this.sectorsOffset = diskMetric.getDirectoryStartSector();

		resetDirectory();
	}

	public void resetDirectory() {
		entriesPerSector = bytesPerSector / Disk.DIRECTORY_ENTRY_SIZE;
		entriesPerBlock = sectorsPerBlock * entriesPerSector;
		clearDirEntries();
		initializeAllocationTable();
	}

	public int getDirectoryBlockNumber(int directoryEntryNumber) {
		return directoryEntryNumber / entriesPerBlock;
	}

	public int getPhysicalSectorNumber(int directoryEntryNumber) {
		return (directoryEntryNumber / entriesPerSector) + this.sectorsOffset;
	}

	// where in the physical record this belongs
	public int getLogicalRecordIndex(int directoryEntryNumber) {
		return directoryEntryNumber % entriesPerSector;
	}

	public byte[] getRawDirectoryEntry(int directoryIndex) {
		return dirEntries.get(directoryIndex).getRawDirectory();
	}

	public void addEntry(byte[] rawEntry, int directoryEntryNumber) {
		dirEntries.put(directoryEntryNumber, new CPMDirectoryEntry(rawEntry, bigDisk));
		allocateBlocks(directoryEntryNumber);
	}

	public CPMDirectoryEntry getDirectoryEntry(int directoryIndex) {
		return dirEntries.get(directoryIndex);

	}

	public int addEntry(byte[] rawEntry) {
		int entryLocation = this.getNextAvailableEntry();
		addEntry(rawEntry, entryLocation);
		return entryLocation;
	}

	public int updateEntry(String fullName) {
		return updateEntry(0, fullName, 0);
	}

	public int updateEntry(int userNumber, String fullName) {
		return updateEntry(userNumber, fullName, 0);
	}

	public int updateEntry(int userNumber, String fullName, int extentNumber) {
		int entryLocation = this.getNextAvailableEntry();
		CPMDirectoryEntry currentEntry = dirEntries.get(entryLocation);
		currentEntry.resetEntry();
		currentEntry.setFilenameAndType(fullName);
		currentEntry.setUserNumber((byte) (userNumber & 0xFF));
		currentEntry.setActualExtentNumber(extentNumber);
		this.allocateBlocks(entryLocation);
		return entryLocation;
	}

	public int getNextDirectoryExtent(int directoryIndex) {
		int index = directoryIndex;
		if (this.bigDisk) {
			index = getNextDirectoryExtentEntry(directoryIndex);
		} else { // small disk
			CPMDirectoryEntry currentEntry = dirEntries.get(directoryIndex);
			if (currentEntry.getExInt() % 2 == 0) {
				currentEntry.incEx();
				index = directoryIndex;
			} else {// need a whole new entry
				index = getNextDirectoryExtentEntry(directoryIndex);
			}// extent even/odd
		}// big/small disk

		return index;
	}//getNextDirectoryExtent

	public int getNextDirectoryExtentEntry(int directoryIndex) {
		CPMDirectoryEntry currentEntry = dirEntries.get(directoryIndex);
		int currentExtent = currentEntry.getActualExtentNumber();
		byte userNo = currentEntry.getUserNumber();
		String fullName = currentEntry.getNameAndTypePeriod();
		return this.updateEntry(userNo, fullName, currentExtent + 1);
	}// getNextDirectoryExtentEntry

	public int getMoreStorage(int directoryIndex) {
		int nextBlock = this.getNextAvailableBlock();
		dirEntries.get(directoryIndex).addBlock(nextBlock);
		allocationTable.put(nextBlock, true);
		return nextBlock;
	}

	public void deleteFile(int directoryEntryNumber) {
		ArrayList<Integer> blockList = getFilesBlocks(directoryEntryNumber);

		for (Integer block : blockList) {
			allocationTable.remove(block);
		}//
		dirEntries.get(directoryEntryNumber).markAsDeleted();
	}

	public void deleteFile(String fullName) {
		ArrayList<Integer> targetDirectories = getDirectoryEntries(fullName);
		for (Integer de : targetDirectories) {
			deleteFile(de);
		}// for
	}

	public ArrayList<Integer> getFilesBlocks(String fullName) {
		return getFilesBlocks(getDirectoryEntries(fullName));
	}

	public int getFileBlocksCount(String fullName) {
		return getFilesBlocks(fullName).size();
	}

	public ArrayList<Integer> getFilesBlocks(int directoryEntryNumber) {
		return dirEntries.get(directoryEntryNumber).getAllocatedBlocks();
	}

	public ArrayList<Integer> getFilesBlocks(ArrayList<Integer> directoryEntries) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (Integer directoryEntryNumber : directoryEntries) {
			result.addAll(dirEntries.get(directoryEntryNumber).getAllocatedBlocks());
		}
		return result;
	}

	public void allocateBlocks(int directoryEntryNumber) {
		ArrayList<Integer> blockList = getFilesBlocks(directoryEntryNumber);
		for (Integer block : blockList) {
			allocationTable.put(block, true);
		}//
		return;
	}

	public void deAllocateBlocks(int directoryEntryNumber) {
		ArrayList<Integer> blockList = getFilesBlocks(directoryEntryNumber);
		for (Integer block : blockList) {
			allocationTable.remove(block);
		}//
		return;
	}

	public void deAllocateBlocks(ArrayList<Integer> blocks) {
		for (Integer block : blocks) {
			allocationTable.remove(block);
		}//
		return;
	}

	public int getActiveEntryCount() {
		Collection<CPMDirectoryEntry> entries = dirEntries.values();
		return (int) entries.stream().filter(entry -> entry.isEmpty() != true).count();
	}

	public int getAvailableEntryCount() {
		Collection<CPMDirectoryEntry> entries = dirEntries.values();
		return (int) entries.stream().filter(entry -> entry.isEmpty() == true).count();
	}

	public int getNextAvailableEntry() {
		int ans = -1;
		for (int i = 0; i < dirEntries.size(); i++) {
			if (dirEntries.get(i).isEmpty()) {
				ans = i;
				break;
			}// if
		}// for
		return ans;
	}

	private int getNextAvailableBlock() {
		int ans = -1;
		for (Integer i = 0; i < maxBlocks + 1; i++) {
			if (!allocationTable.containsKey(i)) {
				ans = i;
				break;
			}// if
		}// for
		return ans;
	}// getNextAvailableBlock

	public int getAllocatedBlockCount() {
		return allocationTable.size();
	}

	public int getAvailableBlockCount() {
		return maxBlocks - getAllocatedBlockCount();
	}

	public void incrementRc(int directoryIndex, int amount) {
		dirEntries.get(directoryIndex).incrementRc(amount);
	}

	public int getTotalRecordCount(String target) {
		String fullName = makeFileName11(target);
		int totalRecordCount = 0;

		for (int i = 0; i < maxEntries; i++) {
			if (dirEntries.get(i).getNameAndType11().equals(fullName)) {
				totalRecordCount += dirEntries.get(i).getRcInt();
			}// if
		}// for
		return totalRecordCount;
	}

	public ArrayList<Integer> getAllAllocatedBlocks(String target) {
		ArrayList<Integer> targetBlocks = new ArrayList<Integer>();

		ArrayList<Integer> targetEntries = getDirectoryEntries(target);
		for (int i = 0; i < targetEntries.size(); i++) {
			targetBlocks.addAll(dirEntries.get(targetEntries.get(i)).getAllocatedBlocks());
		}
		return targetBlocks;
	}

	public boolean isReadOnly(String fileName) {
		boolean result = false;
		String fullName = makeFileName11(fileName);
		for (int i = 0; i < maxEntries; i++) {
			if (dirEntries.get(i).getNameAndType11().equals(fullName)) {
				result = dirEntries.get(i).isReadOnly();
			}// if
		}// for
		return result;
	}

	public boolean isEntryFull(int directoryIndex) {
		return dirEntries.get(directoryIndex).isEntryFull();
	}

	public boolean isSystemFile(String fileName) {
		boolean result = false;
		String fullName = makeFileName11(fileName);
		for (int i = 0; i < maxEntries; i++) {
			if (dirEntries.get(i).getNameAndType11().equals(fullName)) {
				result = dirEntries.get(i).isSystemFile();
			}// if
		}// for
		return result;
	}

	public ArrayList<Integer> getDirectoryEntries(String target) {
		String fullName = makeFileName11(target);
		HashMap<Integer, Integer> targetEntries = new HashMap<Integer, Integer>();

		for (int i = 0; i < maxEntries; i++) {
			if (dirEntries.get(i).getNameAndType11().equals(fullName)) {

				targetEntries.put(dirEntries.get(i).getActualExtentNumber(), i);
			}// if
		}// for

		// we have all entries that match the name & type with the directory index & extent number
		ArrayList<Integer> ans = new ArrayList<Integer>();
		Set<Integer> extentNumbers = targetEntries.keySet();
		for (Integer extentNuber : extentNumbers) {
			ans.add(targetEntries.get(extentNuber)); // get the indexes in order
		}
		return ans;
	}

	private static String padEntryField(String field, int fieldLength) {
		String result = field.trim().toUpperCase();
		result = result.length() > fieldLength ? result.substring(0, fieldLength) : result;
		result = String.format("%-" + fieldLength + "s", result);
		return result;
	}

	private void clearDirEntries() {
		if (dirEntries != null) {
			dirEntries = null;
		}
		dirEntries = new HashMap<Integer, CPMDirectoryEntry>();
		for (int i = 0; i < maxEntries; i++) {
			dirEntries.put(i, CPMDirectoryEntry.emptyDirectoryEntry(this.bigDisk));
		}
	}

	private void initializeAllocationTable() {
		if (allocationTable != null) {
			allocationTable = null;
		}// for

		allocationTable = new HashMap<Integer, Boolean>();
		for (int i = 0; i < this.directoryBlockCount; i++) {
			allocationTable.put(i, true);
		}// for

	}// initializeAllocationTable

	public void refreshAllocationTable() {
		initializeAllocationTable();
		for (int i = 0; i < maxEntries; i++) {
			if (!dirEntries.get(i).isEmpty()) {
				allocateBlocks(i);
			}// if
		}// for

	}// refreshAllocationTable

	public static String makeFileName11(String fullName) {
		String[] parts = fullName.split("\\" + Disk.PERIOD);

		String name = padEntryField(parts[0].trim().toUpperCase(), Disk.NAME_MAX);
		String type = fullName.contains(Disk.PERIOD) ? padEntryField(parts[1].trim().toUpperCase(), Disk.TYPE_MAX)
				: "   ";
		return name + type;
	}// makeFileName11

	public void markAsDeleted(int directoryEntryNumber) {
		dirEntries.get(directoryEntryNumber).markAsDeleted();
	}// markAsDeleted

}
