package disks;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MakeNewDisk {

	public static String makeNewDisk() {
		String targetFileName = null;
		String fileLocation = ".";
		String fileDescription = null;
		String fileExtension = null;
		String fileBaseName = null;
		DiskMetrics diskMetric;

		Path sourcePath = Paths.get(fileLocation, "Disks");
		String fp = sourcePath.resolve(fileLocation).toString();
		JFileChooser chooser = new JFileChooser(fp);

		String[] fileTypes = DiskMetrics.getDiskTypes();
		String[] fileDesc = DiskMetrics.getDiskDescriptionss();
		for (int i = 0; i < fileTypes.length; i++) {
			chooser.addChoosableFileFilter(new FileNameExtensionFilter(fileDesc[i], fileTypes[i]));
		}// for

		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
			return null; // do nothing
		}// if

		javax.swing.filechooser.FileFilter chooserFilter = chooser
				.getFileFilter();
		File selectedFile = chooser.getSelectedFile();
		String fileName = selectedFile.getName();
		File parentPath = selectedFile.getParentFile();

		String[] fileNameComponents = fileName.split("\\.");
		fileBaseName = fileNameComponents[0]; // have the base name

		if (chooserFilter.accept(selectedFile)) { // has name and valid extension
			fileExtension = fileNameComponents[1];
		} else if (fileNameComponents.length == 1) {// no extension, use selected form filter
			for (int i = 0; i < fileDesc.length; i++) {
				if (fileDesc[i].equals(chooserFilter.getDescription())) {
					fileExtension = fileTypes[i];
				}
			}
		} else { // there is an ext, but is it valid?
			boolean validExt = false;
			for (String fileType : fileTypes) {
				if (fileType.equals(fileExtension)) {
					validExt = true;
					break;
				}// if
			}// for
			if (!validExt) {
				String message = String.format("%S is not a valid file extension",
						fileNameComponents[1]);
				JOptionPane.showMessageDialog(null, message, "Make a new disk",
						JOptionPane.ERROR_MESSAGE);
				return null;
			}// if - not valid
		}// if - determine type
		diskMetric = DiskMetrics.diskMetric(fileExtension);
		targetFileName = fileBaseName + "." + fileExtension;
		String separator = File.separator;
		String targetAbsoluteFIleName = parentPath.getAbsolutePath() + separator + targetFileName;

		File f = new File(targetAbsoluteFIleName);
		if (f.exists()) {
			if (JOptionPane.showConfirmDialog(null,
					"File already exists do you want to overwrite it?",
					"YES - Continue, NO - Cancel", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
				return null;
			} else {
				f.delete();
				f = null;
				f = new File(targetAbsoluteFIleName);
			}// inner if

		}// if - file exists

		try {
			FileChannel fc = new RandomAccessFile(f, "rw").getChannel();
			MappedByteBuffer disk = fc.map(FileChannel.MapMode.READ_WRITE, 0, diskMetric.getTotalBytes());
			ByteBuffer sector = ByteBuffer.allocate(diskMetric.bytesPerSector);
			int sectorCount = 0;
			while (disk.hasRemaining()) {
				sector = setUpBuffer(sector, sectorCount++);
				disk.put(sector);
			}
			fc.force(true);
			fc.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		chooser = null;
		return targetAbsoluteFIleName;
	}// makeNewDisk

	private static ByteBuffer setUpBuffer(ByteBuffer sector, int value) {
		sector.clear();
		// set value to be put into sector
		Byte byteValue = (byte) 0x00; // default to null
		Byte MTfileVlaue = (byte) 0xE5; // feleted file value
		Byte workingValue;
		while (sector.hasRemaining()) {
			workingValue = ((sector.position() % 0x20) == 0) ? MTfileVlaue : byteValue;
			sector.put(workingValue);
		}// while
		sector.flip();
		return sector;
	}

}// class MakeNewDisk
