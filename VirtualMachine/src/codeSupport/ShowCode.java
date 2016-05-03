package codeSupport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import java.awt.GridBagLayout;

import javax.swing.JScrollPane;

import java.awt.GridBagConstraints;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingConstants;

public class ShowCode extends JFrame {
	class Limits {
		public int start;
		public int end;

		public Limits() {
			this(-1, -1);
		}

		public Limits(int start, int end) {
			this.start = start;
			this.end = end;
		}// constructor
	}// class Limits

	private JPanel contentPane;
	private int programCounter;

	Pattern limitStart = Pattern.compile("CodeStart:",Pattern.CASE_INSENSITIVE);
	Pattern limitEnd = Pattern.compile("CodeEnd:",Pattern.CASE_INSENSITIVE);
	Pattern pLineNumber = Pattern.compile("^[0-9]{4}:\\s[a-f,A-F,0-9]{4}");

	private HashMap<String, Limits> fileList; // HashMap<AbsoluteFIlePath,Limits>
	private HashMap<String, String> listings; // HashMap<AbsoluteFIlePath,contents>
	private String defaultDirectory = "'";
	private JLabel lblHeader;
	private final static String NO_FILE = "No Active File";
	private final static String SOURCE_FILE = "SourceFile";
	// private final static String SPACE = " ";
	private String currentFilePath;
	private JTextArea txtDisplay;
//	private JEditorPane txtDisplay;
	private Document doc;
	private boolean fileIsCurrent;

	private int currentStart, currentEnd;
	private JScrollPane scrollPane;
	private JMenu mnuFiles;
	private JCheckBoxMenuItem mnuNew;

	public void setProgramCounter(int programCounter) {
		this.programCounter = programCounter & 0XFFFF;
		setFileToShow(programCounter);

		if (currentFilePath == null) {
			// notified user already.
			return; // not much do do
		}// if

		if (!fileIsCurrent) { // file is notcurrent file
			loadDisplay(currentFilePath);
		}// if

		selectTheCorrectLine();
	}// setProgramCounter

	private void selectTheCorrectLine() {
		String taregetAddress = String.format("%04X ", programCounter);
		doc = txtDisplay.getDocument();
		int posStart = 0;
		int posEnd = 0;
		boolean notTheTargetLine = true;
		try {
			while (notTheTargetLine) {
				posStart = doc.getText(0, doc.getLength()).indexOf(taregetAddress, posStart + 1);
				posEnd = doc.getText(posStart, doc.getLength() - posStart).indexOf("\n");

				if (!doc.getText(posStart - 1, 1).equals("\n")) {
					posStart = posStart + posEnd;
					continue;
				}//

				if (!doc.getText(posStart + 6, 2).equals("  ")) {// posStart + 9, 2)
					notTheTargetLine = false;
				}// if we have the line!

			}// while
		} catch (BadLocationException e) {
			System.out.printf("Instruction at %04X not found in Targeted Files%n", programCounter);
			return;
		}// try

		txtDisplay.setSelectionStart(posStart);
		txtDisplay.setSelectionEnd(posStart + posEnd);
	}

	private boolean isLineInCurrentFile(int lineNumber) {
		boolean isLineInCurrentFile = false;
		if ((lineNumber >= currentStart) && (lineNumber <= currentEnd)) {
			isLineInCurrentFile = true;
		}// if
		return isLineInCurrentFile;
	}// isLineInCurrentFile

	private boolean isLineInThisFile(int lineNumber, String filePath) {
		boolean isLineInThisFile = false;
		Limits thisFilesLimit = fileList.get(filePath);
		if ((lineNumber >= thisFilesLimit.start) && (lineNumber <= thisFilesLimit.end)) {
			currentFilePath = filePath;
			currentStart = thisFilesLimit.start;
			currentEnd = thisFilesLimit.end;
			isLineInThisFile = true;
		}// if
		return isLineInThisFile;
	}// isLineInThisFile

	private void setFileToShow(int lineNumber) {
		// returns true if number is in the currently loaded file
		fileIsCurrent = false;
		if (isLineInCurrentFile(lineNumber)) {
			fileIsCurrent = true;
			return; // everything is in place
		}//

		// Limits thisFilesLimit = new Limits();
		boolean weHaveAFile = false;

		Set<String> filePaths = fileList.keySet();
		for (String filePath : filePaths) {
			if (isLineInThisFile(lineNumber, filePath)) {
				weHaveAFile = true;
				currentFilePath = filePath;
				Limits thisLimits = fileList.get(filePath);
				currentStart = thisLimits.start;
				currentEnd = thisLimits.end;
				break;
			}// if
		}// for

		if (!weHaveAFile) {
			clearCurrentIndicaters();
			System.out.printf("Target line: %04X Not File in Currently Loaded Files%n", lineNumber);
			// JOptionPane.showMessageDialog(null,
			// "Target line Not File in Currently Loaded Files : "
			// + lineNumber, "Not here",
			// JOptionPane.ERROR_MESSAGE);
		}// if file not found
		return;
	}// getFileToShow

	private JFileChooser getFileChooser(String directory, String filterDescription, String filterExtensions) {
		return getFileChooser(directory, filterDescription, filterExtensions, false);

	}// getFileChooser - single select

	private JFileChooser getFileChooser(String directory, String filterDescription, String filterExtensions,
			boolean multiSelect) {
		// Path sourcePath = Paths.get(directory);
		// String fp = sourcePath.resolve(defaultDirectory).toString();

		JFileChooser chooser = new JFileChooser(directory);
		chooser.setMultiSelectionEnabled(multiSelect);
		chooser.addChoosableFileFilter(new FileNameExtensionFilter(filterDescription, filterExtensions));
		chooser.setAcceptAllFileFilterUsed(false);
		return chooser;
	}// getFileChooser

	private String restructureLine(String line) {
		String restructureLine = null;
		if (line.length() < 6) {
			restructureLine = null; // force something
		} else {
			restructureLine = String.format("%s%n", line.substring(6));
			// System.out.printf("%s%n", line.substring(6));
		}// if

		return restructureLine;
	}

	private int getLineNumber(String line) {
		int getLineNumber = -1;
		Matcher mLineNumber = pLineNumber.matcher(line);
		if (mLineNumber.find()) {
			getLineNumber = Integer.valueOf(mLineNumber.group().substring(6, 10), 16);
		}// if
		return getLineNumber;
	}// getLineNumber

	private void loadDisplay(String filePath) {
		doc = txtDisplay.getDocument();
		try {
			doc.remove(0, doc.getLength());
			doc.insertString(0, listings.get(filePath), null);
			lblHeader.setText(filePath);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// try
	}// loadDisplay

	private void clearCurrentIndicaters() {
		currentFilePath = null;
		currentStart = -1;
		currentEnd = -1;
		fileIsCurrent = false;
	}

	private void addFileToApp(String newFilePath) {
		try {
			FileReader fileReader = new FileReader((newFilePath));
			BufferedReader reader = new BufferedReader(fileReader);
			String line, adjustedLine;
			// int lineNumber = 0;
			doc.putProperty(PlainDocument.tabSizeAttribute, 4);
			try { // clear it out);
				doc.remove(0, doc.getLength());
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}// try

			Limits limits = new Limits();
			Boolean displayLine = false;

			StringBuilder sb = new StringBuilder();

			while ((line = reader.readLine()) != null) {
				Matcher mStart = limitStart.matcher(line);
				if (mStart.find()) { // don't save until we get the CodeStart:
					limits.start = getLineNumber(line);
					displayLine = true;
				}// if start
				Matcher mEnd = limitEnd.matcher(line);
				if (mEnd.find()) { // stop the save because we reached the CodeEnd:
					limits.end = getLineNumber(line);
					break;
				}// if end

				if (displayLine) {
					adjustedLine = restructureLine(line);
					sb.append(adjustedLine);
				}// if
			}// while

			fileList.put(newFilePath, limits);
			listings.put(newFilePath, sb.toString());
			loadDisplay(newFilePath);
			txtDisplay.setCaretPosition(0);
			reader.close();
		} catch (FileNotFoundException fnfe) {
			JOptionPane.showMessageDialog(null, newFilePath
					+ "not found", "unable to locate",
					JOptionPane.ERROR_MESSAGE);
			return; // exit gracefully
		} catch (IOException ie) {
			JOptionPane.showMessageDialog(null, newFilePath
					+ ie.getMessage(), "IO error",
					JOptionPane.ERROR_MESSAGE);
			return; // exit gracefully
		}
		clearCurrentIndicaters();
		File newFile = new File(newFilePath);

		defaultDirectory = newFile.getParent();
		setTitle(defaultDirectory);
		// fileList.put(sourceFile, new Point(0,0));
		String thisFileName = newFile.getName();
		mnuNew = new JCheckBoxMenuItem(thisFileName);
		mnuNew.setName(newFile.getAbsolutePath());
		mnuNew.setActionCommand(thisFileName);
		mnuFiles.add(mnuNew);

		return;

	}// addFileToApp

	private void resetApp() {
		fileList.clear();
		listings.clear();
		lblHeader.setText(NO_FILE);
		try {
			doc.remove(0, doc.getLength());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fileIsCurrent = false;
		currentStart = -1;
		currentEnd = -1;
		currentFilePath = null;

	}

	// -------------------------------------------------

	private void appInit() {
		fileList = new HashMap<String, Limits>();
		listings = new HashMap<String, String>();
		lblHeader = new JLabel(NO_FILE);
		lblHeader.setHorizontalAlignment(SwingConstants.CENTER);
		lblHeader.setFont(new Font("Courier New", Font.BOLD, 16));
		lblHeader.setForeground(Color.BLUE);
		scrollPane.setColumnHeaderView(lblHeader);
		txtDisplay.setTabSize(10);
		doc = txtDisplay.getDocument();
		fileIsCurrent = false;
		currentStart = -1;
		currentEnd = -1;
		currentFilePath = null;

	}

	// -------------------------------------------------
	public ShowCode() {
		initialize();
		appInit();
		this.setLocation(920, 200);
		this.setVisible(true);
		
	}

	private void initialize() {
		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 965, 850);		//original 100,100,669,674

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnuFiles = new JMenu("Files");
		menuBar.add(mnuFiles);

		JMenuItem mnuFilesAdd = new JMenuItem("Add Files...");
		mnuFilesAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = getFileChooser(defaultDirectory, "Listing Files", "List", true);
				if (fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
					System.out.printf("You cancelled the Load File...%n", "");
					return;
				} // if

				File[] newFiles = fc.getSelectedFiles();
				for (File newFile : newFiles) {
					addFileToApp(newFile.getAbsolutePath());
					defaultDirectory =newFile.getParent();
					// if (addFileToApp(newFile.getAbsolutePath())) {
					// defaultDirectory = newFile.getParent();
					// setTitle(defaultDirectory);
					// // fileList.put(sourceFile, new Point(0,0));
					// String thisFileName = newFile.getName();
					// JCheckBoxMenuItem mnuNew = new JCheckBoxMenuItem(thisFileName);
					// mnuNew.setName(newFile.getAbsolutePath());
					// mnuNew.setActionCommand(thisFileName);
					// mnuFiles.add(mnuNew);
					// }// only add if we built the HashMaps ok
				}// for each

			}// actionPerformed
		});
		mnuFilesAdd.setActionCommand("mnuFilesAdd");
		mnuFiles.add(mnuFilesAdd);

		JMenuItem mnuFilesRemove = new JMenuItem("Remove All  File ...");
		mnuFilesRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				int itemCount = mnuFiles.getItemCount();
				for (int i = itemCount - 1; i > 0; i--) {
					if (mnuFiles.getItem(i) instanceof JCheckBoxMenuItem) {
						JCheckBoxMenuItem mi = (JCheckBoxMenuItem) mnuFiles.getItem(i);
						mnuFiles.remove(mi);
					}// outer if
				}// for
				resetApp();

			}// actionPerformed
		});

		JMenuItem mnuFileAddList = new JMenuItem("Add Files from List");
		mnuFileAddList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = getFileChooser("Lists", "Listing Files", "ListSet");
				if (fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
					System.out.printf("You cancelled the Add Files from List...%n", "");
					return;
				}// if
				FileReader fileReader;
				try {
					fileReader = new FileReader((fc.getSelectedFile().getAbsolutePath()));
					BufferedReader reader = new BufferedReader(fileReader);
					String rawFilePathName = null;
					String filePathName = null;
					while ((rawFilePathName  = reader.readLine()) != null) {
						filePathName = rawFilePathName.replaceAll("mem\\z", "list");
						addFileToApp(filePathName);
					}// for each
					reader.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}// actionPerformed
		});
		mnuFiles.add(mnuFileAddList);

		JSeparator separator_1 = new JSeparator();
		mnuFiles.add(separator_1);
		mnuFilesRemove.setActionCommand("mnuFilesRemove");
		mnuFiles.add(mnuFilesRemove);

		JMenuItem mnuFilesRemoveSelected = new JMenuItem("Remove Selected Files");
		mnuFilesRemoveSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				int itemCount = mnuFiles.getItemCount();
				for (int i = itemCount - 1; i > 0; i--) {
					if (mnuFiles.getItem(i) instanceof JCheckBoxMenuItem) {
						JCheckBoxMenuItem mi = (JCheckBoxMenuItem) mnuFiles.getItem(i);
						if (mi.getState()) {
							listings.remove(mi.getName());
							fileList.remove(mi.getName());
							mnuFiles.remove(mi);
						}// inner if - selected = true
					}// outer if
				}// for

				lblHeader.setText(NO_FILE);
				try {
					doc.remove(0, doc.getLength());
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fileIsCurrent = false;
				currentStart = -1;
				currentEnd = -1;
				currentFilePath = null;
			}
		});
		mnuFiles.add(mnuFilesRemoveSelected);

		JSeparator separator_2 = new JSeparator();
		mnuFiles.add(separator_2);

		JMenuItem mnuFilesSaveSelected = new JMenuItem("Save Selected as List");
		mnuFilesSaveSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = getFileChooser("Lists", "Listing Set Files", "ListSet");
				if (fc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
					System.out.printf("You cancelled theSave Selected as List...%n", "");
					return;
				}// if
				String destinationPath = fc.getSelectedFile().getAbsolutePath();

				try {
					FileWriter fileWriter = new FileWriter(destinationPath + ".ListSet");
					BufferedWriter writer = new BufferedWriter(fileWriter);

					int itemCount = mnuFiles.getItemCount();
					for (int i = itemCount - 1; i > 0; i--) {

						if (mnuFiles.getItem(i) instanceof JCheckBoxMenuItem) {
							JCheckBoxMenuItem mi = (JCheckBoxMenuItem) mnuFiles.getItem(i);
							if (mi.getState()) {
								writer.write(mi.getName() + "\n");
							}// inner if - selected = true
						}// outer if
					}// for

					writer.close();
				} catch (Exception e1) {
					e1.printStackTrace();
					return;
				}// try

			}
		});
		mnuFiles.add(mnuFilesSaveSelected);

		JSeparator separator = new JSeparator();
		mnuFiles.add(separator);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		contentPane.add(scrollPane, gbc_scrollPane);

		txtDisplay = new JTextArea();
//		txtDisplay = new JEditorPane();
		txtDisplay.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				if (me.getClickCount() >= 2) {
					selectTheCorrectLine();
				}
			}
		});
		txtDisplay.setEditable(false);
		txtDisplay.setFont(new Font("Courier New", Font.PLAIN, 14));
		scrollPane.setViewportView(txtDisplay);
	}

}
