package myComponents;

import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;


public class Hex64KSpinner extends JSpinner{
	private static final long serialVersionUID = 1L;

	public Hex64KSpinner() {
		this(65536,1);
	}//Constructor - default
	
	public Hex64KSpinner(int maxValue, int step) {
		super(new SpinnerNumberModel(0,0,maxValue-1,step));
		JSpinner.DefaultEditor editor = (DefaultEditor) this.getEditor();
		JFormattedTextField ftf = editor.getTextField();
		ftf.setFormatterFactory(new MyFormatterFactory());	
	}//Constructor
	
	public int getMaxValue(){
		return (int) ((SpinnerNumberModel) this.getModel()).getMaximum();
	}//getMaxValue
	
	public void setMaxValue(int maxValue){
		((SpinnerNumberModel) this.getModel()).setMaximum(maxValue);
	}//setMaxValue
		
	// ----------------------------------------------------------------------------------------------------------
	private static class HexFormatter extends DefaultFormatter {
		private static final long serialVersionUID = 1L;

		public Object stringToValue(String text) throws ParseException {
			try {
				return Integer.valueOf(text, 16);
			} catch (NumberFormatException nfe) {
				throw new ParseException(text, 0);
			}//try
		}//stringToValue

		public String valueToString(Object value) throws ParseException {
			return String.format("%04X", value);
		}//valueToString
	}//class HexFormatter

	private static class MyFormatterFactory extends DefaultFormatterFactory {
		private static final long serialVersionUID = 1L;

		public AbstractFormatter getDefaultFormatter() {
			return new HexFormatter();
		}//getDefaultFormatter
	}//class MyFormatterFactory
	// ----------------------------------------------------------------------------------------------------------
}//class Hex64KSpinner
