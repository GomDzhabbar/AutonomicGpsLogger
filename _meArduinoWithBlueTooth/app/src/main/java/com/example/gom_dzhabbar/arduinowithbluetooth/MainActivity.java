package com.example.gom_dzhabbar.arduinowithbluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.example.gom_dzhabbar.arduinowithbluetooth.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
    private static final String TAG = "bluetooth1";

    Button btnOn, btnOff, btReConnecting, downloadMaps;
    TextView txtArduino, lightText, temperText, hydrText;
    Handler h;

    private static final int REQUEST_ENABLE_BT = 1;
    final int RECIEVE_MESSAGE = 1;		// Статус для Handler
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder sb = new StringBuilder();
    private ConnectedThread mConnectedThread;

    // SPP UUID сервиса
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-адрес Bluetooth модуля
    private static String address = "98:D3:31:90:4C:AA";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        btnOn = (Button) findViewById(R.id.btnOn);					// кнопка включения
        btnOff = (Button) findViewById(R.id.btnOff);				// кнопка выключения
        txtArduino = (TextView) findViewById(R.id.txtArduino);		// для вывода текста, полученного от Arduino
        btReConnecting = (Button) findViewById(R.id.btReConnecting);
        lightText = (TextView) findViewById(R.id.lightText);
        temperText = (TextView) findViewById(R.id.temperText);
        hydrText = (TextView) findViewById(R.id.hydrText);
        downloadMaps = (Button) findViewById(R.id.downloadMaps);

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:													// если приняли сообщение в Handler
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);
                        sb.append(strIncom);

                        Log.d(TAG, "message    :" + sb);
                        Log.d(TAG, "messageEND-----------------------");
                        int endOfLineIndex = sb.indexOf("#");


                        if (endOfLineIndex > 0) {
                            Log.d(TAG, "string    :" + sb);
                            String sbprint = sb.substring(0, endOfLineIndex);				// то извлекаем строку
                            sb.delete(0, endOfLineIndex + 1);										// и очищаем sb
                            lightText.setText(sbprint);
                            Log.d(TAG, "setText - " + sbprint);
                            int valueL = Integer.parseInt(sbprint);
                            Log.d(TAG, "light    :" + sbprint);

                            Log.d(TAG, "string    :" + sb);
                            endOfLineIndex = sb.indexOf("*");
                            if(endOfLineIndex < 0)
                                Log.d(TAG, "Некорректная строка пришла с arduino:" + endOfLineIndex);
                            sbprint = sb.substring(0, endOfLineIndex);
                            hydrText.setText(sbprint);
                            sb.delete(0, endOfLineIndex+1);
                            Log.d(TAG, "hydr    :" + sbprint);

                            Log.d(TAG, "string    :" + sb);
                            endOfLineIndex = sb.indexOf("^");
                            sbprint = sb.substring(0, endOfLineIndex);
                            temperText.setText(sbprint);
                            sb.delete(0, sb.length());
                            Log.d(TAG, "temper    :" + sbprint);

                            btnOff.setEnabled(true);
                            btnOn.setEnabled(true);

                            if(valueL < 300)
                                mConnectedThread.write("1");
                            else
                                mConnectedThread.write("0");
                        }
                        break;
                }
            };
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();		// получаем локальный Bluetooth адаптер
        checkBTState();

        downloadMaps.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnOn.setOnClickListener(new OnClickListener() {        // определяем обработчик при нажатии на кнопку
            public void onClick(View v) {
                //btnOn.setEnabled(false);
                mConnectedThread.write("1");    // Отправляем через Bluetooth цифру 1
                //Toast.makeText(getBaseContext(), "Включаем LED", Toast.LENGTH_SHORT).show();
            }
        });

        btnOff.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //btnOff.setEnabled(false);
                mConnectedThread.write("0");    // Отправляем через Bluetooth цифру 0
                //Toast.makeText(getBaseContext(), "Выключаем LED", Toast.LENGTH_SHORT).show();
            }
        });

        btReConnecting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "...onResume - попытка соединения...");

                // Set up a pointer to the remote node using it's address.
                BluetoothDevice device = btAdapter.getRemoteDevice(address);

                // Two things are needed to make a connection:
                //   A MAC address, which we got above.
                //   A Service ID or UUID.  In this case we are using the
                //     UUID for SPP.
                try {
                    btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                } catch (IOException e) {
                    errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
                }

                // Discovery is resource intensive.  Make sure it isn't going on
                // when you attempt to connect and pass your message.
                btAdapter.cancelDiscovery();

                // Establish the connection.  This will block until it connects.
                Log.d(TAG, "...Соединяемся...");

                reConecting();
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - попытка соединения...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Соединяемся...");

        reConecting();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void reConecting(){
        try {
            btSocket.connect();
            Log.d(TAG, "...Соединение установлено и готово к передачи данных...");
        } catch (IOException e) {
            try {
                Log.d(TAG, "...Какая-то ошибка, закрытие сокета...."+ e.getMessage());
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        Log.d(TAG,"getState   :   "  + btAdapter.getState());

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Создание Socket...");

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth не поддерживается");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth включен...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    Log.d(TAG, "..while..");
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);		// Получаем кол-во байт и само собщение в байтовый массив "buffer"
                    Log.d(TAG, "..!!!!!..");
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();		// Отправляем в очередь сообщений Handler
                } catch (IOException e) {
                    Log.d(TAG, "..run().." + e.getMessage());
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            Log.d(TAG, "...Данные для отправки: " + message + "...");
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "...Ошибка отправки данных: " + e.getMessage() + "...");
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

}