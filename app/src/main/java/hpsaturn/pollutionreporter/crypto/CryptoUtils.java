package hpsaturn.pollutionreporter.crypto;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import hpsaturn.pollutionreporter.BuildConfig;

public class CryptoUtils {

    private static final String TAG = CryptoUtils.class.getSimpleName();

    public static String genWallet(Context ctx, String pwd){
        try {
            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            WalletFile walletFile = Wallet.createLight(pwd, ecKeyPair);
            ObjectMapper objectMapper = new ObjectMapper();
            File walletPathFile = new File(ctx.getApplicationInfo().dataDir);
            String fileName = getWalletFileName(walletFile);
            Log.d("ETH",fileName);
            File destination = new File(walletPathFile, fileName);
            try {
                objectMapper.writeValue(destination, walletFile);
                return fileName;
            } catch (IOException e) {
                Log.e("IO EXCEPTION ETH",e.getMessage());
            }
        } catch (CipherException e) {
            Log.e("ETH",e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.e("ETH",e.getMessage());
        } catch (NoSuchProviderException e) {
            Log.e("ETH",e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            Log.e("ETH",e.getMessage());
        }
        return null;
    }


    public static Credentials loadCredentials(Context ctx, String fileName, String pwd) {
        Credentials credentials = null;
        try {
            File walletPathFile = new File(ctx.getApplicationInfo().dataDir);
            credentials = WalletUtils.loadCredentials(pwd, new File(walletPathFile, fileName));
            return credentials;
        } catch (IOException e) {
            Log.e("ETH",e.getMessage());
        } catch (CipherException e) {
            Log.e("ETH",e.getMessage());
        }
        return null;
    }


    private static String getWalletFileName(WalletFile walletFile) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("'UTC--'yyyy-MM-dd'T'HH-mm-ss.SSS'--'");
        return dateFormat.format(new Date()) + walletFile.getAddress() + ".json";
    }

    public static String signTx(RawTransaction rawTransaction, Credentials credentials){
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        return hexValue;
    }
    public static String createOfflineContracReporterstTx(Web3j web3, String function, Credentials credentials) {
        RawTransaction rawTX = createRawTX(web3, credentials.getAddress(), function);
        String signedTx= signTx(rawTX, credentials);
        return signedTx;
    }

    public static RawTransaction createRawTX(Web3j web3, String address, String fx){
        String set_contract_Address = BuildConfig.contract;
        Function function = new Function(
                fx,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(address)),
                Collections.<TypeReference<?>>emptyList());
        BigInteger count = nonce(web3,address);
        String encodedFunction = FunctionEncoder.encode(function);
	
        RawTransaction rawTransaction = RawTransaction.createTransaction(
                count, ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT, set_contract_Address, BigInteger.ZERO,  encodedFunction);
        return rawTransaction;

    }

    public static BigInteger nonce(Web3j web3, String address){
        try {
            Future<EthGetTransactionCount> ethGetTransactionCount = web3.ethGetTransactionCount(
                    address, DefaultBlockParameterName.LATEST)
                    .sendAsync();
            BigInteger count = null;
            count = ethGetTransactionCount.get().getTransactionCount();
            return count;
        } catch (InterruptedException e) {
            Log.e("ERROR ETH EXEC",e.getMessage() + Arrays.deepToString(e.getStackTrace()));
        } catch (ExecutionException e) {
            Log.e("ERROR ETH EXEC",e.getMessage() + Arrays.deepToString(e.getStackTrace()));
        }
        return null;
    }


    public static  Web3j connectEth() {
        Web3j web3 = Web3jFactory.build(new HttpService(BuildConfig.infura));  // defaults to http://localhost:8545/
        return web3;
    }

    public String account(Credentials c) {
        return c.getAddress();
    }

    public static void sendRawSignedTx(Web3j web3, String offlineTx, Credentials credentials) {
        // Context of the app under test.
        Log.d(TAG, credentials.getAddress());
        EthSendTransaction transactionResponse = null;
        try {
            transactionResponse = web3.ethSendRawTransaction(offlineTx).sendAsync().get();
            Log.i("ETH", ("Transaction complete, view it at https://rinkeby.etherscan.io/tx/"
                    + transactionResponse.getTransactionHash()));
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        } catch (ExecutionException e) {
            Log.e(TAG, e.getMessage());
        }
    }




    public static String getAccountBalance( Web3j web3, String address){
        try {
            Future<EthGetBalance> ethGetBalance = web3.ethGetBalance(
                    address, DefaultBlockParameterName.LATEST)
                    .sendAsync();
            BigInteger value = ethGetBalance.get().getBalance();
            BigDecimal balance = new BigDecimal(value.toString());
            BigDecimal weiEth = new BigDecimal(1e18);
            balance = balance.divide(weiEth, MathContext.DECIMAL32);
            return balance.toString();
        } catch (InterruptedException e) {
            Log.e("ERROR ETH INT",e.getMessage());
        } catch (ExecutionException e) {
            Log.e("ERROR ETH EXEC",e.getMessage() + Arrays.deepToString(e.getStackTrace()));
        }
        return null;
    }


}
