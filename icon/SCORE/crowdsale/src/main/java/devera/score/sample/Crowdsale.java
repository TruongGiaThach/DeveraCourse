package devera.score.example;

import score.Address;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.ArrayDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;

import java.math.BigInteger;
import java.util.Map;

import scorex.util.ArrayList;

public class Crowdsale
{
    private static final BigInteger ONE_ICX = new BigInteger("1000000000000000000");
    private final Address teacher;
    private final BigInteger tuition;
    private final BigInteger numberOfLesson;
    private BigInteger currentNumberOfLesson;
    private Boolean activeCourse;
    private final long deadline;
    private final DictDB<Address, BigInteger> balanceOfStudents;
    private final VarDB<BigInteger> amountRaised;
    private final ArrayDB<Address> listStudent;
    private final DictDB<Address, BigInteger> counter;
    private final DictDB<Address, Boolean> check;
    

    public Crowdsale(BigInteger _tuition, BigInteger _numberOfLesson,BigInteger _durationInDefault) {
        // some basic requirements
        Context.require(_tuition.compareTo(BigInteger.ZERO) >= 0);
        Context.require(_numberOfLesson.compareTo(BigInteger.ZERO) >= 0);

        this.teacher = Context.getCaller();
        this.tuition = ONE_ICX.multiply(_tuition);
        this.numberOfLesson = _numberOfLesson;
        this.currentNumberOfLesson = BigInteger.ZERO;
        this.deadline = Context.getBlockHeight() + _durationInDefault.longValue();
        this.activeCourse = false;

        this.balanceOfStudents = Context.newDictDB("balanceOfStudents", BigInteger.class);
        this.amountRaised = Context.newVarDB("amountRaised", BigInteger.class);
        this.counter = Context.newDictDB("counter", BigInteger.class);
        this.check = Context.newDictDB("check", Boolean.class);
        this.listStudent = Context.newArrayDB("listStudent" , Address.class);
    }

    @External(readonly=true)
    public String name() {
        return "Devera Course";
    }

    @External(readonly=true)
    public String description() {
        return "Blockchain course from Devera";
    }

    @External(readonly=true)
    public BigInteger balanceOf(Address _owner) {
        return this.safeGetBalance(_owner);
    }

    @External(readonly=true)
    public BigInteger amountRaised() {
        return safeGetAmountRaised();
    }

    @External(readonly=true)
    public BigInteger CurrentLesson() {
        
        return this.currentNumberOfLesson;
    }
    @External(readonly=true)
    public BigInteger TotalNumberOfLesson() {
        
        return this.numberOfLesson;
    }

    @External(readonly=true)
    public BigInteger checkNumberOfStudentAttended(Address _owner) {
        
        return this.safeGetCounter(_owner);
    }

    @External(readonly=true)
    public String isOpenRollCall() {
        
        return this.activeCourse.toString();
    }
    /*
     * Called when anyone sends funds to the SCORE and that funds would be regarded as a contribution.
     */
    @Payable
    public void fallback() {
        // check if the course is ended
        Context.require(!this.afterEndCourse());

        Address _from = Context.getCaller();
        BigInteger _value = Context.getValue();
        Context.require(_value.compareTo(BigInteger.ZERO) > 0);

        //check tuition
        Context.require(_value.compareTo(this.tuition) >= 0);

        // accept the tuition
        BigInteger fromBalance = safeGetBalance(_from);
        this.balanceOfStudents.set(_from, fromBalance.add(_value));

        //add student address to list student
        this.listStudent.add(_from);

        // increase the total amount of funding
        BigInteger amountRaised = safeGetAmountRaised();
        this.amountRaised.set(amountRaised.add(_value));

        // emit eventlog
        Registration(_from, _value);
    }
    
    private Boolean isAvailableToRefund(Address _from){
        BigInteger _studentCounter = this.safeGetCounter(_from);
        if (_studentCounter.compareTo(BigInteger.ZERO) <= 0)
            return false;

        BigInteger _require = this.numberOfLesson.multiply(BigInteger.valueOf(80));
        _require = _require.divide(BigInteger.valueOf(100));
        BigInteger _studentTuition = this.safeGetBalance(_from);
        Boolean _isRefund = (_studentTuition.compareTo(BigInteger.ZERO) > 0);
        return ((_studentCounter.compareTo(_require) >= 0) && _isRefund);
    }

    private void refund(Address _from, BigInteger _value){
        this.amountRaised.set(this.amountRaised.get().subtract(_value));
        this.balanceOfStudents.set(_from,BigInteger.ZERO);
        Context.transfer(_from,_value);
    }

    @External
    public void withdraw() {
        // only withdraw when the course is finished
        Context.require(this.afterEndCourse());
        Address _from = Context.getCaller();
        //if caller is teacher
        if (_from.equals(this.teacher) ){
            
            // refund tuition to student first
            for (int i = 0; i < this.listStudent.size() ; i++){
                if (isAvailableToRefund(this.listStudent.get(i))){
                    Address _studentAddress = this.listStudent.get(i);
                    BigInteger _value = this.safeGetBalance(_studentAddress);
                    Context.require(_value.compareTo(BigInteger.ZERO) > 0);

                    refund(_studentAddress, _value);
                }
            }
            //refund to teacher
            BigInteger _amount = this.amountRaised.get();
            this.amountRaised.set(BigInteger.ZERO);
            Context.transfer(this.teacher, _amount);
            return;
        }

        //check existed student
        BigInteger _value = this.safeGetBalance(_from);
        Context.require(_value.compareTo(BigInteger.ZERO) > 0);

        //check requirement number of class
        Context.require(isAvailableToRefund(_from));

        //refund to student
        refund(_from, _value);




        
    }
    
    @External
    public void rollCall() {
        Address _from = Context.getCaller();
        // check if it is in the time of roll call
        if (this.activeCourse && !this.check.getOrDefault(_from, true) )
            if (!afterDeadline()){
                BigInteger tmp_num = this.safeGetCounter(_from);
                this.counter.set(_from, tmp_num.add(BigInteger.ONE));
                this.check.set(_from,true);
                //even log
                RollCall(_from,this.safeGetCounter(_from));    
                return;
            }     
            else {
                this.activeCourse = false;
        } 
        //even log
        FailRollCall(_from);
    }

    @External
    public void openRollCall() {
        Address _from = Context.getCaller();
        Context.require(this.teacher.equals(_from));
        Context.require(!this.activeCourse);
        //create new list roll call
        for (int i = 0; i < this.listStudent.size() ; i++)
            this.check.set(this.listStudent.get(i),false);
        
        //check end of course
        if (!afterEndCourse()){
            this.activeCourse = true;
            BigInteger one_big =  new BigInteger("1");
            this.currentNumberOfLesson = this.currentNumberOfLesson.add(one_big);
            //even log
            ActiveCourse(_from,this.currentNumberOfLesson);
            return;
        }
        FailToOpenRollCall();
        
    }
    @External
    public void closeRollCall() {
        Address _from = Context.getCaller();
        Context.require(this.teacher.equals(_from));
        Context.require(this.activeCourse);
        
        this.activeCourse = false;
        
      
        //even log
        InActiveCourse(_from,this.currentNumberOfLesson);
    }

    private BigInteger safeGetBalance(Address owner) {
        return this.balanceOfStudents.getOrDefault(owner, BigInteger.ZERO);
    }

  
    private BigInteger safeGetAmountRaised() {
        return this.amountRaised.getOrDefault(BigInteger.ZERO);
    }

     private BigInteger safeGetCounter(Address owner) {
        return this.counter.getOrDefault(owner,BigInteger.ZERO);
    }
    private Boolean afterEndCourse() {
        
        return this.currentNumberOfLesson.compareTo(this.numberOfLesson) >= 0;
    }
    private Boolean afterDeadline() {
        // checks if it has been reached to the deadline block
        return Context.getBlockHeight() >= this.deadline;
    }
    
    @EventLog(indexed=2)
    protected void Registration(Address _from,BigInteger _value){};
    
    @EventLog(indexed=2)
    protected void RollCall(Address _from,BigInteger _currentNumberOfLessonAttended){};    

    @EventLog(indexed=1)
    protected void FailRollCall(Address _from){};

    @EventLog(indexed=2)
    protected void ActiveCourse(Address _from,BigInteger _currentNumberOfLesson){};

    @EventLog(indexed=0)
    protected void FailToOpenRollCall(){};

    @EventLog(indexed=2)
    protected void InActiveCourse(Address _from,BigInteger _currentNumberOfLesson){};


}
