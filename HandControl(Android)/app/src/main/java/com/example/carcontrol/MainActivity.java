// Araba kontrol(wifi) uygulaması Android uygulaması java kodları
// 24.03.2015


package com.example.carcontrol;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.R.string;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends Activity implements SensorEventListener {

	private TextView AccText;
	private TextView ModeText;
	private Button ControlButton;
	private Button ForwardButton;
	private Button RearButton;
	private Button RightButton;
	private Button LeftButton;
	private TextView IPTextView;
	private TextView PortTextView;

	private boolean ControlMode = BUTTON_CONTROL;
	private boolean SendEnable = true;
	private String IP = "10.100.105.83";
	private int Port = 3333;

	// sabit tanımlamaları
	private static final boolean BUTTON_CONTROL = false;
	private static final boolean ACC_CONTROL = true;
	private static final byte FORWARD = 0x01;
	private static final byte REAR 	= 0x02;
	private static final byte LEFT = 0x04;
	private static final byte RIGHT = 0x08;

	// Uygulamada kullanılan değişkenler
	byte[] CarControlData = new byte[1];	// kontrol verisini tutan değişken
	DatagramSocket clientSocket;			// Soket nesnesi
	InetAddress IPAddress;					// Ip bilgisini tutan nesne
	Timer timer;							// Zamanlayıcı
	TimerTask timerTask;					// Zamanlayıcı görevi
	final Handler handler = new Handler();	// Handler nesnesi tanımlaması



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		SensorManager sensorYoneticisi = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		Sensor yonSensoru = sensorYoneticisi.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		// Arayüzde ki nesneler uygulama kodu ile eşelştiriliyor.
		AccText = (TextView)findViewById(R.id.textViewAcc);
		ModeText = (TextView)findViewById(R.id.textViewControl);
		ControlButton = (Button) findViewById(R.id.buttonControl);
		ForwardButton = (Button) findViewById(R.id.buttonForward);
		RearButton = (Button) findViewById(R.id.buttonRear);
		RightButton = (Button) findViewById(R.id.buttonRight);
		LeftButton = (Button) findViewById(R.id.buttonLeft);
		IPTextView = (TextView)findViewById(R.id.editTextIP);
		PortTextView = (TextView)findViewById(R.id.editTextPort);

		if(!sensorYoneticisi.registerListener(this, yonSensoru, SensorManager.SENSOR_DELAY_NORMAL))
			AccText.setText("Sensor çalışmıyor..");

		// zamanlayıcı ayarları. Her 20 msde bir zamanlayıcı çalışır..
		timer = new Timer();
		initializeTimerTask();
		timer.schedule(timerTask, 1500, 20);

		getActionBar().setTitle("Hand Control: Disconnected");

		// Control butonuna basınca kontrol durumunu değiştir.
		ControlButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CarControlData[0] = 0;
				if(ControlMode == ACC_CONTROL)
				{
					ControlMode = BUTTON_CONTROL;
					ModeText.setText("Button Control");
				}
				else
				{
					ControlMode = ACC_CONTROL;
					ModeText.setText("Acc Control");
				}

			}
		});
		///////////////////////////////////////////////////////////

		// İleri butonuna basınca ileri bilgisini gönderilecek veriye ekle
		ForwardButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(ControlMode == BUTTON_CONTROL)
				{
					if(event.getAction() == MotionEvent.ACTION_DOWN) {
						CarControlData[0] |= FORWARD;
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						CarControlData[0] &= ~FORWARD;
					}
				}
				return false;
			}
		});
		//////////////////////////////////////////////////////////////////

		// Geri  butonuna basınca geri bilgisini gönderilecek veriye ekle
		RearButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(ControlMode == BUTTON_CONTROL)
				{
					if(event.getAction() == MotionEvent.ACTION_DOWN) {
						CarControlData[0] |= REAR;
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						CarControlData[0] &= ~REAR;
					}
				}
				return false;
			}
		});
		//////////////////////////////////////////////////////////////////

		// Sağ butonuna basınca sağ bilgisini gönderilecek veriye ekle
		RightButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(ControlMode == BUTTON_CONTROL)
				{
					if(event.getAction() == MotionEvent.ACTION_DOWN) {
						CarControlData[0] |= RIGHT;
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						CarControlData[0] &= ~RIGHT;
					}
				}
				return false;
			}
		});
		////////////////////////////////////////////////////////////////////

		// Sol butonuna basınca sol bilgisini gönderilecek veriye ekle
		LeftButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(ControlMode == BUTTON_CONTROL)
				{
					if(event.getAction() == MotionEvent.ACTION_DOWN) {
						CarControlData[0] |= LEFT;
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						CarControlData[0] &= ~LEFT;
					}
				}
				return false;
			}
		});
		// İleri butonuna basınca ileri bilgisini gönderilecek veriye ekle

		// Ip bilgisini kullanıcıdan alan kısım
		IPTextView.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				try {
					SendEnable = false;
					IP = IPTextView.getText().toString();
					SendEnable = true;
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "IP Change Error", Toast.LENGTH_SHORT).show();
				}

			}
		});

		// Port bilgsini kullanıcıdan alan kısım
		PortTextView.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {

				try {
					SendEnable = false;
					Port = Integer.valueOf(PortTextView.getText().toString());
					SendEnable = true;
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "Port Change Error", Toast.LENGTH_SHORT).show();
				}


			}
		});

	}

	// ivme sensörünü okuyup sensör değerine göre kontrol sağalayn kısım
	public void onSensorChanged(SensorEvent event) {
		// Arc tanjant fonksiyonu ile ivme bilgisinden dönme dereceleri tespit edilir.
		double Angle1,Angle2;		// açı değerlerini tutan değişkenler
		double temp1 = Math.sqrt(Math.pow(event.values[0], 2)+Math.pow(event.values[1], 2));
		Angle1 = Math.atan2(event.values[1], event.values[0])*180/Math.PI;
		Angle2 = Math.atan2(event.values[2],temp1)*180/Math.PI;

		// Acc kontrol modu aktif ise araba ivme sensörü ile kontrol edilir.
		if(ControlMode == ACC_CONTROL)
		{
			// Açı1 değerine göre arabaya sağ-sol bilgisini gönder
			if(Angle1<-20 && Angle1 > -50)
			{
				CarControlData[0] |= LEFT;
				CarControlData[0] &= ~RIGHT;
			}
			else if(Angle1<50 && Angle1 > 20)
			{
				CarControlData[0] &= ~LEFT;
				CarControlData[0] |=  RIGHT;
			}
			else
			{
				CarControlData[0] &= ~LEFT;
				CarControlData[0] &= ~RIGHT;
			}

			// Açı2 değerine göre arabaya ileri-geri bilgisini gönder
			if(Angle2<-10 && Angle2 > -50)
			{
				CarControlData[0] |= REAR;
				CarControlData[0] &= ~FORWARD;
			}
			else if(Angle2<70 && Angle2 > 30)
			{
				CarControlData[0] &= ~REAR;
				CarControlData[0] |=  FORWARD;
			}
			else
			{
				CarControlData[0] &= ~REAR;
				CarControlData[0] &= ~FORWARD;
			}
		}
		AccText.setText("Angle1: " + (int)(Angle1) + " Angle2: " + (int)(Angle2) +
				"\nX : " + (int)(event.values[0]*100) + " Y : " + (int)(event.values[1]*100) + " Z : " + (int)(event.values[2]*100));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// Her 20msde bir zamanlayıcı fonksiyonu çalıştırılıp
	// kontrol değerleri arabaya gönderilir.
	public void initializeTimerTask() {

		timerTask = new TimerTask() {
			public void run() {

				//use a handler to run a toast that shows the current timestamp
				handler.post(new Runnable() {
					public void run() {
						if(SendEnable)
							SendCarData();
					}
				});
			}
		};
	}


	@Override
	protected void onDestroy(){
		super.onStop();
		try {

			clientSocket.close();
			//Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// UDP üzerinden 1 byte kontrol verisini gönderen fonksiyon
	public void SendCarData(){
		try {
			clientSocket = new DatagramSocket();
			IPAddress = InetAddress.getByName(IP);
			byte[] TempData = new byte[1];
			TempData[0] = (byte) (CarControlData[0] | 0x40);
			DatagramPacket sendPacket = new DatagramPacket(TempData, TempData.length, IPAddress, Port);
			clientSocket.send(sendPacket);
			clientSocket.close();
			getActionBar().setTitle("Hand Control: Connected");
		} catch (Exception e) {
			getActionBar().setTitle("Hand Control: Disconnected");
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}


}


