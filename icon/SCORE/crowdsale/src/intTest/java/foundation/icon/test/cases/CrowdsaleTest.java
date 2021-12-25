/*
 * Copyright 2019 ICON Foundation
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

package foundation.icon.test.cases;

import foundation.icon.icx.IconService;
import foundation.icon.icx.KeyWallet;
import foundation.icon.icx.data.Bytes;
import foundation.icon.icx.data.TransactionResult;
import foundation.icon.icx.transport.http.HttpProvider;
import foundation.icon.test.Constants;
import foundation.icon.test.Env;
import foundation.icon.test.TestBase;
import foundation.icon.test.TransactionHandler;
import foundation.icon.test.score.CrowdSaleScore;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigInteger;

import static foundation.icon.test.Env.LOG;

class CrowdsaleTest extends TestBase {
    private static TransactionHandler txHandler;
    private static KeyWallet[] wallets;
    private static KeyWallet ownerWallet;

    @BeforeAll
    static void setup() throws Exception {
        Env.Chain chain = Env.getDefaultChain();
        IconService iconService = new IconService(new HttpProvider(chain.getEndpointURL(3)));
        txHandler = new TransactionHandler(iconService, chain);

        // init wallets
        wallets = new KeyWallet[4];
        BigInteger amount = ICX.multiply(BigInteger.valueOf(200));
        for (int i = 0; i < wallets.length; i++) {
            wallets[i] = KeyWallet.create();
            txHandler.transfer(wallets[i].getAddress(), amount);
        }
        for (KeyWallet wallet : wallets) {
            ensureIcxBalance(txHandler, wallet.getAddress(), BigInteger.ZERO, amount);
        }
        ownerWallet = wallets[0];
    }

    @AfterAll
    static void shutdown() throws Exception {
        for (KeyWallet wallet : wallets) {
            txHandler.refundAll(wallet);
        }
    }

    @Test
    void deployAndStartCrowdsale() throws Exception {
        // deploy SCORE
        BigInteger _tuition = BigInteger.valueOf(10);
        BigInteger _numberOfLesson = BigInteger.valueOf(2);
        BigInteger _durationInDefault = BigInteger.valueOf(3600);

        CrowdSaleScore crowdsaleScore = CrowdSaleScore.mustDeploy(txHandler, ownerWallet,
                _tuition, _numberOfLesson, _durationInDefault);
    
        startCrowdsale(crowdsaleScore, _tuition , _numberOfLesson, _durationInDefault);
    }

    void startCrowdsale( CrowdSaleScore crowdsaleScore,
                BigInteger _tuition, BigInteger _numberOfLesson,BigInteger _durationInDefault) throws Exception {
        KeyWallet A_Wallet = wallets[1];
        KeyWallet B_Wallet = wallets[2];
        KeyWallet C_Wallet = wallets[3];
        KeyWallet teacher = wallets[0];


        // register the course for A and B
        LOG.infoEntering("transfer icx", "to crowdsale score (40 from Alice, 60 from Bob)");
        Bytes[] ids = new Bytes[2];
        BigInteger aliceDepositIcxAmount = BigInteger.valueOf(40);
        BigInteger bobDepositIcxAmount = BigInteger.valueOf(60);
        ids[0] = txHandler.transfer(A_Wallet, crowdsaleScore.getAddress(), ICX.multiply(aliceDepositIcxAmount));
        ids[1] = txHandler.transfer(B_Wallet, crowdsaleScore.getAddress(), ICX.multiply(bobDepositIcxAmount));
        for (Bytes id : ids) {
            assertSuccess(txHandler.getResult(id));
        }
        crowdsaleScore.ensureTuitionBalance(A_Wallet.getAddress(),40);
        crowdsaleScore.ensureTuitionBalance(B_Wallet.getAddress(),60);
        LOG.infoExiting();
        
        LOG.infoEntering("Begin course ");
        // first class
        crowdsaleScore.ensureOpenRoll(teacher);
        for (int i = 1; i < wallets.length; i++) {
            crowdsaleScore.ensureRollCall(wallets[i]);
        }
        LOG.infoExiting();
        

/*
        // do safe withdrawal
        LOG.infoEntering("call", "withdraw()");
        BigInteger withdrawAmount = ICX.multiply(BigInteger.valueOf(30));
        String description = "lesson 8";
        crowdsaleScore.withdraw(ownerWallet, withdrawAmount, description);
        if (!Constants.STATUS_SUCCESS.equals(result.getStatus())) {
            throw new IOException("Failed to execute withdraw.");
        }
        crowdsaleScore.ensureWithdrawal(withdrawAmount, BigInteger.ZERO, description);

        crowdsaleScore.voteWithdrawal(aliceWallet);
        crowdsaleScore.ensureWithdrawal(withdrawAmount, ICX.multiply(aliceDepositIcxAmount), description);
        crowdsaleScore.voteWithdrawal(bobWallet);

        crowdsaleScore.ensureWithdrawal(withdrawAmount, ICX.multiply(aliceDepositIcxAmount).add(ICX.multiply(bobDepositIcxAmount)), description);

        BigInteger oldBal = txHandler.getBalance(ownerWallet.getAddress());
        result = crowdsaleScore.executeWithdrawal(ownerWallet);
        // check the final icx balance of owner
        LOG.info("ICX balance before safeWithdrawal: " + oldBal);
        BigInteger fee = result.getStepUsed().multiply(result.getStepPrice());
        BigInteger newBal = oldBal.add(withdrawAmount).subtract(fee);
        ensureIcxBalance(txHandler, ownerWallet.getAddress(), oldBal, newBal);
        assertEquals(ICX.multiply(BigInteger.valueOf(70)), crowdsaleScore.amountRaised());
        LOG.infoExiting();
        */
    }
}
