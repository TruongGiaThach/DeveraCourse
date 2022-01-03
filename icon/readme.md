# Manual Test
    ./gradlew build
    ./gradlew optimizedJar
    ./gradlew crowdsale:deployToSejong  -PkeystoreName=./godWallet.json -PkeystorePass=gochain

### Change ur current contract address to all of bellow statement
register course with 10 ICX
    
    goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx transfer --to cx00ba43897d8a9e2363e6626250ed6fac00ff2eb7 --value 10000000000000000000 --key_store ./test.json --key_password btvn123456@ --nid 0x53 --step_limit 2000000000

    0x026e7d2edd5267da93fb186cb7ccd8825b4a3e49db0c09d75a916b255d197f04
        goloop rpc txresult --uri https://sejong.net.solidwallet.io/api/v3

teacher open roll-call

	goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cx00ba43897d8a9e2363e6626250ed6fac00ff2eb7 \
	    --method openRollCall \
	    --key_store godWallet.json --key_password gochain \
	    --nid 0x53 --step_limit 200000000

"0x9e3d5329c36e015b698c38cbc6f4ef84026a2fc822b3e72c21a6b307e7c7d612"

student rollcall

    goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cx00ba43897d8a9e2363e6626250ed6fac00ff2eb7 \
        --method rollCall \
        --key_store test.json --key_password btvn123456@ \
        --nid 0x53 --step_limit 200000000


teacher close roll-call

    goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cx00ba43897d8a9e2363e6626250ed6fac00ff2eb7 \
        --method closeRollCall \
        --key_store godWallet.json --key_password gochain \
        --nid 0x53 --step_limit 200000000

student withdraw when the course is not finnised
    goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cx00ba43897d8a9e2363e6626250ed6fac00ff2eb7 \
        --method withdraw \
        --key_store test.json --key_password btvn123456@ \
        --nid 0x53 --step_limit 200000000
-----------second class--------------------------------------------
teacher open roll-call

	goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cx00ba43897d8a9e2363e6626250ed6fac00ff2eb7 \
	    --method openRollCall \
	    --key_store godWallet.json --key_password gochain \
	    --nid 0x53 --step_limit 200000000

"0x9e3d5329c36e015b698c38cbc6f4ef84026a2fc822b3e72c21a6b307e7c7d612"

student rollcall

    goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cx00ba43897d8a9e2363e6626250ed6fac00ff2eb7 \
        --method rollCall \
        --key_store test.json --key_password btvn123456@ \
        --nid 0x53 --step_limit 200000000

student rollcall again

    goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cx00ba43897d8a9e2363e6626250ed6fac00ff2eb7 \
        --method rollCall \
        --key_store test.json --key_password btvn123456@ \
        --nid 0x53 --step_limit 200000000
    
teacher close roll-call

    goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cx00ba43897d8a9e2363e6626250ed6fac00ff2eb7 \
        --method closeRollCall \
        --key_store godWallet.json --key_password gochain \
        --nid 0x53 --step_limit 200000000
teacher try to open roll-call when maximum class

	goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cx00ba43897d8a9e2363e6626250ed6fac00ff2eb7 \
	    --method openRollCall \
	    --key_store godWallet.json --key_password gochain \
	    --nid 0x53 --step_limit 200000000
student claim token 

    goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cx00ba43897d8a9e2363e6626250ed6fac00ff2eb7 \
        --method withdraw \
        --key_store test.json --key_password btvn123456@ \
        --nid 0x53 --step_limit 200000000

teacher withdraw

    goloop rpc --uri https://sejong.net.solidwallet.io/api/v3 sendtx call  --to cx00ba43897d8a9e2363e6626250ed6fac00ff2eb7 \
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