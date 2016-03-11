package com.tamer.burshlessmotor.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.Toast;

import com.tamer.burshlessmotor.R;
import com.tamer.burshlessmotor.base.BaseActivity;
import com.tamer.burshlessmotor.bluetooth.BluetoothService;
import com.tamer.burshlessmotor.bluetooth.BluetoothState;
import com.tamer.burshlessmotor.bluetooth.DeviceListActivity;
import com.tamer.burshlessmotor.fragment.RealtimeUpdates;

public class HomeActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "HomeActivity";

    // Intent request codes
    private static final int RECEIVE_DATA = 1;
    private static final int SEND_DATA = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Send Type
    private static final byte SET_SPEED_MODE = 0x00;
    private static final byte SET_PWM_MODE = 0x01;
    private static final byte SET_START = 0x10;
    private static final byte SET_STOP = 0x11;
    private static final byte RECEIVED_SPEED = 0x00;
    private static final byte SET_SPEED = 0x01;
    private static final byte TYPE_CMD = 0x00;
    private static final byte TYPE_DATA = 0x01;

    // Layout Views

    private boolean isServiceRunning = false;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothService mService = null;

    RealtimeUpdates realtimeUpdates;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        realtimeUpdates = (RealtimeUpdates) getFragmentManager().findFragmentById(R.id.realtime_fragment);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(HomeActivity.this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            HomeActivity.this.finish();
        }
    }


    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.fab:
                if (mService.getState() == BluetoothService.STATE_CONNECTED) {
                    disconnect();
                } else {
                    intent = new Intent(getApplicationContext(), DeviceListActivity.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
                break;
            default:
        }
    }

    private class OnSeekBarChangeListenerImp implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setup() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mService == null) {
            setup();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            mService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mService.start();
            }
        }
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setup() {
        // Initialize the BluetoothService to perform bluetooth connections
        mService = new BluetoothService(HomeActivity.this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Makes this device discoverable.
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        FragmentActivity activity = this;
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        if (null == HomeActivity.this) {
            return;
        }
        final ActionBar actionBar = HomeActivity.this.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    public void disconnect() {
        if (mService != null) {
            isServiceRunning = false;
            mService.stop();
            if (mService.getState() == BluetoothState.STATE_NONE) {
                isServiceRunning = true;
                mService.start();
            }
        }
    }

    /**
     * The Handler that gets information back from the BluetoothService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothState.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorConnected)));
                            fab.setImageResource(R.drawable.ic_cab_done_holo_dark);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorConnecting)));
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                            fab.setImageResource(android.R.drawable.stat_sys_data_bluetooth);
                            break;
                    }
                    break;
                case BluetoothState.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case BluetoothState.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    handleFrameData(readBuf);
                    break;
                case BluetoothState.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(BluetoothState.DEVICE_NAME);
                    if (null != HomeActivity.this) {
                        Toast.makeText(HomeActivity.this, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case BluetoothState.MESSAGE_TOAST:
                    if (null != HomeActivity.this) {
                        Toast.makeText(HomeActivity.this, msg.getData().getString(BluetoothState.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BluetoothState.REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;

            case BluetoothState.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setup();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(HomeActivity.this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    HomeActivity.this.finish();
                }
        }
    }

    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mService.connect(device, secure);
    }

    /**
     * Handle messages read from brushless motor.
     * A5 xxxx data AA
     * |  |     |   |
     * st |   data end
     * |--00     cmd
     * |  |
     * |  |--00  set speed mode
     * |  |--01  set pwm mode
     * |  |--10  set start
     * |  |--11  set stop
     * |
     * |--01     data
     * |  |
     * |  |--00  receive speed
     * |  |--01  set speed
     *
     * @param message messages read from brushless motor.
     */

    private void handleFrameData(byte[] message) {
        byte[] tempMessage = new byte[6];
        System.arraycopy(message, 0, tempMessage, 0, 6);
            /* 如果针头不对，直接返回，放弃这次数据 */
        if ((tempMessage[0] == (byte)0xA5) && (tempMessage[5] == (byte)0xAA)) {


            switch (tempMessage[1]) {
                case 0x00:
//              commend message
                    switch (tempMessage[2]) {
                        case 0x00:
//                      speed mode
                            break;
                        case 0x01:
//                      pwm mode
                            break;
                        case 0x10:
//                      start motor
                            break;
                        case 0x11:
//                      stop motor
                            break;
                        default:
                    }
                    break;
                case 0x01:
//              data message
                    switch (tempMessage[2]) {
                        case 0x00:
//                      received speed
                            int tempSpeed = ((tempMessage[3] & 0xFF)<< 8) | (tempMessage[4] & 0xFF) ;
                            realtimeUpdates.setSpeedData(tempSpeed);
                            break;
                        case 0x01:
//                      set speed
                            break;
                        default:
                    }
                    break;
                default:
            }
        }

    }
    /**
     * Establish connection with other divice
     *
     * @param data data of speed.
     * @param dataOrCmd choose CMD or DATA mode
     * @param mode mode of send
     */
    private void sendFrameData(int data, byte dataOrCmd, byte mode) {
        // Check that we're actually connected before trying anything
        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(HomeActivity.this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] tempMessage = new byte[]{
                (byte) 0xA5, 0x00, 0x00, 0x00, 0x00, (byte) 0xAA
        };
        tempMessage[1] = dataOrCmd;
        tempMessage[2] = mode;
        tempMessage[3] = (byte) ((data >> 8) & 0xFF);
        tempMessage[4] = (byte) (data & 0xFF);
        // Get the message bytes and tell the BluetoothChatService to write
        mService.write(tempMessage);

        // Reset out string buffer to zero and clear the edit text field
        mOutStringBuffer.setLength(0);
    }

}
