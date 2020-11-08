package com.sakusaku.beacon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class BeaconListAdapter extends ArrayAdapter<BeaconListItems> {

    private final Integer mResource;
    private final List<BeaconListItems> mBeacons;
    private final LayoutInflater mInflater;

    /**
     * コンストラクタ
     * @param context コンテキスト
     * @param resource リソースID
     * @param beacons ListViewの要素
     */
    public BeaconListAdapter(Context context, int resource, List<BeaconListItems> beacons) {
        super(context, resource, beacons);

        mResource = resource;
        mBeacons = beacons;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(mResource, null);
        }

        //表示させるBeacon情報を取得
        BeaconListItems item = mBeacons.get(position);

        //この先で各要素を設定
        TextView uuid = view.findViewById(R.id.uuid);
        uuid.setText(item.getmUuid().toString());

        TextView major = view.findViewById(R.id.major);
        major.setText(item.getmMajor().toString());

        TextView minor = view.findViewById(R.id.minor);
        minor.setText(item.getmMinor().toString());

        TextView rssi = view.findViewById(R.id.rssi);
        rssi.setText(String.valueOf(item.getmRssi()));

        TextView txPower = view.findViewById(R.id.txPower);
        txPower.setText(String.valueOf(item.getmTxPower()));

        TextView distance = view.findViewById(R.id.distance);
        distance.setText(String.valueOf(item.getmDistance()));

        return view;
    }
}
