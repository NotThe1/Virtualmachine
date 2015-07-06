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
		ArrayList<DiskLayout> diskLayouts = new ArrayList<DiskLayout>();
		DiskLayout selectedDiskLayout = null;

		Path sourcePath = Paths.get(fileLocation, "Disks");
		String fp = sourcePath.resolve(fileLocation).toString();
		JFileChooser chooser = new JFileChooser(fp);

		// get all the valid disk layouts - file filtering
		for (DiskLayout diskLayout : DiskLayout.values()) {
			diskLayouts.add(diskLayout);
			fileExtension = diskLayout.fileExtension;
			fileDescription = diskLayout.descriptor;
			chooser.addChoosableFileFilter(new FileNameExtensionFilter(
					fileDescription, fileExtension));
		}// for
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
			return null; // do nothing
		}// if

		// now we have something to work with
		// String selectedExtension;

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
			for (DiskLayout d : diskLayouts) {
				if (d.descriptor.equals(chooserFilter.getDescription())) {
					fileExtension = d.fileExtension;
					break; // we found it
				}
			}// for
		} else { // there is an ext, but is it valid?
			boolean validExt = false;
			for (DiskLayout d : diskLayouts) {
				if (fileNameComponents[1].equalsIgnoreCase(d.fileExtension)) {
					fileExtension = d.fileExtension;
					validExt = true;
					break;
				}// if - valid?
			}// for
			if (!validExt) {
				String message = String.format("%S is not a valid file extension",
						fileNameComponents[1]);
				JOptionPane.showMessageDialog(null, message, "Make a new disk",
						JOptionPane.ERROR_MESSAGE);
				return null;
			}// if - not valid
		}// if - determine type
		for (DiskLayout d : diskLayouts) {
			if (fileExtension.equalsIgnoreCase(d.fileExtension)) {
				selectedDiskLayout = d;
				break;
			}// if
		}// for - pick disk layout

		// System.out.printf("Base name: %s, Extension %s:%n", fileBaseName, fileExtension);
		// System.out.printf("ParentPath: %s %n", parentPath.getAbsolutePath());
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

		try  {
			FileChannel fc = new RandomAccessFile(f, "rw").getChannel();
			MappedByteBuffer disk = fc.map(FileChannel.MapMode.READ_WRITE, 0, selectedDiskLayout.getTotalBytes());
			ByteBuffer sector = ByteBuffer.allocate(selectedDiskLayout.bytesPerSector);
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
		Byte byteValue = (byte) (value & 0XFF);
		while (sector.hasRemaining()) {
			sector.put(byteValue);
		}// while
		sector.flip();
		return sector;
	}

}// class MakeNewDisk
