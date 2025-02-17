package devera.score.example;

import devera.score.token.irc2.IRC2BurnableToken;
import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static java.math.BigInteger.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class CrowdsaleTest extends TestBase {
   
    // sample-crowdsale
    private static final BigInteger tuition = BigInteger.valueOf(20);
    private static final BigInteger numberOfLesson = BigInteger.valueOf(10);
    private static final BigInteger durationInDefault = BigInteger.valueOf(3600);

    private static final ServiceManager sm = getServiceManager();
    private static final Account teacher = sm.createAccount();
    private Score crowdsaleScore;

    private Crowdsale crowdsaleSpy;
    private final byte[] startCrowdsaleBytes = "start crowdsale".getBytes();

    @BeforeEach
    public void setup() throws Exception {
        // deploy  crowdsale scores
        
        crowdsaleScore = sm.deploy(teacher, Crowdsale.class,
                tuition, numberOfLesson , durationInDefault);

        // setup spy object against the crowdsale object
        crowdsaleSpy = (Crowdsale) spy(crowdsaleScore.getInstance());
        crowdsaleScore.setInstance(crowdsaleSpy);
    }
    private Account initStudent() {
       // fund 40 icx from Alice
        Account alice = sm.createAccount(100);
        BigInteger fund = ICX.multiply(BigInteger.valueOf(40));
        sm.transfer(alice, crowdsaleScore.getAddress(), fund);
        return alice;
    }
    // register test
    @Test
    void fallback() {
        
        // fund 40 icx from Alice
        Account alice = sm.createAccount(100);
        BigInteger fund = ICX.multiply(BigInteger.valueOf(40));
        sm.transfer(alice, crowdsaleScore.getAddress(), fund);
        // verify
        verify(crowdsaleSpy).fallback();
        verify(crowdsaleSpy).Registration(alice.getAddress(), fund);
        assertEquals(fund, Account.getAccount(crowdsaleScore.getAddress()).getBalance());
        assertTrue(fund.equals(crowdsaleScore.call("balanceOf", alice.getAddress())));
    }
    @Test
    void fallback_notEnoughTuition() {
        Account alice = sm.createAccount(100);
        BigInteger fund = ICX.multiply(BigInteger.valueOf(10));
        
        assertThrows(AssertionError.class, () ->
                sm.transfer(alice, crowdsaleScore.getAddress(), fund));
    }
    @Test
    void fallback_courseWasClosed() {
        Account alice = sm.createAccount(100);
        BigInteger fund = ICX.multiply(BigInteger.valueOf(40));
        // increase the number of classes has passed
        for (int i = 0; i < this.numberOfLesson.intValue(); i++){
            crowdsaleScore.invoke( this.teacher,"openRollCall");
            crowdsaleScore.invoke( this.teacher,"closeRollCall");
          
        }
        // crowdsale has finished
        assertThrows(AssertionError.class, () -> 
            sm.transfer(alice, crowdsaleScore.getAddress(), fund));
    }
     @Test
    void fallback_withExistedStudent() {
        
        // fund 40 icx from Alice
        Account alice = sm.createAccount(100);
        BigInteger fund = ICX.multiply(BigInteger.valueOf(40));
        sm.transfer(alice, crowdsaleScore.getAddress(), fund);
        // fund more 20 icx from Alice
        fund = ICX.multiply(BigInteger.valueOf(20));
        sm.transfer(alice, crowdsaleScore.getAddress(), fund);
        BigInteger expectedValue = ICX.multiply(BigInteger.valueOf(60));
        // verify
        verify(crowdsaleSpy).Registration(alice.getAddress(), fund);
        assertEquals(expectedValue, Account.getAccount(crowdsaleScore.getAddress()).getBalance());
        assertTrue(expectedValue.equals(crowdsaleScore.call("balanceOf", alice.getAddress())));
    }
    //student roll call test
    @Test
    void rollCall() {
        Account alice = this.initStudent();
        crowdsaleScore.invoke( this.teacher,"openRollCall");
        crowdsaleScore.invoke( alice,"rollCall");
        BigInteger  _value = BigInteger.valueOf(1);
        // verify
        verify(crowdsaleSpy).rollCall();
        verify(crowdsaleSpy).RollCall(alice.getAddress(), _value);
        assertTrue(_value.equals(crowdsaleScore.call("checkNumberOfStudentAttended", alice.getAddress())));
    }
    @Test
    void rollCall_withNonExistedStudent() {
        Account alice = sm.createAccount(100);
        crowdsaleScore.invoke( this.teacher,"openRollCall");
        crowdsaleScore.invoke( alice,"rollCall");
        BigInteger  _value = BigInteger.valueOf(0);
        // verify
        verify(crowdsaleSpy).rollCall();
        verify(crowdsaleSpy).FailRollCall(alice.getAddress());
        assertTrue(_value.equals(crowdsaleScore.call("checkNumberOfStudentAttended", alice.getAddress())));
    }
    @Test
    void rollCall_whenInactiveClass() {
        Account alice = initStudent();
        crowdsaleScore.invoke( alice,"rollCall");
        BigInteger  _value = BigInteger.valueOf(0);
        // verify
        verify(crowdsaleSpy).rollCall();
        verify(crowdsaleSpy).FailRollCall(alice.getAddress());
        assertTrue(_value.equals(crowdsaleScore.call("checkNumberOfStudentAttended", alice.getAddress())));
    }
    @Test
    void rollCall_hadRolled() {
        Account alice = initStudent();
        crowdsaleScore.invoke( this.teacher,"openRollCall");
        crowdsaleScore.invoke( alice,"rollCall");
        crowdsaleScore.invoke( alice,"rollCall");
        BigInteger  _value = BigInteger.valueOf(1);
        // verify
        verify(crowdsaleSpy).FailRollCall(alice.getAddress());
        assertTrue(_value.equals(crowdsaleScore.call("checkNumberOfStudentAttended", alice.getAddress())));
    }
    // teacher contact to contract test
    @Test
    void openRollCall() {
       
        crowdsaleScore.invoke( this.teacher,"openRollCall");
        BigInteger  _value = BigInteger.valueOf(1);
        // verify
        verify(crowdsaleSpy).openRollCall();
        verify(crowdsaleSpy).ActiveCourse(this.teacher.getAddress(), _value);
        assertTrue(_value.equals(crowdsaleScore.call("CurrentLesson")));
        assertEquals("true",crowdsaleScore.call("isOpenRollCall"));
    }
    @Test
    void openRollCall_nonUsingTeacherAccount() {
        Account alice = sm.createAccount(100);
        
        // verify
         assertThrows(AssertionError.class, () -> 
            crowdsaleScore.invoke( alice,"openRollCall"));
    }
    @Test
    void openRollCall_withOpenedClass() {
        crowdsaleScore.invoke( this.teacher,"openRollCall");
        // verify
         assertThrows(AssertionError.class, () -> 
            crowdsaleScore.invoke( this.teacher,"openRollCall"));
    }
    @Test
    void openRollCall_afterEndCourse() {
        // increase the number of classes has passed
        for (int i = 1; i <= this.numberOfLesson.intValue(); i++){
            crowdsaleScore.invoke( this.teacher,"openRollCall");
            crowdsaleScore.invoke( this.teacher,"closeRollCall");
            
        }
        BigInteger  _value = BigInteger.valueOf(10);
        crowdsaleScore.invoke( this.teacher,"openRollCall");
        // verify
        verify(crowdsaleSpy).FailToOpenRollCall();
        assertTrue(_value.equals(crowdsaleScore.call("CurrentLesson")));
        assertEquals("false",crowdsaleScore.call("isOpenRollCall"));
    }
    @Test
    void closeRollCall() {
       
        crowdsaleScore.invoke( this.teacher,"openRollCall");
        crowdsaleScore.invoke( this.teacher,"closeRollCall");
        BigInteger  _value = BigInteger.valueOf(1);
        // verify
        verify(crowdsaleSpy).closeRollCall();
        verify(crowdsaleSpy).InActiveCourse(this.teacher.getAddress(), _value);
        assertTrue(_value.equals(crowdsaleScore.call("CurrentLesson")));
        assertEquals("false",crowdsaleScore.call("isOpenRollCall"));
    }
    @Test
    void closeRollCall_nonUsingTeacherAccount() {
        Account alice = sm.createAccount(100);
        crowdsaleScore.invoke( this.teacher,"openRollCall");
        // verify
         assertThrows(AssertionError.class, () -> 
            crowdsaleScore.invoke( alice,"closeRollCall"));
    }
    @Test
    void closeRollCall_withClosedClass() {
        crowdsaleScore.invoke( this.teacher,"openRollCall");
        crowdsaleScore.invoke( this.teacher,"closeRollCall");
        // verify
         assertThrows(AssertionError.class, () -> 
            crowdsaleScore.invoke( this.teacher,"closeRollCall"));
    }
    //withdraw test
    @Test
    void teacherWithdraw_withNonAccordantStudent() {
        //create new student with 40icx in course, 60icx balance
        Account alice = this.initStudent();
        // increase the number of classes has passed
        for (int i = 1; i <= this.numberOfLesson.intValue(); i++){
            crowdsaleScore.invoke( this.teacher,"openRollCall");
            crowdsaleScore.invoke( this.teacher,"closeRollCall");
        }
        BigInteger  _value = this.teacher.getBalance().add(ICX.multiply(BigInteger.valueOf(40)));
        crowdsaleScore.invoke( this.teacher,"withdraw");
        // verify
        verify(crowdsaleSpy).withdraw();
        assertTrue(_value.equals(this.teacher.getBalance()));
        assertEquals(BigInteger.ZERO, Account.getAccount(crowdsaleScore.getAddress()).getBalance());
        assertEquals(BigInteger.ZERO, crowdsaleScore.call("balanceOf", alice.getAddress()));
    }
    @Test
    void teacherWithdraw_withAccordantStudent() {
        //create new student with 40icx in course, 60icx balance
        Account alice = this.initStudent();
        // increase the number of classes has passed
        for (int i = 1; i <= this.numberOfLesson.intValue(); i++){
            crowdsaleScore.invoke( this.teacher,"openRollCall");
            crowdsaleScore.invoke( alice,"rollCall");
            crowdsaleScore.invoke( this.teacher,"closeRollCall");
            
        }
        BigInteger  _valueOfStudent = alice.getBalance().add(ICX.multiply(BigInteger.valueOf(40)));
        BigInteger  _valueOfTeacher = this.teacher.getBalance();
        crowdsaleScore.invoke( this.teacher,"withdraw");
        // verify
        verify(crowdsaleSpy).withdraw();
        assertEquals(_valueOfTeacher, this.teacher.getBalance());
        assertEquals(BigInteger.ZERO, Account.getAccount(crowdsaleScore.getAddress()).getBalance());

        assertEquals(_valueOfStudent, alice.getBalance());
        assertEquals(BigInteger.ZERO, crowdsaleScore.call("balanceOf", alice.getAddress()));
    }
    @Test
    void studentWithdraw() {
        //create new student with 40icx in course, 60icx balance
        Account alice = this.initStudent();
        // increase the number of classes has passed
        for (int i = 1; i <= this.numberOfLesson.intValue(); i++){
            crowdsaleScore.invoke( this.teacher,"openRollCall");
            crowdsaleScore.invoke( alice,"rollCall");
            crowdsaleScore.invoke( this.teacher,"closeRollCall");
           
        }
        BigInteger  _valueOfStudent = alice.getBalance().add(ICX.multiply(BigInteger.valueOf(40)));
        crowdsaleScore.invoke( alice, "withdraw");
        // verify
        verify(crowdsaleSpy).withdraw();
        assertEquals(BigInteger.ZERO, Account.getAccount(crowdsaleScore.getAddress()).getBalance());
        assertEquals(_valueOfStudent, alice.getBalance());
        assertEquals(BigInteger.ZERO, crowdsaleScore.call("balanceOf", alice.getAddress()));
    } 
    @Test
    void withdraw_whenWrongTime() {
        //create new student with 40icx in course, 60icx balance
        Account alice = this.initStudent();
        // increase the number of classes has passed
        for (int i = 1; i <= this.numberOfLesson.intValue()-2; i++){
            crowdsaleScore.invoke( this.teacher,"openRollCall");
            crowdsaleScore.invoke( alice,"rollCall");
            crowdsaleScore.invoke( this.teacher,"closeRollCall");
           
        }
        BigInteger  _valueOfStudent = alice.getBalance().add(ICX.multiply(BigInteger.valueOf(40)));
        // verify
        assertThrows(AssertionError.class, () -> 
            crowdsaleScore.invoke( alice, "withdraw"));
    }
    @Test
    void withdraw_fromStranger() {
        //create new student with 40icx in course, 60icx balance
        Account alice = this.initStudent();
        // increase the number of classes has passed
        for (int i = 1; i <= this.numberOfLesson.intValue(); i++){
            crowdsaleScore.invoke( this.teacher,"openRollCall");
            crowdsaleScore.invoke( alice,"rollCall");
            crowdsaleScore.invoke( this.teacher,"closeRollCall");
           
        }
        Account randomUser = sm.createAccount();
        // verify
        assertThrows(AssertionError.class, () -> 
            crowdsaleScore.invoke( randomUser, "withdraw"));
    }
   @Test
    void studentWithdraw_withNonRequiredNumberOfClass() {
        //create new student with 40icx in course, 60icx balance
        Account alice = this.initStudent();
        // increase the number of classes has passed
        for (int i = 1; i <= this.numberOfLesson.intValue(); i++){
            crowdsaleScore.invoke( this.teacher,"openRollCall");
            if (i < 8)
                crowdsaleScore.invoke( alice,"rollCall");
            crowdsaleScore.invoke( this.teacher,"closeRollCall");
           
        }
        BigInteger  _valueOfStudent = alice.getBalance().add(ICX.multiply(BigInteger.valueOf(40)));
        // verify
        assertThrows(AssertionError.class, () -> 
            crowdsaleScore.invoke( alice, "withdraw"));
    }
}
