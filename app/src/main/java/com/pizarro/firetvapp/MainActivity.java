package com.pizarro.firetvapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {

    private WebView myWebView;
    private ValueCallback<Uri[]> uploadMessage;
    private final static int FILECHOOSER_RESULTCODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        myWebView = new WebView(this);
        setContentView(myWebView);

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        
        // Habilitar multimedia y archivos
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false); // Autoplay permitido
        
        // Persistencia y visualización
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("vlc://")) {
                    String cleanUrl = url.replace("vlc://", "");
                    
                    // --- LÓGICA XTREAM ---
                    // Si el link no tiene extensión, suele ser Xtream Codes. Le añadimos .ts para mayor compatibilidad
                    if (!cleanUrl.contains(".") || cleanUrl.endsWith("/")) {
                        cleanUrl = cleanUrl + ".ts";
                    }

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(cleanUrl), "video/*");
                    
                    // Intentamos forzar VLC, pero sin fallar si no existe
                    try {
                        intent.setPackage("org.videolan.vlc");
                        startActivity(intent);
                    } catch (Exception e) {
                        // Si no hay VLC, abrimos el selector de Android universal
                        Intent chooser = Intent.createChooser(new Intent(Intent.ACTION_VIEW)
                                .setDataAndType(Uri.parse(cleanUrl), "video/*"), "Reproducir con...");
                        startActivity(chooser);
                    }
                    return true;
                }
                return false;
            }
        });

        myWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (uploadMessage != null) uploadMessage.onReceiveValue(null);
                uploadMessage = filePathCallback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(Intent.createChooser(intent, "Selecciona tu lista M3U"), FILECHOOSER_RESULTCODE);
                return true;
            }
        });

        myWebView.loadUrl("file:///android_asset/index.html"); 
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (uploadMessage == null) return;
            Uri result = (intent == null || resultCode != RESULT_OK) ? null : intent.getData();
            uploadMessage.onReceiveValue(result != null ? new Uri[]{result} : null);
            uploadMessage = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
