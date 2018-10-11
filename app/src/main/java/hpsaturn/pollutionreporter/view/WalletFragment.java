package hpsaturn.pollutionreporter.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.hpsaturn.tools.Logger;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import butterknife.BindView;
import butterknife.ButterKnife;
import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.api.GitHubApi;
import hpsaturn.pollutionreporter.api.ResponseCallback;
import hpsaturn.pollutionreporter.crypto.CryptoUtils;
import okhttp3.ResponseBody;


/**
 * Created by Antonio Vanegas @hpsaturn on 6/30/18.
 */
public class WalletFragment extends Fragment implements View.OnClickListener {

    private static final String GITHUB_USER = "hi";

    public static String TAG = WalletFragment.class.getSimpleName();
    private static Context context;


    @BindView(R.id.tv_address)
    TextView tv_address;

    @BindView(R.id.tv_balance)
    TextView tv_balance;

    @BindView(R.id.tv_wallet)
    TextView tv_wallet;

    @BindView(R.id.et_pwd)
    EditText et_pwd;


    @BindView(R.id.bt_create_wallet)
    Button bt_wallet;

    @BindView(R.id.bt_balance)
    Button bt_balance;

    @BindView(R.id.bt_create_issue)
    Button bt_issue;


    @BindView(R.id.progress_bar)
    ProgressBar pb_loader;


    private int i;
    private long referenceTimestamp;
    private boolean loadingData = true;

    public static final String KEY_RECORD_ID = "key_record_id";
    private String recordId;
    private String walletFile;
    private static Web3j web3;
    private boolean networkCallIsInProgress;
    private String address;
    private String pwd;
    private int count;

    public static WalletFragment newInstance() {
        WalletFragment fragment = new WalletFragment();
        return fragment;
    }

    public static WalletFragment newInstance(String recordId) {
        WalletFragment fragment = new WalletFragment();
        Bundle args = new Bundle();
        args.putString(KEY_RECORD_ID, recordId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        ButterKnife.bind(this, view);
        bt_balance.setOnClickListener(this);
        bt_wallet.setOnClickListener(this);
        bt_issue.setOnClickListener(this);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void touchWallet() {
        walletFile = CryptoUtils.genWallet(context, et_pwd.getText().toString());
        pwd = et_pwd.getText().toString();
        Credentials credentials = CryptoUtils.loadCredentials(context, walletFile, pwd);
        address = credentials.getAddress();
        web3 = CryptoUtils.connectEth();
        String balance = CryptoUtils.getAccountBalance(web3, address);
        tv_address.setText(address);
        tv_balance.setText(balance);
        tv_wallet.setText(walletFile);

    }

    public void checkBalance() {
        String balance = CryptoUtils.getAccountBalance(web3, address);
        tv_balance.setText(balance);

    }

    public void sendReport() {
        if (walletFile != null && pwd != null && web3 != null) {
            Credentials credentials = CryptoUtils.loadCredentials(context, walletFile, pwd);
            String tx_reporter = CryptoUtils.createOfflineContracReporterstTx(web3, "reporterReward", credentials);
            CryptoUtils.sendRawSignedTx(web3, tx_reporter, credentials);

        }
    }

    private void syncUI() {
        getActivity().runOnUiThread(() -> {
            if (networkCallIsInProgress) {
                pb_loader.setVisibility(View.VISIBLE);
            } else {
                pb_loader.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void setNetworkCallInProgress(final boolean inProgress) {
        networkCallIsInProgress = inProgress;
        syncUI();
    }

    private void issueJoinCommunityRequest() {
        setNetworkCallInProgress(true);
        String yamlBody = String.format(
                "name: AIRQ bot testing reports\n" +
                        "motivation: im a bot testing airquality reports\n" +
                        "address: '%s'", address);
        final GitHubApi gitHubApi = GitHubApi.getSharedInstance();

        ResponseCallback<ResponseBody> cbGithub = new ResponseCallback<ResponseBody>() {
            @Override
            public void onSuccess(@NonNull ResponseBody result) {
                setNetworkCallInProgress(false);
            }

            @Override
            public void onError() {
                setNetworkCallInProgress(false);

            }

            @Override
            public void onFailure() {
                setNetworkCallInProgress(false);

            }
        };
        gitHubApi.createIssue(cbGithub, yamlBody);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_create_wallet:
                touchWallet();
                break;
            case R.id.bt_balance:
                checkBalance();
                break;
            case R.id.bt_create_issue:
                issueJoinCommunityRequest();
                break;
        }

    }

    public void addData(int p25) {
        count++;
        if (count % 3 == 0) {
            Logger.i(TAG, "sending new Report" + p25);
            sendReport();
        }

    }
}
