package hardware;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class AUtest {
	ConditionCodeRegister ccr;
	ArithmeticUnit au;

	Byte operand1Byte;
	Byte operand2Byte;
	Byte answerByte;
	Byte ccrByte;
	Integer operand1Integer;
	Integer operand2Integer;
	Integer answerInteger;

	static Byte zerosByte = (byte) 0x00;
	static Byte onesByte = (byte) 0xFF;
	static Integer zerosInteger = 0x0000 & 0xFFFF;
	static Integer onesInteger = 0xFFFF & 0xFFFF;

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		ccr = new ConditionCodeRegister();
		au = new ArithmeticUnit(ccr);
	}

	@Test
	public void testAddByteByte() {
		operand1Byte = zerosByte;
		operand2Byte = zerosByte;
		ccrByte = (byte) 0x46;
		answerByte = zerosByte;
		ccr.setConditionCode(zerosByte);
		assertThat("00 + 00 ccr =00", answerByte, equalTo(au.add(operand1Byte, operand2Byte)));
		assertThat("00 + 00 ccr = 00", ccrByte, equalTo(ccr.getConditionCode()));
		ccr.setConditionCode(onesByte);
		assertThat("00 + 00 ccr =-1", answerByte, equalTo(au.add(operand1Byte, operand2Byte)));
		assertThat("00 + 00 ccr = -1", ccrByte, equalTo(ccr.getConditionCode()));

		operand1Byte = onesByte;
		operand2Byte = onesByte;
		ccrByte = (byte) 0x93;
		answerByte = (byte) 0xFE;
		ccr.setConditionCode(zerosByte);
		assertThat("-1 + -1 ccr =00", answerByte, equalTo(au.add(operand1Byte, operand2Byte)));
		assertThat("-1 + -1 ccr = 00", ccrByte, equalTo(ccr.getConditionCode()));
		ccr.setConditionCode(onesByte);
		assertThat("-1 + -1 ccr =-1", answerByte, equalTo(au.add(operand1Byte, operand2Byte)));
		assertThat("-1 + -1 ccr = -1", ccrByte, equalTo(ccr.getConditionCode()));

		operand1Byte = (byte) 0xAA;
		operand2Byte = (byte) 0x55;
		ccrByte = (byte) 0x86;
		answerByte = (byte) 0xFF;
		ccr.setConditionCode(zerosByte);
		assertThat("AA + 55 ccr =00", answerByte, equalTo(au.add(operand1Byte, operand2Byte)));
		assertThat("AA + %% ccr = 00", ccrByte, equalTo(ccr.getConditionCode()));
		ccr.setConditionCode(onesByte);
		assertThat("AA + 55 ccr =-1", answerByte, equalTo(au.add(operand1Byte, operand2Byte)));
		assertThat("AA + %% ccr = -1", ccrByte, equalTo(ccr.getConditionCode()));

		// fail("Not yet implemented");
	}

	// @Test
	// public void testAddIntInt() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testAddWithCarry() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testSubtractByteByte() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testSubtractShortShort() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testSubtractWithBorrow() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testIncrementByte() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testIncrementShort() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testDecrementByte() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testDecrementShort() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testRotateLeft() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testRotateRight() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testComplement() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testLogicalAnd() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testLogicalXor() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testLogicalOr() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testDecimalAdjustByte() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testSetCarry() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testComplementCarry() {
	// fail("Not yet implemented");
	// }

}
