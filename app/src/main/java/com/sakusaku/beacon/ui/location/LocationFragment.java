package com.sakusaku.beacon.ui.location;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sakusaku.beacon.BeaconService;
import com.sakusaku.beacon.R;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class LocationFragment extends Fragment {

    private static final String ServerIP = "10.0.2.2";
    private static final String ServerPORT = "8081";

    private myWsClientListener ws;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LocationViewModel locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);
        View root = inflater.inflate(R.layout.fragment_location, container, false);

        FloatingActionButton fab = root.findViewById(R.id.fab);
        FloatingActionButton fabPause = root.findViewById(R.id.fab_pauce);

        SharedPreferences pref = requireActivity().getSharedPreferences("preference_data", Context.MODE_PRIVATE);
        if (pref.getInt("isForeground", 0) == 1) {
            fabPause.setVisibility(View.VISIBLE);
            fab.setVisibility(View.GONE);
        }

        //サーバーの接続準備
        try {
            ws = new myWsClientListener(new URI("ws://" + ServerIP + ":" + ServerPORT + "/"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if(!ws.isOpen()) {
            ws.connect();
        }

        fab.setOnClickListener(v -> {
            // デバイスのBLE対応チェック
            if (!requireActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                // 未対応の場合、Toast表示
                Toast.makeText(getActivity(), "このデバイスはBLE未対応です", Toast.LENGTH_LONG).show();

                if (ws.isOpen()){
                    ws.send("hello");
                }
            } else {
                fabPause.setVisibility(View.VISIBLE);
                fab.setVisibility(View.GONE);

                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("isForeground", 1);
                editor.apply();

                requireActivity().startForegroundService(new Intent(getActivity(), BeaconService.class));
            }
        });
        fabPause.setOnClickListener(v -> {
            fab.setVisibility(View.VISIBLE);
            fabPause.setVisibility(View.GONE);

            SharedPreferences.Editor editor = pref.edit();
            editor.putInt("isForeground", 0);
            editor.apply();

            requireActivity().stopService(new Intent(getActivity(), BeaconService.class));
        });

        final TextView textView = root.findViewById(R.id.text_location);
        locationViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    //WS Lister
    private static class myWsClientListener extends WebSocketClient {
        public myWsClientListener(URI serverUri) {
            super(serverUri);
        }

        @Override
        //接続
        public void onOpen(ServerHandshake handshakedata) {
            Log.d("WebSocket", "Connected");
        }

        @Override
        //Serverからのメッセージの受信
        public void onMessage(final String message) {
            Log.d("WebSocket", message);
        }

        @Override
        //Serverの切断
        public void onClose(int code, String reason, boolean remote) {
            Log.d("WebSocket", "Disconnected");
        }

        @Override
        //エラー
        public void onError(Exception ex) {
            Log.d("WebSocket", "error");
        }
    }
}