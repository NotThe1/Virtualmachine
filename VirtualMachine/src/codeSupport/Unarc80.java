package codeSupport;

import java.awt.EventQueue;

import javax.swing.JFrame;

import java.awt.GridBagLayout;

import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JPanel;

import java.awt.GridBagConstraints;

import javax.swing.JLabel;

import java.awt.Font;

import javax.swing.JTabbedPane;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Unarc80 implements ActionListener{

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Unarc80 window = new Unarc80();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		String actionCommand = actionEvent.getActionCommand();
		
		switch(actionCommand){
		case AC_MNU_FILE_OPEN:
			sourceFile = getNativeFile();
			openSourceFile(sourceFile);
			fileIsOpen(sourceFile != null);
			break;
		case AC_MNU_FILE_CLOSE:
			sourceFile = null;
			fileIsOpen(sourceFile != null);
			break;
		case AC_MNU_FILE_EXIT:
			appClose();
			break;
		}//switch
		
	}//actionPerformed
	
	private void openSourceFile(File sourceFile){
		if (sourceFile == null) {
			JOptionPane.showMessageDialog(null, " Need to select a source file", "Open Source File",
					JOptionPane.WARNING_MESSAGE);
			return;
		}// if
		
		FileChannel fcIn = null;
		FileInputStream fIn = null;
		byte[] arcHeaderRaw;
		MappedByteBuffer mbb;
		int arcHeaderBase =0;
		try {
			fIn = new FileInputStream(sourceFile);
			fcIn = fIn.getChannel();
			arcHeaderRaw = new byte[ARC_HEADER_SIZE];
			mbb = fcIn.map(FileChannel.MapMode.READ_ONLY, 0, sourceSize);
			
			while(isHeader(mbb,arcHeaderBase)){
				mbb.position(arcHeaderBase);
				mbb.get(arcHeaderRaw, 0, ARC_HEADER_SIZE);
				ArcHeader arcHeader = new ArcHeader(arcHeaderBase,arcHeaderRaw);
				String name = arcHeader.getName();
				int stored = arcHeader.getStored();
				int length = arcHeader.getLength();
				int saved = arcHeader.getSaved();
				
				System.out.printf("File name is : %s, base is : %06X Length is : %,7d, Stored is :%,7d%n",
						arcHeader.getNameFixed(),arcHeader.getBaseAddress(),length,stored);
				
				arcHeaderBase +=stored + ARC_HEADER_SIZE;
				int a = 0;

			}//while
			fcIn.close();
			fIn.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}//try		
	}//openSourceFile
	
	boolean isHeader(MappedByteBuffer mbb,  int arcHeaderStart){
				
		if (arcHeaderStart >= (sourceSize-2)){
			return false;		// end of file
		}
		mbb.position(arcHeaderStart);
		byte[] type = new byte[2];
		mbb.get(type, 0, 2);
		if (Arrays.equals(type, HEADER_FLAG)){
			return true;
		}else{
			return false;	
		}//if
		
		
	}
	
	private void fileIsOpen(boolean state){
		mnuFileOpen.setEnabled(!state);
		mnuFileClose.setEnabled(state);
		
		if(state){
			lblSourceFileName.setText(sourceFile.getName());
			lblSourceFileName.setToolTipText(sourceFile.getAbsolutePath());
			
		}else{
			lblSourceFileName.setText(NO_FILE_SELECTED);
			lblSourceFileName.setToolTipText(NO_FILE_SELECTED);			
		}//if
		
	}//fileIsOpen

	private  File getNativeFile() {
		sourceFile = pickNativeFile();
		
		if (sourceFile != null) {
			sourceSize = sourceFile.length();
		}//if
		return sourceFile;
	}// getNativeFile

	private File pickNativeFile() {
		return pickNativeFile(false);
	}// pickNativeFile
	
	private File pickNativeFile(boolean write) {
		String fileLocation = ".";
		Path sourcePath = Paths.get(fileLocation);

		JFileChooser nativeChooser = new JFileChooser(sourcePath.resolve(fileLocation).toString());
		nativeChooser.setMultiSelectionEnabled(false);
		if (nativeChooser.showDialog(null, "Select the file") != JFileChooser.APPROVE_OPTION) {
			return null;
		}// if
		return nativeChooser.getSelectedFile();
	}// pickNativeFile

	//---------------------------------------------------------------------------
	public void appClose(){
		System.exit(0);
	}
	public void appInit(){
		
	}

	/**
	 * Create the application.
	 */
	public Unarc80() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				appClose();
			}
		});
		frame.setBounds(100, 100, 770, 696);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		frame.getContentPane().setLayout(gridBagLayout);
		
		JPanel panelSourceFileName = new JPanel();
		GridBagConstraints gbc_panelSourceFileName = new GridBagConstraints();
		gbc_panelSourceFileName.insets = new Insets(0, 0, 5, 0);
		gbc_panelSourceFileName.fill = GridBagConstraints.VERTICAL;
		gbc_panelSourceFileName.gridx = 0;
		gbc_panelSourceFileName.gridy = 0;
		frame.getContentPane().add(panelSourceFileName, gbc_panelSourceFileName);
		GridBagLayout gbl_panelSourceFileName = new GridBagLayout();
		gbl_panelSourceFileName.columnWidths = new int[]{0, 0};
		gbl_panelSourceFileName.rowHeights = new int[]{0, 0};
		gbl_panelSourceFileName.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panelSourceFileName.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panelSourceFileName.setLayout(gbl_panelSourceFileName);
		
		lblSourceFileName = new JLabel("<No File Selected>");
		lblSourceFileName.setFont(new Font("Tahoma", Font.BOLD, 15));
		GridBagConstraints gbc_lblSourceFileName = new GridBagConstraints();
		gbc_lblSourceFileName.gridx = 0;
		gbc_lblSourceFileName.gridy = 0;
		panelSourceFileName.add(lblSourceFileName, gbc_lblSourceFileName);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 1;
		frame.getContentPane().add(tabbedPane, gbc_tabbedPane);
		
		JPanel tabTOC = new JPanel();
		tabbedPane.addTab("TOC", null, tabTOC, null);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnuFile = new JMenu("File");
		menuBar.add(mnuFile);
		
		mnuFileOpen = new JMenuItem("Open...");
		mnuFileOpen.setActionCommand(AC_MNU_FILE_OPEN);
		mnuFileOpen.addActionListener(this);
		mnuFile.add(mnuFileOpen);
		
		JSeparator separator = new JSeparator();
		mnuFile.add(separator);
		
		mnuFileClose = new JMenuItem("Close");
		mnuFileClose.setActionCommand(AC_MNU_FILE_CLOSE);
		mnuFileClose.addActionListener(this);
		mnuFile.add(mnuFileClose);
		
		JSeparator separator_1 = new JSeparator();
		mnuFile.add(separator_1);
		
		JMenuItem mnuFileExit = new JMenuItem("Exit");
		mnuFileExit.setActionCommand(AC_MNU_FILE_EXIT);
		mnuFileExit.addActionListener(this);
		mnuFile.add(mnuFileExit);
	}
	//--------------------------------------------------------------------------------
	
	private final static String AC_MNU_FILE_OPEN = "MenuFileOpen";
	private final static String AC_MNU_FILE_CLOSE = "MenuFileClose";
	private final static String AC_MNU_FILE_EXIT = "MenuFileExit";
	
	private final static String NO_FILE_SELECTED = "<No File Selected>";
	
	private final static int ARC_HEADER_SIZE = 0x1D;
	
	private final static byte[] HEADER_FLAG = new byte[]{0x1A,0x08};
	
	
	//------------------------------------------------------------
//	private String selectedAbsolutePath;
//	private FileChannel fcIn;
	private File sourceFile = null;
//	private String sourceFileAbsoluteName = null;
	private long sourceSize;
	private JLabel lblSourceFileName;
	private JMenuItem mnuFileOpen;
	private JMenuItem mnuFileClose;

	//------------------------------------------------------------

public class ArcHeader{
	byte[] rawData = new byte[ARC_HEADER_SIZE];

	private int baseAddress;
	private String disk;
	private String method;
	private String ver;
	private String date;
	private String time;
	private int crc;
	public boolean isValid;
	
	private ArcHeader(int baseAddress,byte[] rawData){
		if (rawData.length != (ARC_HEADER_SIZE)){
			this.isValid = false;
			return;
		}//
		this.isValid = true;
		this.baseAddress= baseAddress;
		this.rawData = rawData;
	}//constructor
	
	public String toString(){
		return getNameFixed();
	}//toString
	
	public int getBaseAddress(){
		return this.baseAddress;
	}//getBaseAddress
	
	public String getName(){
		int index = 2;
		StringBuilder sb = new StringBuilder();
		while (rawData[index] != 00){
			sb.append((char)rawData[index++]);
		}
		return sb.toString();
	}//getName
	
	public String getNameFixed(){
	String[] nameParts = getName().split("\\.");
	String left = nameParts[0];
	String right = (nameParts.length > 1)?nameParts[1]:"";
	return String.format("%-8s.%-3s",left,right);
	}//getNameFixed
	
	public int getLength(){
		return calculateValue( 0x19);
	}// getLength
	
	public int getStored(){
		return calculateValue( 0x0F);
	}//getStored
	
	private int calculateValue(int index){
		int ans = rawData[index] & 0xFF;
		ans |= (rawData[index +1] << 8)  & 0xFFFF;
		ans |= (rawData[index +2] << 16)  & 0xFFFFFF;
		ans |= (rawData[index +3] << 24)  & 0xFFFFFFFF;
		return ans;	
	}//calculateValue
	
	public int getSaved(){
		float l = (float) getLength();
		float s = (float) getStored();
		float ans = s/l;
		ans = (1.00f - ans) * 100;
		return (int)ans;		
	}//getSaved
	
	public String getDisk(){
		//TODO getDisk
		return "?K";
	}//getDisk
	
	public String getMethod(){
		String ans = "   Error";
		switch (rawData[1]){
		case (byte)0x00:
			ans = " InValid";
			break;
		case (byte)0x01: case (byte)0x02:
			ans = "Unpacked";
			break;
		case (byte)0x04:
			ans = "Squeezed";
			break;
		case (byte)0x03:
		case (byte)0x05: case (byte)0x06:
		case (byte)0x07: case (byte)0x08:
			ans = "Crunched";
			break;
		default:
			ans = " Unknown";
			break;	
		}//switch
		return ans;
	}//getMethod
	
	public String getVer(){
		return Integer.valueOf(rawData[1]).toString();
	}//getVer
	
	public String getDate(){
		//TODO getDate
		return "00 abc 00";
	}//getDate
	public String getTime(){
		//TODO getTime
		return "0:00p";
	}//getTime
	
	public int getCRC(){
		//TODO getCRC
		return 0;
	}//getCRC

	
}//class ArcHeader


}//class Unarc80
