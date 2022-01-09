# Manual Test
Go to SCORE folder

    ./gradlew build
    ./gradlew optimizedJar
    ./gradlew crowdsale:deployToSejong  -PkeystoreName=./godWallet.json -PkeystorePass=gochain

### Change ur current contract address to all of bellow statement
Register course with 10 ICX
    
    goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx transfer --to cxe8fbd4f9488f61f02ce8d30b355e027aae7562d4 --value 10000000000000000000 --key_store ./test.json --key_password btvn123456@ --nid 0x53 --step_limit 2000000000

    0x026e7d2edd5267da93fb186cb7ccd8825b4a3e49db0c09d75a916b255d197f04
        goloop rpc txresult --uri https://sejong.net.solidwallet.io/api/v3

Teacher open roll-call

	goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cxe8fbd4f9488f61f02ce8d30b355e027aae7562d4 \
	    --method openRollCall \
	    --key_store godWallet.json --key_password gochain \
	    --nid 0x53 --step_limit 200000000

"0x9e3d5329c36e015b698c38cbc6f4ef84026a2fc822b3e72c21a6b307e7c7d612"

Student rollcall

    goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cxe8fbd4f9488f61f02ce8d30b355e027aae7562d4 \
        --method rollCall \
        --key_store test.json --key_password btvn123456@ \
        --nid 0x53 --step_limit 200000000


Teacher close roll-call

    goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cxe8fbd4f9488f61f02ce8d30b355e027aae7562d4 \
        --method closeRollCall \
        --key_store godWallet.json --key_password gochain \
        --nid 0x53 --step_limit 200000000


Student withdraw when the course is not finnised
    goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cxe8fbd4f9488f61f02ce8d30b355e027aae7562d4 \
        --method withdraw \
        --key_store test.json --key_password btvn123456@ \
        --nid 0x53 --step_limit 200000000

    {
        "to": "cxe8fbd4f9488f61f02ce8d30b355e027aae7562d4",
        "cumulativeStepUsed": "0x1fc49",
        "stepUsed": "0x1fc49",
        "stepPrice": "0x2e90edd00",
        "eventLogs": [],
        "logsBloom": "0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
        "status": "0x0",
        "failure": {
            "code": "0x20",
            "message": "Reverted(0)"
        },
        "blockHash": "0x34cbc47c7ac6b252ee5e91ec295a890a826b0d0a496f24a14a324909012e78e1",
        "blockHeight": "0x274d13",
        "txIndex": "0x1",
        "txHash": "0xa1a45fac6685af346a9c82cc521dfe09b08ee5adce103eb18b2359aeb737385c"
    }

-----------second class--------------------------------------------
Teacher open roll-call

	goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cxe8fbd4f9488f61f02ce8d30b355e027aae7562d4 \
	    --method openRollCall \
	    --key_store godWallet.json --key_password gochain \
	    --nid 0x53 --step_limit 200000000

"0xdcd87570e09aae084a02a04e0eb2c38a0d51f8f1723bfb9ce15f3a750093df9e"

Student rollcall

    goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cxe8fbd4f9488f61f02ce8d30b355e027aae7562d4 \
        --method rollCall \
        --key_store test.json --key_password btvn123456@ \
        --nid 0x53 --step_limit 200000000

Student rollcall again

    goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cxe8fbd4f9488f61f02ce8d30b355e027aae7562d4 \
        --method rollCall \
        --key_store test.json --key_password btvn123456@ \
        --nid 0x53 --step_limit 200000000
    
Teacher close roll-call

    goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cxe8fbd4f9488f61f02ce8d30b355e027aae7562d4 \
        --method closeRollCall \
        --key_store godWallet.json --key_password gochain \
        --nid 0x53 --step_limit 200000000
Teacher try to open roll-call when maximum class

	goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cxe8fbd4f9488f61f02ce8d30b355e027aae7562d4 \
	    --method openRollCall \
	    --key_store godWallet.json --key_password gochain \
	    --nid 0x53 --step_limit 200000000
Student claim token 

    goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cxe8fbd4f9488f61f02ce8d30b355e027aae7562d4 \
        --method withdraw \
        --key_store test.json --key_password btvn123456@ \
        --nid 0x53 --step_limit 200000000

Teacher withdraw

    goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cxe8fbd4f9488f61f02ce8d30b355e027aae7562d4 \
        --method withdraw \
        --key_store godWallet.json --key_password gochain \
        --nid 0x53 --step_limit 200000000

# integration test
install extention "live server" into vscode and start the sever

go to gochain_local folder and run:

    ./run_gochain.sh stop
    ./run_gochain.sh start

turn back to project and run:

    ./gradlew build
    ./gradlew optimizedJar
    ./gradlew crowdsale:integrationTest

open chrome and go to http://127.0.0.1:5500/SCORE/crowdsale/build/reports/tests/integrationTest/classes/foundation.icon.test.cases.CrowdsaleTest.html
    to see result

# unit test
install extention "live server" into vscode and start the sever
in project, run:

    ./gradlew test

open chrome and go to http://127.0.0.1:5500/SCORE/crowdsale/build/reports/tests/test/classes/devera.score.example.CrowdsaleTest.html
    to see result

# using web ui to contact with course

go to server folder in this project, run:

    npm install
    npm start

open chrome and go to http://localhost:8080 to see result
