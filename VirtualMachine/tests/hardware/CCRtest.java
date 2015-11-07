package hardware;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CCRtest {

	ConditionCodeRegister ccr;
	Byte value;
	Byte ans;
	static Byte zeros = (byte) 0x00;
	static Byte ones = (byte) 0xFF;

	// XX0X 0X1X

	// @BeforeClass
	// public static void setUpBeforeClass() throws Exception {
	// }
	//
	// @AfterClass
	// public static void tearDownAfterClass() throws Exception {
	// }

	@Before
	public void setUp() throws Exception {
		ccr = new ConditionCodeRegister();
	}

	@After
	public void tearDown() throws Exception {
		ccr = null;
	}

	@Test
	public void testSetGetConditionCode() {
		// XX0X 0X1X

		ans = (byte) 0x02;
		ccr.setConditionCode(zeros);
		assertThat("00", ans, equalTo(ccr.getConditionCode()));

		ans = (byte) 0xD7;
		ccr.setConditionCode(ones);
		assertThat("FF", ans, equalTo(ccr.getConditionCode()));
	}

	@Test
	public void testSignFlag() {
		ccr.setConditionCode(zeros);
		
		ccr.setSignFlag(true);
		assertThat("00", true, equalTo(ccr.isSignFlagSet()));
		
		ccr.setSignFlag(false);
		assertThat("00", false, equalTo(ccr.isSignFlagSet()));
		
		ccr.setConditionCode(ones);
		
		ccr.setSignFlag(true);
		assertThat("00", true, equalTo(ccr.isSignFlagSet()));
		
		ccr.setSignFlag(false);
		assertThat("00", false, equalTo(ccr.isSignFlagSet()));
		
	}

	@Test
	public void testZeroFlag() {
		ccr.setConditionCode(zeros);
		
		ccr.setZeroFlag(true);
		assertThat("00", true, equalTo(ccr.isZeroFlagSet()));
		
		ccr.setZeroFlag(false);
		assertThat("00", false, equalTo(ccr.isZeroFlagSet()));
		
		ccr.setConditionCode(ones);
		
		ccr.setZeroFlag(true);
		assertThat("00", true, equalTo(ccr.isZeroFlagSet()));
		
		ccr.setZeroFlag(false);
		assertThat("00", false, equalTo(ccr.isZeroFlagSet()));
		
	}

	@Test
	public void testAuxilaryCarryFlag() {
		ccr.setConditionCode(zeros);
		
		ccr.setAuxilaryCarryFlag(true);
		assertThat("00", true, equalTo(ccr.isAuxilaryCarryFlagSet()));
		
		ccr.setAuxilaryCarryFlag(false);
		assertThat("00", false, equalTo(ccr.isAuxilaryCarryFlagSet()));
		
		ccr.setConditionCode(ones);
		
		ccr.setAuxilaryCarryFlag(true);
		assertThat("00", true, equalTo(ccr.isAuxilaryCarryFlagSet()));
		
		ccr.setAuxilaryCarryFlag(false);
		assertThat("00", false, equalTo(ccr.isAuxilaryCarryFlagSet()));
		
	}

	@Test
	public void testParityFlag() {
		ccr.setConditionCode(zeros);
		
		ccr.setParityFlag(true);
		assertThat("00", true, equalTo(ccr.isParityFlagSet()));
		
		ccr.setParityFlag(false);
		assertThat("00", false, equalTo(ccr.isParityFlagSet()));
		
		ccr.setConditionCode(ones);
		
		ccr.setParityFlag(true);
		assertThat("00", true, equalTo(ccr.isParityFlagSet()));
		
		ccr.setParityFlag(false);
		assertThat("00", false, equalTo(ccr.isParityFlagSet()));
		
	}

	@Test
	public void testCarryFlag() {
		ccr.setConditionCode(zeros);
		
		ccr.setCarryFlag(true);
		assertThat("00", true, equalTo(ccr.isCarryFlagSet()));
		
		ccr.setCarryFlag(false);
		assertThat("00", false, equalTo(ccr.isCarryFlagSet()));
		
		ccr.setConditionCode(ones);
		
		ccr.setCarryFlag(true);
		assertThat("00", true, equalTo(ccr.isCarryFlagSet()));
		
		ccr.setCarryFlag(false);
		assertThat("00", false, equalTo(ccr.isCarryFlagSet()));
		
	}

	 @Test
	 public void testZSP() {
		 ccr.setConditionCode(zeros);
		 ccr.setZSP((byte) 0x80);		// set S, reset Z&P
		 assertThat("sign - zeros with 0x80",true,equalTo(ccr.isSignFlagSet()));
		 assertThat("zero - zeros with 0x80",false,equalTo(ccr.isZeroFlagSet()));
		 assertThat("parity - zeros with 0x80",false,equalTo(ccr.isParityFlagSet()));
		 
		 ccr.setConditionCode(ones);
		 ccr.setZSP((byte) 0x80);		// set S, reset Z&P
		 assertThat("sign - zeros with 0x80",true,equalTo(ccr.isSignFlagSet()));
		 assertThat("zero - zeros with 0x80",false,equalTo(ccr.isZeroFlagSet()));
		 assertThat("parity - zeros with 0x80",false,equalTo(ccr.isParityFlagSet()));
		 
		 ccr.setConditionCode(zeros);
		 ccr.setZSP((byte) 0x40);		// set ., reset S,Z&P
		 assertThat("sign - zeros with 0x40",false,equalTo(ccr.isSignFlagSet()));
		 assertThat("zero - zeros with 0x40",false,equalTo(ccr.isZeroFlagSet()));
		 assertThat("parity - zeros with 0x40",false,equalTo(ccr.isParityFlagSet()));
		 
		 ccr.setConditionCode(ones);
		 ccr.setZSP((byte) 0x40);		// set ., reset S,Z&P
		 assertThat("sign - zeros with 0x40",false,equalTo(ccr.isSignFlagSet()));
		 assertThat("zero - zeros with 0x40",false,equalTo(ccr.isZeroFlagSet()));
		 assertThat("parity - zeros with 0x40",false,equalTo(ccr.isParityFlagSet()));
		 
		 ccr.setConditionCode(zeros);
		 ccr.setZSP((byte) 0x00);		// set Z & P, reset S
		 assertThat("sign - zeros with 0x00",false,equalTo(ccr.isSignFlagSet()));
		 assertThat("zero - zeros with 0x00",true,equalTo(ccr.isZeroFlagSet()));
		 assertThat("parity - zeros with 0x00",true,equalTo(ccr.isParityFlagSet()));
		 
		 ccr.setConditionCode(ones);
		 ccr.setZSP((byte) 0x00);		// set Z & P, reset S
		 assertThat("sign - zeros with 0x00",false,equalTo(ccr.isSignFlagSet()));
		 assertThat("zero - zeros with 0x00",true,equalTo(ccr.isZeroFlagSet()));
		 assertThat("parity - zeros with 0x00",true,equalTo(ccr.isParityFlagSet()));
		 
		 
		 ccr.setConditionCode(zeros);
		 ccr.setZSP((byte) 0x81);		// set Z & P, reset S
		 assertThat("sign - zeros with 0x00",true,equalTo(ccr.isSignFlagSet()));
		 assertThat("zero - zeros with 0x00",false,equalTo(ccr.isZeroFlagSet()));
		 assertThat("parity - zeros with 0x00",true,equalTo(ccr.isParityFlagSet()));
		 
		 ccr.setConditionCode(ones);
		 ccr.setZSP((byte) 0x81);		// set Z & P, reset S
		 assertThat("sign - zeros with 0x00",true,equalTo(ccr.isSignFlagSet()));
		 assertThat("zero - zeros with 0x00",false,equalTo(ccr.isZeroFlagSet()));
		 assertThat("parity - zeros with 0x00",true,equalTo(ccr.isParityFlagSet()));
		 
		 
		 ccr.setConditionCode(zeros);
		 ccr.setZSP((byte) 0xFF);		// set Z & P, reset S
		 assertThat("sign - zeros with 0x00",true,equalTo(ccr.isSignFlagSet()));
		 assertThat("zero - zeros with 0x00",false,equalTo(ccr.isZeroFlagSet()));
		 assertThat("parity - zeros with 0x00",true,equalTo(ccr.isParityFlagSet()));
		 
		 ccr.setConditionCode(ones);
		 ccr.setZSP((byte) 0xFF);		// set Z & P, reset S
		 assertThat("sign - zeros with 0x00",true,equalTo(ccr.isSignFlagSet()));
		 assertThat("zero - zeros with 0x00",false,equalTo(ccr.isZeroFlagSet()));
		 assertThat("parity - zeros with 0x00",true,equalTo(ccr.isParityFlagSet()));
		 
	 }
	

}
