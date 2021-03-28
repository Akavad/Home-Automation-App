package com.example.dell.homeautomation2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    TextView tvbluetooth_status;
    TextView txvResult,tvbulb_status,tvfan_status;

    String address = null , name=null;

    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    Set<BluetoothDevice> pairedDevices;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    ImageView imgbtnSpeak;

    Switch switchbulb,switchfan;

    Button btnstatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvbluetooth_status=(TextView)findViewById(R.id.tvbluetooth_status);
        txvResult= (TextView) findViewById(R.id.txvResult);
        tvbulb_status= (TextView) findViewById(R.id.tvbulb_status);
        tvfan_status= (TextView) findViewById(R.id.tvfan_status);
        switchbulb= (Switch) findViewById(R.id.switchbulb);
        switchfan= (Switch) findViewById(R.id.switchfan);
        btnstatus= (Button) findViewById(R.id.btnstatus);
        try {
            bluetooth_connect_device();
        } catch (IOException e) {
            e.printStackTrace();
        }
        imgbtnSpeak= (ImageView) findViewById(R.id.imgbtnSpeak);
        imgbtnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getVoiceInput();
            }
        });
        switchbulb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (name != null){
                    if (b){
                        lightFanOnOff("A");
                        tvbulb_status.setBackgroundColor(Color.rgb(0,100,0));
                        tvbulb_status.setText("LIGHT IS ON");
                    }else{
                        lightFanOnOff("a");
                        tvbulb_status.setBackgroundColor(Color.RED);
                        tvbulb_status.setText("LIGHT IS OFF");
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"bluetooth device is not connected properly", Toast.LENGTH_LONG).show();
                }
            }
        });
        switchfan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (name!=null){
                    if (b){
                        lightFanOnOff("B");
                        tvfan_status.setBackgroundColor(Color.rgb(0,100,0));
                        tvfan_status.setText("FAN IS ON");
                    }else{
                        lightFanOnOff("b");
                        tvfan_status.setBackgroundColor(Color.RED);
                        tvfan_status.setText("FAN IS OFF");
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"bluetooth device is not connected properly", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnstatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(getApplicationContext(),StatusActivity.class);
                startActivity(i);
            }
        });
    }

    private void bluetooth_connect_device() throws IOException
    {
        try
        {
            myBluetooth = BluetoothAdapter.getDefaultAdapter();
            address = myBluetooth.getAddress();
            pairedDevices = myBluetooth.getBondedDevices();
            if (pairedDevices.size()>0)
            {
                for(BluetoothDevice bt : pairedDevices)
                {
                    address=bt.getAddress().toString();name = bt.getName().toString();
                    Toast.makeText(getApplicationContext(),"Connected", Toast.LENGTH_LONG).show();

                }
            }

        }
        catch(Exception we){}
        myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
        BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
        btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
        btSocket.connect();
        try { tvbluetooth_status.setText("BT Name: "+name+"\nBT Address: "+address); }
        catch(Exception e){}
    }

    public void getVoiceInput(){
        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());//default language english

        startActivityForResult(intent,1);
    }

    private void lightFanOnOff(String i){
        try{
            if (btSocket != null)
            {
                btSocket.getOutputStream().write(i.toString().getBytes());
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),"Exception: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                if(resultCode==RESULT_OK && data!=null){
                    ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txvResult.setText(result.get(0));
                    String input = txvResult.getText().toString();
                    if (btSocket!=null&&name!=null){
                        if (input.equals("turn on light")) {
                            lightFanOnOff("A");
                            tvbulb_status.setBackgroundColor(Color.rgb(0,100,0));
                            tvbulb_status.setText("LIGHT IS ON");
                            switchbulb.setChecked(true);
                        }
                        if (input.equals("turn off light")) {
                            lightFanOnOff("a");
                            tvbulb_status.setBackgroundColor(Color.RED);
                            tvbulb_status.setText("LIGHT IS OFF");
                            switchbulb.setChecked(false);
                        }
                        if (input.equals("turn on fan")||input.equals("turn on light one")) {
                            lightFanOnOff("B");
                            tvfan_status.setBackgroundColor(Color.rgb(0,100,0));
                            tvfan_status.setText("FAN IS ON");
                            switchfan.setChecked(true);
                        }
                        if (input.equals("turn off fan")||input.equals("turn off light one")) {
                            lightFanOnOff("b");
                            tvfan_status.setBackgroundColor(Color.RED);
                            tvfan_status.setText("FAN IS OFF");
                            switchfan.setChecked(false);
                        }
                    }else {
                        Toast.makeText(getApplicationContext(),"bluetooth device is not connected properly", Toast.LENGTH_LONG).show();
                    }

                }
        }
    }
}
