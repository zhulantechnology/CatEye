package com.rgk.factory.versioninfo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.rgk.factory.Config;
import com.rgk.factory.NvRAMAgentHelper;
import com.rgk.factory.R;
import com.mediatek.telephony.TelephonyManagerEx;
import com.rgk.factory.ControlCenter.ResultHandle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
public class MotherboardInfo extends Activity implements OnClickListener {
	public static final String TAG = "MotherboardInfo";
	TextView chip,board,imei1,imei2,psn,/*blutooth_address,*/wifi_address,memory,lcd,camera,rf_calibrate,gps_calibrate;
	Button motherboard_pass,motherboard_fail;
	private boolean hadSendBroadcast = false;

        //wangjun add begin
	private static String JTEK_BOARD_NAME = "DS05";
	//wangjun add end
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setTitle(R.string.board_info);
		setContentView(R.layout.motherboard_info);
		RelativeLayout mLayout = (RelativeLayout) findViewById(R.id.motherboard_layout);
	    mLayout.setSystemUiVisibility(0x00002000);
		initTextView();
		setText();
		super.onCreate(savedInstanceState);
	}
	@Override
	public void onClick(View v) {
		if(v.equals(motherboard_pass)) {
			SendBroadcast(Config.PASS);
		} else if(v.equals(motherboard_fail)){
			SendBroadcast(Config.FAIL);
		}
		
	}
	private void initTextView(){
		chip = (TextView)findViewById(R.id.chip);
		board = (TextView)findViewById(R.id.board);
		imei1 = (TextView)findViewById(R.id.imei1);
		imei2 = (TextView)findViewById(R.id.imei2);
		psn = (TextView)findViewById(R.id.psn);
		//blutooth_address = (TextView)findViewById(R.id.blutooth_address);
		wifi_address = (TextView)findViewById(R.id.wifi_address);
		memory = (TextView)findViewById(R.id.memory);
		lcd = (TextView)findViewById(R.id.lcd);
		camera = (TextView)findViewById(R.id.camera);
		rf_calibrate = (TextView)findViewById(R.id.rf_calibrate);
		gps_calibrate = (TextView)findViewById(R.id.gps_calibrate);
		
		motherboard_pass = (Button)findViewById(R.id.motherboard_pass);
		motherboard_pass.setOnClickListener(this);
		motherboard_fail = (Button)findViewById(R.id.motherboard_fail);
		motherboard_fail.setOnClickListener(this);
	}
	private void setText(){
		chip.setText(getChip());
//wangjun modify begin
		//board.setText(getBoard());
		board.setText(JTEK_BOARD_NAME);
//wangjun modify end
		imei1.setText(getImei1());
		imei2.setText(getImei2());
		psn.setText(getPsn());
		//blutooth_address.setText(getBlutooth_address());
		wifi_address.setText(getWifi_address());
		memory.setText(getMemory());
		lcd.setText(getLcd());
		camera.setText(getCamera());
		//rf_calibrate.setText(getRFCalibrate());
		//gps_calibrate.setText(getGPSCalibrate());
	}
	public void SendBroadcast(String result){
		if (!hadSendBroadcast) {
			hadSendBroadcast = true;
			ResultHandle.SaveResultToSystem(result, TAG);
			sendBroadcast(new Intent(Config.ItemOnClick));
			sendBroadcast(new Intent(Config.ACTION_START_AUTO_TEST).putExtra("test_item", TAG));
			finish();
		}
	}	
	public String getChip() {
		String result = "";
		try {
			FileInputStream fis = new FileInputStream("/proc/cpuinfo");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr, 4096);
            String ch = br.readLine();
            String[] strArr = null;
            while(ch != null) {
            	Log.i(TAG,"getChip()->ch="+ch);
            	if(ch.contains("Hardware")) {
            		strArr = ch.split(":");
            		result = strArr[1].trim();
            		break;
            	} else {
            		ch = br.readLine();
            	}
            }
            fis.close();
        } catch (Exception e) {
        	e.printStackTrace();
        }
		return result;
	}
	public String getBoard() {
		return SystemProperties.get("ro.product.board", "unknow");
	}
	public String getImei1() {
		TelephonyManagerEx tm = new TelephonyManagerEx(this);
		return tm.getDeviceId(0);
	}
	public String getImei2() {
		TelephonyManagerEx tm = new TelephonyManagerEx(this);
		return tm.getDeviceId(1);
	}
	public String getPsn() {
		return Build.SERIAL;
	}
	public String getBlutooth_address() {
		return BluetoothAdapter.getDefaultAdapter().getAddress();
	}
	public String getWifi_address() {
		WifiManager mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		if(!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
		WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
		String mac = mWifiInfo.getMacAddress();
		Log.i(TAG, "getWifi_address()->wifimac="+mac);
		if(mac == null || mac.trim() == "") {
			mac = NvRAMAgentHelper.readNVWifiMac().toString();
			Log.i(TAG, "getWifi_address()->wifimac2="+mac);
		}
		return mac;
	}
	public String getMemory() {
		String result = "";
		try {
			FileInputStream fis = new FileInputStream("/proc/rgk_memInfo");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr, 4096);
            String ch = br.readLine();
            if(ch != null){
            	String[] memory = ch.split(" ");
            	result = memory[0].trim();
            }
            fis.close();
        } catch (Exception e) {
        	e.printStackTrace();
        }
		return result;
	}
	public String getLcd() {
		String result = "";
		try {
            FileInputStream fis = new FileInputStream("/proc/rgk_lcdInfo");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr, 4096);
            String ch = null;
            while ((ch = br.readLine()) != null) {
            	result = ch;
            }
            fis.close();
        } catch (Exception e) {
        	e.printStackTrace();
        }
		return result;
	}
	public StringBuilder getCamera() {
		StringBuilder result = new StringBuilder();
		try {
			FileInputStream mFileInputStream = new FileInputStream("/proc/rgk_cameraInfo");
			InputStreamReader mInputStreamReader = new InputStreamReader(mFileInputStream);
			BufferedReader mBufferedReader = new BufferedReader(mInputStreamReader);			
			String cameraString = null;
			if((cameraString = mBufferedReader.readLine()) != null) {
				String[] cameraCh = cameraString.split(",");
				result.append(cameraCh[0]).append("\n");
				result.append(cameraCh[1]);
		    } else {
		    	result.append("unknow");
		    }
			mFileInputStream.close();
		} catch (Exception e) {
			result.append("No cameraInfo file found");
		}
		return result;
	}
	
	public StringBuilder getRFCalibrate() {
		StringBuilder result = new StringBuilder();
		result.append(getResources().getString(R.string.rf_calibrate));
		String serial = getPsn();
		if (serial != null && !serial.equals("")) {
			if(serial.trim().toUpperCase().endsWith(" 10P")) {
				result.append(getResources().getString(R.string.has_calibrate));
			} else {
				result.append(getResources().getString(R.string.no_calibrate));
			}
		}
		return result;
	}
	
	public StringBuilder getGPSCalibrate() {
		StringBuilder result = new StringBuilder();
		result.append(getResources().getString(R.string.gps_calibrate));
		int C0 = NvRAMAgentHelper.readGpsCalibrateNVData(49);//ap_nvram_gps_config_struct.C0
		int C1 = NvRAMAgentHelper.readGpsCalibrateNVData(51);//ap_nvram_gps_config_struct.C1
		if(C0 != 0 && C1 != 0) {
			result.append(getResources().getString(R.string.has_calibrate));
		} else {
			result.append(getResources().getString(R.string.no_calibrate));
		}
		return result;
	}
}
