package disks;

import java.awt.Canvas;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.GridBagLayout;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import java.awt.GridBagConstraints;

import javax.swing.border.LineBorder;

import java.awt.Color;

import javax.swing.JScrollPane;

import java.awt.Insets;

import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.awt.Dimension;

public class TableTest {

	private JFrame frame;
	private JTable table;
	private DefaultTableModel model;

	// final static String[] columnNames = { "Name", "Ext", "User", "R/O", "Sys", "Seq", "Count", "Blocks" };
	// private Object[][] data;

	private JScrollPane scrollPane;
	private JButton btnPrint;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TableTest window = new TableTest();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private Object[] makeRow(int rowNumber,String name, String ext, int user, boolean readOnly,
			boolean systemFile, int seqNumber, int count, int blocks) {
		Object[] aRow = {rowNumber, name, ext, user, readOnly, systemFile, seqNumber, count, blocks };
		return aRow;
	}

	private final static int NUMBER_OF_ROWS = 40;

	private void fillTable() {
		Object[] columnNames = { "row", "Name", "type", "User", "R/O", "Sys", "Seq", "Count", "Blocks" };
		table = new JTable(new DefaultTableModel(columnNames, 0));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scrollPane.setViewportView(table);
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		int rowNumber = 0;
		JCheckBox box = new JCheckBox();
		box.setSelected(true);
//		model.insertRow(rowNumber++,new Object[]{"String",-1, new Integer(0),new Boolean(false),new Boolean(true)});
		model.insertRow(rowNumber++, makeRow(rowNumber,"W", "COM", 1, false, false, 0, -1, 1));
		model.insertRow(rowNumber++, makeRow(rowNumber,"WW", "COM", 2, false, false, 0, -1, 2));
		model.insertRow(rowNumber++, makeRow(rowNumber,"WWW", "ASM", 3, false, false, 0, -1, 3));
		model.insertRow(rowNumber++, makeRow(rowNumber,"WWWW", "COM", 4, true, false, 0, -1, 4));
		model.insertRow(rowNumber++, makeRow(rowNumber,"WWWWW", "SYS", 5, false, true, 0, -1, 5));
		model.insertRow(rowNumber++, makeRow(rowNumber,"WWWWWW", "ONE", 6, false, false, 1, 1, 1));
		model.insertRow(rowNumber++, makeRow(rowNumber,"WWWWWWW", "COM", 7, false, false, 1, -1, 16));
		model.insertRow(rowNumber++, makeRow(rowNumber,"WWWWWWWW", "", 8, false, false, 0, 0, 0));
		model.insertRow(rowNumber++, makeRow(rowNumber,"WWWWWWWWW", "COM", 9, false, false, 0, -1, 16));
		model.insertRow(rowNumber++, makeRow(rowNumber,"WWWWWWWWWW", "COM", 10, false, false, 0, -1, 16));

		model.insertRow(rowNumber++, makeRow(rowNumber,"WWWWWWWWWWW", "COM", 11, false, false, 0, -1, 1));
		model.insertRow(rowNumber++, makeRow(rowNumber,"MNO", "COM", 0, false, false, 0, -1, 2));
		model.insertRow(rowNumber++, makeRow(rowNumber,"PQR", "ASM", 0, false, false, 0, -1, 3));
		model.insertRow(rowNumber++, makeRow(rowNumber,"STU", "COM", 0, true, false, 0, -1, 4));
		model.insertRow(rowNumber++, makeRow(rowNumber,"VWX", "SYS", 0, false, true, 0, -1, 5));
		model.insertRow(rowNumber++, makeRow(rowNumber,"A1", "ONE", 0, false, false, 1, 1, 1));
		model.insertRow(rowNumber++, makeRow(rowNumber,"B2", "COM", 0, false, false, 1, -1, 16));
		model.insertRow(rowNumber++, makeRow(rowNumber,"<Empty>", "", 0, false, false, 0, 0, 0));
		model.insertRow(rowNumber++, makeRow(rowNumber,"C3", "COM", 0, false, false, 0, -1, 16));
		model.insertRow(rowNumber++, makeRow(rowNumber,"D4", "COM", 0, false, false, 0, -1, 16));

		model.insertRow(rowNumber++, makeRow(rowNumber,"ABC", "COM", 0, false, false, 0, -1, 1));
		model.insertRow(rowNumber++, makeRow(rowNumber,"XYZ", "COM", 0, false, false, 0, -1, 2));
		model.insertRow(rowNumber++, makeRow(rowNumber,"SOURCE", "ASM", 0, false, false, 0, -1, 3));
		model.insertRow(rowNumber++, makeRow(rowNumber,"READONLY", "COM", 0, true, false, 0, -1, 4));
		model.insertRow(rowNumber++, makeRow(rowNumber,"AFILE", "SYS", 0, false, true, 0, -1, 5));
		model.insertRow(rowNumber++, makeRow(rowNumber,"NEXT", "ONE", 0, false, false, 1, 1, 1));
		model.insertRow(rowNumber++, makeRow(rowNumber,"ABC", "COM", 0, false, false, 1, -1, 16));
		model.insertRow(rowNumber++, makeRow(rowNumber,"<Empty>", "", 0, false, false, 0, 0, 0));
		model.insertRow(rowNumber++, makeRow(rowNumber,"DEF", "COM", 0, false, false, 0, -1, 16));
		model.insertRow(rowNumber++, makeRow(rowNumber,"GHI", "COM", 0, false, false, 0, -1, 16));

		model.insertRow(rowNumber++, makeRow(rowNumber,"JKL", "COM", 0, false, false, 0, -1, 1));
		model.insertRow(rowNumber++, makeRow(rowNumber,"MNO", "COM", 0, false, false, 0, -1, 2));
		model.insertRow(rowNumber++, makeRow(rowNumber,"PQR", "ASM", 0, false, false, 0, -1, 3));
		model.insertRow(rowNumber++, makeRow(rowNumber,"STU", "COM", 0, true, false, 0, -1, 4));
		model.insertRow(rowNumber++, makeRow(rowNumber,"VWX", "SYS", 0, false, true, 0, -1, 5));
		model.insertRow(rowNumber++, makeRow(rowNumber,"A1", "ONE", 0, false, false, 1, 1, 1));
		model.insertRow(rowNumber++, makeRow(rowNumber,"B2", "COM", 0, false, false, 1, -1, 16));
		model.insertRow(rowNumber++, makeRow(rowNumber,"<Empty>", "", 0, false, false, 0, 0, 0));
		model.insertRow(rowNumber++, makeRow(rowNumber,"C3", "COM", 0, false, false, 0, -1, 16));
		model.insertRow(rowNumber++, makeRow(rowNumber,"D4", "COM", 0, false, false, 0, -1, 16));
		
		adjustTableLook();
	}

	private void adjustTableLook() {
		
		Font realColumnFont = table.getFont();
		FontMetrics fontMetrics= table.getFontMetrics(realColumnFont);
		
		int charWidth = fontMetrics.stringWidth("W");
		
		TableColumnModel tableColumn = table.getColumnModel();
		tableColumn.getColumn(0).setPreferredWidth(charWidth * 3);
		tableColumn.getColumn(1).setPreferredWidth(charWidth * 12);
		tableColumn.getColumn(2).setPreferredWidth(charWidth * 4);
		tableColumn.getColumn(3).setPreferredWidth(charWidth * 3);
		tableColumn.getColumn(4).setPreferredWidth(charWidth * 4);
		tableColumn.getColumn(5).setPreferredWidth(charWidth * 4);
		tableColumn.getColumn(6).setPreferredWidth(charWidth * 3);
		tableColumn.getColumn(7).setPreferredWidth(charWidth * 3);
		tableColumn.getColumn(8).setPreferredWidth(charWidth * 3);
		
		DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
		rightAlign.setHorizontalAlignment(JLabel.RIGHT);
		tableColumn.getColumn(0).setCellRenderer(rightAlign);
		tableColumn.getColumn(3).setCellRenderer(rightAlign);
		tableColumn.getColumn(6).setCellRenderer(rightAlign);
		tableColumn.getColumn(7).setCellRenderer(rightAlign);
		tableColumn.getColumn(8).setCellRenderer(rightAlign);
		
		DefaultTableCellRenderer centerAlign = new DefaultTableCellRenderer();
		centerAlign.setHorizontalAlignment(JLabel.CENTER);
		tableColumn.getColumn(4).setCellRenderer(centerAlign);
		tableColumn.getColumn(5).setCellRenderer(centerAlign);
		
			
	}

	// --------------------------------------------------------------------------
	private void appInit() {
		fillTable();
		adjustTableLook();
	}

	/**
	 * Create the application.
	 */
	public TableTest() {
		initialize();
		appInit();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 679, 755);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 585, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		frame.getContentPane().setLayout(gridBagLayout);

		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.VERTICAL;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 0;
		frame.getContentPane().add(scrollPane, gbc_scrollPane);

		btnPrint = new JButton("Print");
		btnPrint.setMinimumSize(new Dimension(20, 23));
		btnPrint.setPreferredSize(new Dimension(20, 23));
		btnPrint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					table.print();
				} catch (PrinterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		GridBagConstraints gbc_btnPrint = new GridBagConstraints();
		gbc_btnPrint.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnPrint.gridx = 1;
		gbc_btnPrint.gridy = 2;
		frame.getContentPane().add(btnPrint, gbc_btnPrint);

		// table = new JTable();
		// scrollPane.setViewportView(table);
	}

}
