package ca.uwaterloo.io.measure;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CMS50EW {

    private UsbManager manager;
    private PendingIntent permissionIntent;
    private UsbSerialPort port;

    boolean isReading = false;

    public int spo2 = 0;
    public int bpm = 0;
    public boolean fingerOut = false;
    public int data = 0;

    private String cmd_hello1 = "7d81a7808080808080";
    private String cmd_hello2 = "7d81a2808080808080";
    private String cmd_hello3 = "7d81a0808080808080";
    private String cmd_get_live_data = "7d81a1808080808080";

    private String START = "7D81A1";
    private String STOP = "7D81A2";

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            //call method to set up device communication
                        }
                    } else {
                        Log.d("ABC", "permission denied for device " + device);
                    }
                }
            }
        }
    };

    public CMS50EW(Context context) {
        manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(usbReceiver, filter);
    }

    public boolean connect() {
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<String> it = deviceList.keySet().iterator();
        String deviceName = it.next();
        UsbDevice device = deviceList.get(deviceName);
        manager.requestPermission(device, permissionIntent);

        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return false;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            return false;
        }

        port = driver.getPorts().get(0); // Most devices have just one port (port 0)

        if (port == null) {
            return false;
        }

        try {
            port.open(connection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (Exception e) {
            Log.e("Error", e.toString());
            return false;
        }

        return true;
    }

    public void exit() {
        try {
            if (port != null) {
                port.close();
            }
        } catch (Exception e) {
            Log.e("Error: ", e.toString());
        } finally {
            port = null;
        }
    }

    public void start() {
//        initializeDevice();
//        sendCmd(cmd_get_live_data);
        sendCmd(START);
        if (port != null) {
            mSerialIoManager = new SerialInputOutputManager(port, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    public void stop() {
        if (mSerialIoManager != null) {
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
        isReading = false;
    }

    private void sendCmd(String cmdStr) {
        try {
            byte[] cmd = hexStringToByteArray(cmdStr);
            port.write(cmd, 100);
        } catch (Exception e) {
            Log.e("Error", e.toString());
        }
    }


    private void initializeDevice() {
        sendCmd(cmd_hello1);
        recv();
        sendCmd(cmd_hello2);
        sendCmd(cmd_hello3);
        recv();
    }

    private void recv() {
        while (true) {
            byte[] data = new byte[1];
            try {
                int len = port.read(data, 100);
                if (len == 0) break;
            } catch (Exception e) {
                Log.e("Error", e.toString());
            }
        }

    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private SerialInputOutputManager mSerialIoManager;

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d("Error", "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] rawData) {
                    if (rawData.length > 6) {

                        String byte3 = String.format("%8s", Integer.toBinaryString(rawData[3] & 0x7F)).replace(' ', '0');
                        data = Integer.parseInt(byte3, 2);

                        String byte5 = String.format("%8s", Integer.toBinaryString(rawData[5] & 0x7F)).replace(' ', '0');
                        bpm = Integer.parseInt(byte5, 2);

                        String byte6 = String.format("%8s", Integer.toBinaryString(rawData[6] & 0x7F)).replace(' ', '0');
                        spo2 = Integer.parseInt(byte6, 2);
                        try {
                            sendCmd(STOP);
                            port.purgeHwBuffers(true, true);
                            sendCmd(START);
                        } catch (Exception e){}
                    }
                }
            };
}