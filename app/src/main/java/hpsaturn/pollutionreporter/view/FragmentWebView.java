package hpsaturn.pollutionreporter.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hpsaturn.tools.Logger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import hpsaturn.pollutionreporter.R;
import vcm.github.webkit.proview.ProWebResourceError;
import vcm.github.webkit.proview.ProWebResourceRequest;
import vcm.github.webkit.proview.ProWebView;

/**
 * Created by Antonio Vanegas @hpsaturn on 3/28/19.
 */
public class FragmentWebView extends Fragment {

    public static final String TAG = FragmentWebView.class.getSimpleName();

    private static final String KEY_URL = "key_url";

    private String url = "";

    @BindView(R.id.webview)
    ProWebView webView;

    public static FragmentWebView newInstance(String url) {
        FragmentWebView fragment = new FragmentWebView();
        Bundle args = new Bundle();
        args.putString(KEY_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(KEY_URL)) {
            url = getArguments().getString(KEY_URL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_webview, container, false);
        ButterKnife.bind(this, view);

        webView.setActivity(getActivity()); // Also works with fragments!
        webView.setProClient(new ProWebView.ProClient() {

            @Override
            public void onProgressChanged(ProWebView webView, int progress) {
                Logger.v(TAG,"onProgressChanged: "+progress);

            }

            @Override
            public void onStateChanged(ProWebView webView) {
                Logger.v(TAG,"onStateChanged");

            }

            @Override
            public void onWindowStateChanged(ProWebView webView, boolean showing) {
                Logger.v(TAG,"onWindowStateChanged, showing: "+showing);

            }

            @Override
            public void onInformationReceived(ProWebView webView, String url, String title, Bitmap favicon) {
                Logger.v(TAG,"onInformationReceived, url: "+url+" title:"+title);

            }

            @Override
            public void onCustomViewStateChanged(ProWebView webView, View customView, boolean showing) {
                Logger.v(TAG,"onCustomViewStateChanged");

            }

            @Override
            public void onDownloadStarted(ProWebView webView, String filename, String url) {
                Logger.v(TAG,"onDownloadStarted");

            }

            @Override
            public void onReceivedError(ProWebView webView, ProWebResourceRequest resourceRequest, ProWebResourceError webResourceError) {
                Logger.v(TAG,"onReceivedError "+webResourceError.getDescription());

            }
        });

        webView.loadUrl(url);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        webView.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        webView.onRequestPermissionResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.onSavedInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        webView.onDestroy();
        super.onDestroyView();
    }
}
