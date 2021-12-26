/*
 * Copyright 2018 ICON Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package foundation.icon.test.score;

import foundation.icon.icx.Wallet;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.data.IconAmount;
import foundation.icon.icx.data.TransactionResult;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import foundation.icon.icx.transport.jsonrpc.RpcValue;
import foundation.icon.test.Constants;
import foundation.icon.test.ResultTimeoutException;
import foundation.icon.test.TransactionFailureException;
import foundation.icon.test.TransactionHandler;

import java.io.IOException;
import java.math.BigInteger;

import static foundation.icon.test.Env.LOG;

public class CrowdSaleScore extends Score {

    public static CrowdSaleScore mustDeploy(TransactionHandler txHandler, Wallet wallet
        ,BigInteger _tuition, BigInteger _numberOfLesson,BigInteger _durationInDefault)
            throws ResultTimeoutException, TransactionFailureException, IOException {
        LOG.infoEntering("deploy", "Crowdsale");
        RpcObject params = new RpcObject.Builder()
                .put("_tuition", new RpcValue(_tuition))
                .put("_numberOfLesson", new RpcValue(_numberOfLesson))
                .put("_durationInDefault", new RpcValue(_durationInDefault))
                .build();
        Score score = txHandler.deploy(wallet, getFilePath("crowdsale"), params);
        LOG.info("scoreAddr = " + score.getAddress());
        LOG.infoExiting();
        return new CrowdSaleScore(score);
    }

    public CrowdSaleScore(Score other) {
        super(other);
    }
    public TransactionResult openRollCall(Wallet wallet)
            throws ResultTimeoutException, IOException {
        RpcObject params = new RpcObject.Builder()
            .build();
        return invokeAndWaitResult(wallet, "openRollCall", params);
    }
    public TransactionResult closeRollCall(Wallet wallet)
            throws ResultTimeoutException, IOException {
        RpcObject params = new RpcObject.Builder()
            .build();
        return invokeAndWaitResult(wallet, "closeRollCall", params);
    }
    public TransactionResult rollCall(Wallet wallet)
            throws ResultTimeoutException, IOException {
        RpcObject params = new RpcObject.Builder()
            .build();
        return invokeAndWaitResult(wallet, "rollCall", params);
    }
    public TransactionResult withdraw(Wallet wallet)
            throws ResultTimeoutException, IOException {
        RpcObject params = new RpcObject.Builder()
            .build();
        return invokeAndWaitResult(wallet, "withdraw", params);
    }
    public BigInteger amountRaised()
            throws ResultTimeoutException, IOException {
        return this.call("amountRaised", null).asInteger();
    }
    public Boolean isOpenRollCall()
            throws ResultTimeoutException, IOException {
        return this.call("isOpenRollCall", null).asBoolean();
    }
    public BigInteger balanceOf(Address owner) throws IOException {
        RpcObject params = new RpcObject.Builder()
                .put("_owner", new RpcValue(owner))
                .build();
        return call("balanceOf", params).asInteger();
    }
    public String test(Address owner) throws IOException {
        RpcObject params = new RpcObject.Builder()
                .put("_from", new RpcValue(owner))
                .build();
        return call("test", params).toString();
    }
    public void ensureTuitionBalance(Address owner, long value) throws ResultTimeoutException, IOException {
        long limitTime = System.currentTimeMillis() + Constants.DEFAULT_WAITING_TIME;
        while (true) {
            BigInteger balance = balanceOf(owner);
            String msg = "ICX balance of " + owner + "in course: " + balance;
            if (balance.equals(BigInteger.valueOf(0))) {
                try {
                    if (limitTime < System.currentTimeMillis()) {
                        throw new ResultTimeoutException();
                    }
                    // wait until block confirmation
                    LOG.info(msg + "; Retry in 1 sec.");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (balance.equals(BigInteger.valueOf(value).multiply(BigInteger.TEN.pow(18)))) {
                LOG.info(msg);
                break;
            } else {
                throw new IOException("ICX balance mismatch!");
            }
        }
    }
    public void ensureOpenRoll(Wallet wallet) throws Exception {
        while (true) {
            TransactionResult result = openRollCall(wallet);
            if (!Constants.STATUS_SUCCESS.equals(result.getStatus())) {
                throw new IOException("Failed to execute open roll call.");
            }
            TransactionResult.EventLog event = findEventLog(result, getAddress(), "ActiveCourse(Address,int)");
            if (event != null) {
                BigInteger _amount = event.getIndexed().get(2).asInteger();
                LOG.info("class is open: " + _amount.toString());

                break;
            }
            LOG.info("Sleep 1 second.");
            Thread.sleep(1000);
        }
    }
    public void ensureRollCall(Wallet wallet) throws Exception {
        while (true) {
            TransactionResult result = rollCall(wallet);
            if (!Constants.STATUS_SUCCESS.equals(result.getStatus())) {
                throw new IOException("Failed to execute roll call.");
            }
            TransactionResult.EventLog event = findEventLog(result, getAddress(), "RollCall(Address,int)");
            if (event != null) {
                Address _address = event.getIndexed().get(1).asAddress();
                BigInteger _amount = event.getIndexed().get(2).asInteger();
                LOG.info(_address.toString() + " rollcall. " + "Total of this student: " + _amount.toString());

                break;
            }
            TransactionResult.EventLog _event = findEventLog(result, getAddress(), "FailRollCall(Address)");
            if (_event != null) {
                
                LOG.info(wallet.getAddress() + " rollcall." + " Fail to roll call");

                break;
            }
            LOG.info("Sleep 1 second.");
            Thread.sleep(1000);
        }
    }


    
}
