package com.wistron.gerry.bleconnect;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class androidbleconnect extends UnityPlayerActivity{

    Context mContext = null;

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBLEdevice;
    private BluetoothGatt mGatt;
    private BluetoothLeScanner bluetoothLeScanner;
    final static private UUID mHeartRateServiceUuid = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    final static private UUID NORDIC_UART_SERVICE = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    final static private UUID RX_CHARACTERISTIC = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    final static private UUID TX_CHARACTERISTIC = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    final static private UUID mOx = UUID.fromString("00001822-0000-1000-8000-00805f9b34fb");

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
    }

    public void blesttart3(){
        UnityPlayer.UnitySendMessage("Main","message","ConnectAndroid");

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
           // Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        // 初始化蓝牙适配器
        final BluetoothManager bluetoothManager;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }
        // 确保蓝牙在设备上可以开启
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        if (!mBluetoothAdapter.isEnabled()){
            final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
            System.out.println("mBluetoothAdapter is not enable");
            return;
        }
        ScanFunction(true);
    }

    public static int blestart(){
        UnityPlayer.UnitySendMessage("Main","message","Connect");
        //StartActivity0("start");
        return 1;
    }
    public static int blestart2(){
        return 0;
    }


    @Override
    protected void onResume() {
        super.onResume();
        //System.out.println("Callback");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_ENABLE_BT){
            if (resultCode == Activity.RESULT_CANCELED){
                finish();
            }
            else {

            }
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    public static int[] createDataOfWatchSetAutoResult() {
        int[] data = {
                0xf6,
                1,  // 0: off; 1: on
                0
        };
        data[2] = (data[0] + data[1]) & 0xff;
        System.out.println("write data");

        return data;
    }

    private void ScanFunction(boolean enable){
        if (enable){
            System.out.println("Scan Functon enable");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                System.out.println("Go LOLLIPOP");
                bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                bluetoothLeScanner.startScan(scanCallback);
            }
        }
    }

    private void Sandiness(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            System.out.println("Go LOLLIPOP");
            //bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            bluetoothLeScanner.stopScan(scanCallback);
            mGatt = mBLEdevice.connectGatt(this,true,mGattCallback);

        }

    }

    private void onHandleData(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        BluetoothDevice device = gatt.getDevice();
        UUID uuid = characteristic.getUuid();

        if (TX_CHARACTERISTIC.equals(uuid)) {
            //System.out.println(characteristic.getValue().toString());
            extractUartDataMap(characteristic);
            /*
            System.out.println("%%SpO2", dataMap.get("%%SpO2"));
            System.out.println("bpm", dataMap.get("bpm"));
            System.out.println("signalQuality", dataMap.get("signalQuality"));
            System.out.println("motion", dataMap.get("motion"));
            */
        }

    }

    public static void extractUartDataMap(@NonNull BluetoothGattCharacteristic characteristic) {
        byte[] rawData = characteristic.getValue();
        String str = new String(rawData, StandardCharsets.UTF_8);
        StringBuffer buffer = new StringBuffer("0x");
        int i;
        for (byte b : rawData) {
            i = b & 0xff;
            buffer.append(Integer.toHexString(i));
        }
        System.out.println("read data:" + buffer.toString());

        // SpO2 & bpm
        Integer spo2PR = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
        Integer bpm = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 2);
        Integer signalQuality = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 3); // 0~255
        Integer motion = (rawData[4] >> 3) & 1;  // motion bit: 0x08. 0: idle; 1: moving

        String log = String.format(Locale.getDefault(), "%3d, %3d, %3d, %d", spo2PR, bpm, signalQuality, motion);
        Log.d("UART_test", log);
        UnityPlayer.UnitySendMessage("Main","message",log);

    }

    private void initCharacteristicWriting(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
        UUID uuid = characteristic.getUuid();

        if (RX_CHARACTERISTIC.equals(uuid)) {
            //initSetTimeWriting(gatt, characteristic);
            initUartReading(gatt, characteristic);
        }
    }

    private void initUartReading(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
        int[] data = createDataOfWatchSetAutoResult();

        byte[] dataBytes = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            dataBytes[i] = (byte) data[i];
        }
        characteristic.setValue(dataBytes);
        //addBusyAction(gatt, GattBusyAction.WRITE_CHARACTERISTIC, characteristic);
        gatt.writeCharacteristic(characteristic);
    }

    private void initCharacteristicReading(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
        final int charaProp = characteristic.getProperties();
        System.out.println("txCharacteristic = "+ charaProp);
        enableNotifications(characteristic);
        if ((charaProp & characteristic.PROPERTY_READ) > 0) {
            //addBusyAction(gatt, GattBusyAction.READ_CHARACTERISTIC, characteristic);
            System.out.println("readCharacteristic");
            gatt.readCharacteristic(characteristic);

        }
        if ((charaProp & characteristic.PROPERTY_NOTIFY) > 0) {
            boolean isEnableNotification = gatt.setCharacteristicNotification(characteristic, true);
            if(isEnableNotification) {
                List<BluetoothGattDescriptor> descriptorList = characteristic.getDescriptors();
                System.out.println("descriptorList = " + descriptorList.toString());
                if(descriptorList != null && descriptorList.size() > 0) {
                    for(BluetoothGattDescriptor descriptor : descriptorList) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                        System.out.println("descriptor = "+ descriptor.toString());

                    }
                }
            }
        }

    }

    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    protected final boolean enableNotifications(final BluetoothGattCharacteristic characteristic) {
        final BluetoothGatt gatt = mGatt;
        if (gatt == null || characteristic == null)
            return false;

        // Check characteristic property
        final int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0)
            return false;

        Log.d("BLE", "gatt.setCharacteristicNotification(" + characteristic.getUuid() + ", true)");
        gatt.setCharacteristicNotification(characteristic, true);
        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            Log.v("BLE", "Enabling notifications for " + characteristic.getUuid());
            Log.d("BLE", "gatt.writeDescriptor(" + CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID + ", value=0x01-00)");
            return gatt.writeDescriptor(descriptor);
        }
        return false;
    }


    private void initDataReading(@NonNull BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(NORDIC_UART_SERVICE);
        if (service == null){
            System.out.println("没有得到心率服务");
        }
        else {
            System.out.println("RX");
            BluetoothGattCharacteristic rxCharacteristic = service.getCharacteristic(RX_CHARACTERISTIC);
            if (rxCharacteristic != null) {
                //initCharacteristicWriting(gatt, rxCharacteristic);
                System.out.println("RX_CHARACTERISTIC found");
                //System.out.println("TX_CHARACTERISTIC found success");
                initCharacteristicWriting(gatt,rxCharacteristic);

            }
            BluetoothGattCharacteristic txCharacteristic = service.getCharacteristic(TX_CHARACTERISTIC);
            if (txCharacteristic != null) {
                System.out.println("TX");
                //initCharacteristicWriting(gatt, rxCharacteristic);
                System.out.println("TX_CHARACTERISTIC found");
                //System.out.println("TX_CHARACTERISTIC found success");
                initCharacteristicReading(gatt,txCharacteristic);

            }

        }
    }



    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            System.out.println("Connection State Changed:" + (newState == BluetoothProfile.STATE_CONNECTED ? "Connected" : "Disconnected"));
            //System.out.println("BluetoothGatt中中中"+ newState);
            if (BluetoothGatt.STATE_CONNECTED == newState) {
                System.out.println("on Connect");
                gatt.discoverServices();//必须有，可以让onServicesDiscovered显示所有Services
                //tx_display.append("连接成功");
                //Toast.makeText(mContext, "连接成功", Toast.LENGTH_SHORT).show();
            }else if (BluetoothGatt.STATE_DISCONNECTED == newState){
                //System.out.println("断开 中中中");
                //Toast.makeText(mContext, "断开连接", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            //onHandleData(gatt, characteristic);
            System.out.println("Data change");
            onHandleData(gatt,characteristic);

        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //发现服务，在蓝牙连接的时候会调用

            if (status == BluetoothGatt.GATT_SUCCESS){
                initDataReading(gatt);
            }

            /*
            List<BluetoothGattService> list = gatt.getServices();
            //System.out.println(list);

            for (BluetoothGattService bluetoothGattService:list){
                String str = bluetoothGattService.getUuid().toString();
                System.out.println("GATT Service " + str);
                List<BluetoothGattCharacteristic> gattCharacteristics = bluetoothGattService
                        .getCharacteristics();
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    System.out.println("gatt Characteristic" + gattCharacteristic.getUuid());
                }
            }
            */
            //EnableNotification(true,gatt,alertLevel);//必须要有，否则接收不到数据
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            //Log.e("onCharacteristicRead中", "数据接收了哦"+bytesToHexString(characteristic.getValue()));
            System.out.println("Accept data");
        }
    };


    private ScanCallback scanCallback=new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            System.out.println("Result");
            byte[] scanData= new byte[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                scanData = result.getScanRecord().getBytes();
                System.out.println("ResultscanData" + scanData.toString());
                System.out.println("onScanResult :"+result.getScanRecord().getDeviceName());
                if (result.getScanRecord().getDeviceName() !=null && result.getScanRecord().getDeviceName().equals("oCare100_MBT")){
                    mBLEdevice = result.getDevice();
                    Sandiness();
                }
                //mBLEdevice = result.getDevice();
                //mBluetoothAdapter.notifyDataSetChanged();

            }
            //把byte数组转成16进制字符串，方便查看
            //Log.e("TAG","onScanResult :"+CYUtils.Bytes2HexString(scanData));
        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            System.out.println("onScanResultList :"+results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

}
