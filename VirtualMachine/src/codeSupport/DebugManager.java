package codeSupport;

import java.awt.EventQueue;

import javax.swing.JFrame;

import java.awt.GridBagLayout;

import javax.swing.JButton;

import java.awt.GridBagConstraints;

import memory.Core;
import memory.Core.TRAP;
import myComponents.Hex64KSpinner;

import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DebugManager extends JFrame {

	private static final String DISABLE = "Disable";
	private static final String ENABLE = "Enable";
	private JTextArea txtLocations;
	private JButton btnRemove;
	private JButton btnAdd;
	private Hex64KSpinner newLocation;
	private JButton btnDebugEnable;
	private Core core;
	private ArrayList<Integer> locations;
	private Document doc;
	private JButton btnReset;
	private int selectedLocation;
	
	// relies on forcing an opcode of 0X30 to simulate a halt, and leaves the program counter in place

	private void updateTheList() {
		locations = core.getTrapLocations();
		Collections.sort(locations);
		doc = txtLocations.getDocument();
		try {
			doc.remove(0, doc.getLength());
			for (Integer location : locations) {
				doc.insertString(doc.getLength(), String.format("%04X%n", location), null);
			}
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//try
	}//updateTheList
	
	private void updateScreen(){
		btnDebugEnable.setText(core.isDebugTrapEnabled() ? DISABLE : ENABLE);
		btnRemove.setEnabled(false);
		updateTheList();
		txtLocations.setCaretPosition(0);
	}//updateScreen
	
	private void removeAllDebugLocations(){
		ArrayList<Integer> allDebugLocations = core.getTrapLocations();
		for(Integer location:allDebugLocations){
			core.removeTrapLocation(location, TRAP.DEBUG);
		}//for
		updateScreen();
	}//removeAllDebugLocations
	
	// ------------------------------------------------------------------------------------------------

	private void appInit() {
		updateScreen();
	}//appInit


	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		setBounds(100, 100, 383, 262);
		this.setTitle("Debug Manager");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		appInit();
	}

	/**
	 * Create the application.
	 */
	public DebugManager(Core core) {
		this.core = core;
		this.setLocation(1000, 200);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 10, 0, 5, 100, 5, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 20, 0, 0, 23, 23, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);
		
				btnDebugEnable = new JButton(ENABLE);
				btnDebugEnable.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						if(btnDebugEnable.getText().equals(ENABLE)){
							core.setDebugTrapEnabled(true);
							btnDebugEnable.setText(DISABLE);
						}else{
							core.setDebugTrapEnabled(false);
							btnDebugEnable.setText(ENABLE);
						}//if
						updateScreen();
					}
				});
				btnDebugEnable.setActionCommand("btnDebugEnable");
				GridBagConstraints gbc_btnDebugEnable = new GridBagConstraints();
				gbc_btnDebugEnable.fill = GridBagConstraints.BOTH;
				gbc_btnDebugEnable.insets = new Insets(0, 0, 5, 5);
				gbc_btnDebugEnable.gridx = 3;
				gbc_btnDebugEnable.gridy = 1;
				getContentPane().add(btnDebugEnable, gbc_btnDebugEnable);
				
						newLocation = new Hex64KSpinner();
						GridBagConstraints gbc_newLocation = new GridBagConstraints();
						gbc_newLocation.insets = new Insets(0, 0, 5, 5);
						gbc_newLocation.gridx = 1;
						gbc_newLocation.gridy = 2;
						getContentPane().add(newLocation, gbc_newLocation);
		
				JScrollPane scrollPane = new JScrollPane();
				scrollPane.setMinimumSize(new Dimension(23, 100));
				scrollPane.setPreferredSize(new Dimension(0, 0));
				GridBagConstraints gbc_scrollPane = new GridBagConstraints();
				gbc_scrollPane.gridheight = 4;
				gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
				gbc_scrollPane.fill = GridBagConstraints.BOTH;
				gbc_scrollPane.gridx = 3;
				gbc_scrollPane.gridy = 2;
				getContentPane().add(scrollPane, gbc_scrollPane);
				
						txtLocations = new JTextArea();
						txtLocations.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent me) {
								if (me.getClickCount() >=2){
									String selectedText = txtLocations.getSelectedText();
									//System.out.printf("Selected %s%n",selectedText);
									btnRemove.setEnabled(true);
									selectedLocation = Integer.valueOf(selectedText, 16);
							} else{
									btnRemove.setEnabled(false);
								}//if
							}
						});
						scrollPane.setViewportView(txtLocations);
		
				btnAdd = new JButton("Add");
				btnAdd.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						core.addTrapLocation((int) newLocation.getValue(), TRAP.DEBUG);
						updateTheList();
					}
				});
				
						btnRemove = new JButton("Remove");
						btnRemove.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
									core.removeTrapLocation(selectedLocation, TRAP.DEBUG);
									updateScreen();
	
							}
						});
						btnRemove.setActionCommand("btnRemove");
						GridBagConstraints gbc_btnRemove = new GridBagConstraints();
						gbc_btnRemove.insets = new Insets(0, 0, 5, 0);
						gbc_btnRemove.fill = GridBagConstraints.HORIZONTAL;
						gbc_btnRemove.gridx = 5;
						gbc_btnRemove.gridy = 2;
						getContentPane().add(btnRemove, gbc_btnRemove);
				btnAdd.setActionCommand("btnAdd");
				GridBagConstraints gbc_btnAdd = new GridBagConstraints();
				gbc_btnAdd.fill = GridBagConstraints.HORIZONTAL;
				gbc_btnAdd.insets = new Insets(0, 0, 5, 5);
				gbc_btnAdd.gridx = 1;
				gbc_btnAdd.gridy = 3;
				getContentPane().add(btnAdd, gbc_btnAdd);
				
				JButton btnRemoveAll = new JButton("Remove All");
				btnRemoveAll.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						removeAllDebugLocations();
					}
				});
				btnRemoveAll.setActionCommand("btnRemoveAll");
				GridBagConstraints gbc_btnRemoveAll = new GridBagConstraints();
				gbc_btnRemoveAll.anchor = GridBagConstraints.NORTH;
				gbc_btnRemoveAll.insets = new Insets(0, 0, 5, 0);
				gbc_btnRemoveAll.gridx = 5;
				gbc_btnRemoveAll.gridy = 3;
				getContentPane().add(btnRemoveAll, gbc_btnRemoveAll);
				
				btnReset = new JButton("Reset");
				btnReset.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						core.setDebugTrapEnabled(false);
						removeAllDebugLocations();
					}
				});
				btnReset.setActionCommand("btnReset");
				GridBagConstraints gbc_btnReset = new GridBagConstraints();
				gbc_btnReset.fill = GridBagConstraints.HORIZONTAL;
				gbc_btnReset.insets = new Insets(0, 0, 5, 5);
				gbc_btnReset.gridx = 3;
				gbc_btnReset.gridy = 6;
				getContentPane().add(btnReset, gbc_btnReset);

		initialize();

	}

}