package com.codesync.android;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // User enters ABC123 -> http://172.190.1.95:8082/codesync/share/ABC123
    private static final String CODE_SYNC_URL =
            "http://172.190.1.95:8082/codesync/share/";

    private LinearLayout startScreen;
    private LinearLayout webScreen;
    private LinearLayout loadingOverlay;
    private EditText shareKeyInput;
    private TextView accessButton;
    private TextView pasteButton;
    private TextView errorText;
    private TextView webBack;
    private TextView webReload;
    private ProgressBar progressBar;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeButtons();
        initializeWebView();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webScreen.getVisibility() == View.VISIBLE) {
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        showStartScreen();
                    }
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void initializeViews() {
        startScreen = findViewById(R.id.startScreen);
        webScreen = findViewById(R.id.webScreen);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        shareKeyInput = findViewById(R.id.shareKeyInput);
        accessButton = findViewById(R.id.accessButton);
        pasteButton = findViewById(R.id.pasteButton);
        errorText = findViewById(R.id.errorText);
        webBack = findViewById(R.id.webBack);
        webReload = findViewById(R.id.webReload);
        progressBar = findViewById(R.id.progressBar);
        webView = findViewById(R.id.webView);
    }

    private void initializeButtons() {
        accessButton.setOnClickListener(v -> openCodeSync());
        pasteButton.setOnClickListener(v -> pasteShareKey());
        webReload.setOnClickListener(v -> webView.reload());
        webBack.setOnClickListener(v -> showStartScreen());

        shareKeyInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO
                    || actionId == EditorInfo.IME_ACTION_DONE) {
                openCodeSync();
                return true;
            }
            return false;
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initializeWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {

            // false = keep navigation inside the WebView
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                loadingOverlay.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                if (request.isForMainFrame()) {
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this,
                            "Could not reach CodeSync server. Check your connection.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void openCodeSync() {
        String shareKey = shareKeyInput.getText().toString().trim();

        if (shareKey.isEmpty()) {
            showError("Please enter a share key.");
            shareKeyInput.requestFocus();
            return;
        }

        if (shareKey.contains("/") || shareKey.contains("\\")
                || shareKey.contains("?") || shareKey.contains("#")
                || shareKey.contains(" ")) {
            showError("Please enter only the share key.");
            return;
        }

        errorText.setVisibility(View.GONE);
        hideKeyboard();

        startScreen.setVisibility(View.GONE);
        webScreen.setVisibility(View.VISIBLE);
        loadingOverlay.setVisibility(View.VISIBLE);

        webView.loadUrl(CODE_SYNC_URL + shareKey);
    }

    private void pasteShareKey() {
        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null || !clipboard.hasPrimaryClip()) return;

        ClipData clip = clipboard.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) return;

        CharSequence text = clip.getItemAt(0).coerceToText(this);
        if (text != null) {
            shareKeyInput.setText(text.toString().trim());
            shareKeyInput.setSelection(shareKeyInput.length());
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(shareKeyInput.getWindowToken(), 0);
        }
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private void showStartScreen() {
        webView.stopLoading();
        loadingOverlay.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        webScreen.setVisibility(View.GONE);
        startScreen.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }
}
