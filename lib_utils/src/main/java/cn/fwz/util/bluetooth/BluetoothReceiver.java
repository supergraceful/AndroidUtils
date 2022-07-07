package cn.fwz.util.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class BluetoothReceiver extends BroadcastReceiver {

    public static final int OFF = 0;
    public static final int ON = 1;
    public static final int BOND = 1;
    public static final int UNBOND = 0;
    public static final int ERROR = -1;


    private BluetoothUtil.OnStateClick onStateClick;
    private BluetoothUtil.OnSearchClick onSearchClick;
    private BluetoothUtil.OnBondClick onBondClick;

    public void setOnStateClick(BluetoothUtil.OnStateClick onStateClick){
        this.onStateClick=onStateClick;
    }
    public void setOnSearchClick(BluetoothUtil.OnSearchClick onSearchClick){
        this.onSearchClick=onSearchClick;
    }
    public void setonBondClick(BluetoothUtil.OnBondClick onBondClick){
        this.onBondClick=onBondClick;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (TextUtils.equals(action, BluetoothAdapter.ACTION_STATE_CHANGED)) {
            //蓝牙开关监听
            //获取
            if (onStateClick!=null){
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -9090);
                if (state==BluetoothAdapter.STATE_OFF||state==BluetoothAdapter.STATE_ON){
                    onStateClick.getBluetoothState(state,intent);
                }
            }
        }else if (TextUtils.equals(action, BluetoothDevice.ACTION_FOUND)){
            //搜索监听
            if (onSearchClick!=null){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String address = device.getAddress();
                if (!TextUtils.isEmpty(address)){
                    onSearchClick.next(device,intent);
                }
            }
        }else if (TextUtils.equals(action, BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
            //监听配对
            if (onBondClick!=null){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState()==BluetoothDevice.BOND_NONE||device.getBondState()==BluetoothDevice.BOND_BONDED){
                    onBondClick.getBluetoothBond(device.getBondState(), intent);
                }
            }
        }
    }


}
