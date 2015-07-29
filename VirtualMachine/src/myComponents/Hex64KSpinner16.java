package myComponents;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
//import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JFormattedTextField.AbstractFormatter;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;

public class Hex64KSpinner16 extends JSpinner implements PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	
	private static final int SIXTEEN = 16;
	JFormattedTextField ftf;

	public Hex64KSpinner16() {
		this(65536);
	}// Constructor - default

	public Hex64KSpinner16(int maxValue) {
		super(new SpinnerNumberModel(0, 0, maxValue - 1, SIXTEEN));
		JSpinner.DefaultEditor editor = (DefaultEditor) this.getEditor();
		ftf = editor.getTextField();
		ftf.setFormatterFactory(new MyFormatterFactory());
		ftf.addPropertyChangeListener("value", this);
	}// Constructor

	public int getMaxValue() {
		return (int) ((SpinnerNumberModel) this.getModel()).getMaximum();
	}// getMaxValue

	public void setMaxValue(int maxValue) {
		((SpinnerNumberModel) this.getModel()).setMaximum(maxValue);
	}// setMaxValue

	@Override
	public void propertyChange(PropertyChangeEvent pce) {
		int newValue = (int) pce.getNewValue();
		if ((newValue % SIXTEEN) != 0) {
			ftf.setValue(newValue - (newValue % SIXTEEN));
		}// if
	}// propertyChange

	// ----------------------------------------------------------------------------------------------------------

	private static class HexFormatter extends DefaultFormatter {
		private static final long serialVersionUID = 1L;

		public Object stringToValue(String text) throws ParseException {
			try {
				return Integer.valueOf(text, 16);
			} catch (NumberFormatException nfe) {
				throw new ParseException(text, 0);
			}// try
		}// stringToValue

		public String valueToString(Object value) throws ParseException {
			return String.format("%04X", (int) value);
		}// valueToString
	}// class HexFormatter

	private static class MyFormatterFactory extends DefaultFormatterFactory {
		private static final long serialVersionUID = 1L;

		public AbstractFormatter getDefaultFormatter() {
			return new HexFormatter();
		}// getDefaultFormatter
	}// class MyFormatterFactory

	// ----------------------------------------------------------------------------------------------------------
}// class Hex64KSpinner
