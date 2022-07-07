package cn.fwz.util.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressLint("StaticFieldLeak")
public class BluetoothUtil {

    private  BluetoothReceiver bluetoothReceiver;
    private final Context context;
    private BluetoothAdapter bluetoothAdapter;
    private CountDownTimer timer;
    private static volatile BluetoothUtil instance;
    public static BluetoothUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (BluetoothUtil.class) {
                if (instance == null) {
                    instance = new BluetoothUtil(context);
                }
            }
        }
        return instance;
    }

    private BluetoothUtil(Context context) {
        this.context = context;
        bluetoothReceiver = new BluetoothReceiver();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //启动广播监听
        context.registerReceiver(bluetoothReceiver, intentFilter);
    }

    /**
     * 获取蓝牙开关状态
     */
    public boolean isEnabled(){
        return bluetoothAdapter.isEnabled();
    }

    /**
     * 监听蓝牙开关状态
     * @param onStateClick 监听回调
     */
    public void setOnStateClick(OnStateClick onStateClick){
        bluetoothReceiver.setOnStateClick(onStateClick);
    }

    /**
     * 开启蓝牙
     * */
    public boolean openBluetooth() {
        if (bluetoothAdapter == null) {
            return false;
        }
        if (bluetoothAdapter.isEnabled()) {
            return true;
        }
        return bluetoothAdapter.enable();
    }

    /**
     * 关闭蓝牙
     * */
    public boolean closeBluetooth() {
        if (bluetoothAdapter == null) {
            return false;
        }
        if (!bluetoothAdapter.isEnabled()) {
            return true;
        }
        return bluetoothAdapter.disable();
    }

    /**
     * 定时搜索蓝牙搜索
     * @param time          搜索时间 单位（S）
     * @param onSearchClick 搜索回调
     */
    public void timerSearch(long time, OnSearchClick onSearchClick) {
        if (bluetoothAdapter == null) {
            onSearchClick.onError("当前设备不支持蓝牙");
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            onSearchClick.onError("蓝牙未开启");
        }
        if (!gpsState()) {
            onSearchClick.onError("请开启GPS");
        }

        if (time>0) {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            timer = new CountDownTimer(time * 1000, 1000) {
                @Override
                public void onTick(long l) {
                }

                @Override
                public void onFinish() {
                    bluetoothAdapter.cancelDiscovery();
                }
            }.start();
        }else if (time<0){
            onSearchClick.onError("搜索时间不能小于0");
        }
        bluetoothReceiver.setOnSearchClick(onSearchClick);
        bluetoothAdapter.startDiscovery();
    }

    /**
     * 连续搜索
     */
    public void startSearch(OnSearchClick onSearchClick){
        timerSearch(0,onSearchClick);
    }

    /**
     * 停止搜索
     */
    public boolean stopSearch(){
        if (bluetoothAdapter == null) {
            return false;
        }
        return bluetoothAdapter.cancelDiscovery();
    }

    /**
     * 获取本机蓝牙信息
     */
    public String  getSelfMac(){
        if (bluetoothAdapter == null) {
            return "当前设备不支持蓝牙";
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return bluetoothAdapter.getAddress();
        }else{
            try {
                Field field = BluetoothAdapter.class.getDeclaredField("mService");
                field.setAccessible(true);
                Object bluetoothManagerService = field.get(bluetoothAdapter);
                Method method = bluetoothManagerService.getClass().getMethod("getAddress");
                Object address = method.invoke(bluetoothManagerService);
                if (address!=null){
                    return address.toString();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return "获取本机蓝牙失败";
    }

    /**
     * 蓝牙配对
     * @param blue 配对蓝牙对象
     * @param onBondClick 配对监听
     */
    public void connectDevice(BluetoothDevice blue,OnBondClick onBondClick){

        if (bluetoothAdapter == null) {
            onBondClick.getError("当前设备不支持蓝牙");
        }
        if (!bluetoothAdapter.isEnabled()) {
            onBondClick.getError("蓝牙未开启");
        }
        bluetoothReceiver.setonBondClick(onBondClick);
        blue.createBond();
    }

    /**
     * 取消配对
     * @param blue 配对蓝牙对象
     * @param onBondClick 配对监听
     */
    public void disConnectDevice(BluetoothDevice blue,OnBondClick onBondClick){
        if (bluetoothAdapter == null) {
            onBondClick.getError("当前设备不支持蓝牙");
        }
        if (!bluetoothAdapter.isEnabled()) {
            onBondClick.getError("蓝牙未开启");
        }
        bluetoothReceiver.setonBondClick(onBondClick);
        try{
            Method method = BluetoothDevice.class.getMethod("removeBond");
            method.invoke(blue);
        }catch (Exception e){
            e.printStackTrace();
            onBondClick.getError("取消匹配失败");
        }
    }

    /**
     * 获取配对列表
     */
    public Set<BluetoothDevice> getBondedDevices(){
        return bluetoothAdapter.getBondedDevices();
    }

    public void clear() {
        context.unregisterReceiver(bluetoothReceiver);
        bluetoothAdapter.cancelDiscovery();
        timer.cancel();

        timer = null;
        bluetoothReceiver=null;
        bluetoothAdapter=null;
        instance=null;
    }

    //6.0以上蓝牙搜索时需要开启GPS
    public boolean gpsState() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            return true;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providersList = locationManager.getProviders(true);
        return providersList.contains(LocationManager.GPS_PROVIDER);
    }

    private Boolean bluetoothEquals(String blueInfo,BluetoothDevice blu){
        if (blueInfo.contains(":")&&blueInfo.length()==17){
            return blu.getAddress().equals(blueInfo);
        }else{
            return blu.getName().equals(blueInfo);
        }
    }

    public interface OnStateClick {
        void getBluetoothState(int state, Intent intent);
    }

    public interface OnBondClick {
        void getBluetoothBond(int bond, Intent intent);
        void getError(String msg);
    }

    public interface OnSearchClick {
        void next(BluetoothDevice device, Intent intent);
        void onError(String msg);
    }
}
