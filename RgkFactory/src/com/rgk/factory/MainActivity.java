package com.rgk.factory;

/**
 * @author shiguang.du
 * @date 2014.06.17
 */
import java.util.Locale;

import com.rgk.factory.ControlCenter.ResultHandle;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.util.DisplayMetrics;
import android.util.Log;
import com.rgk.factory.NvRAMAgentHelper;

import android.os.SystemProperties; 
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
public class MainActivity extends Activity {
	public static String TAG = "CIT_TEST";
	Button buttAutoTest, buttItemTest, buttClean, buttResult,changeLanguage;
	public static boolean mAutoTest = false;
	private boolean isClickChangeLanguage;
	SharedPreferences sp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED);  //wangjun delete
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		new MyAsyncTask().execute();
		initLanguage();
		setTitle(R.string.app_name);
		setContentView(R.layout.activity_main);
		RelativeLayout mLayout = (RelativeLayout) findViewById(R.id.main_layout);
	    mLayout.setSystemUiVisibility(0x00002000);
		buttAutoTest = (Button) this.findViewById(R.id.auto_test);
		buttItemTest = (Button) this.findViewById(R.id.item_test);
		buttClean = (Button) this.findViewById(R.id.Clean);
		buttResult = (Button) this.findViewById(R.id.TestResults);
		//changeLanguage = (Button)this.findViewById(R.id.ChangeLanguage);
		setLanguageButtonVisible();
	}

	public void autotest(View v) {
		mAutoTest = true;
		StartToAutoTest();
	}

	public void itemtest(View v) {
		mAutoTest = false;
		Intent mIntent = new Intent(this, ItemTestActivity.class);
		startActivity(mIntent);
	}

	public void clean(View v) {
		if (ResultHandle.DeleteResultFromSystem()) {
			Toast.makeText(this, R.string.msg_clean_success, Toast.LENGTH_LONG)
					.show();
		} else {
			Toast.makeText(this, R.string.msg_clean_fail, Toast.LENGTH_LONG)
					.show();
		}
		// sendBroadcast(new Intent(Config.ItemOnClick));
	}

	private void StartToAutoTest() {
		sendBroadcast(new Intent(Config.ACTION_START_AUTO_TEST).putExtra(
				"test_item", TAG));
	}

	public void testResults(View v) {
		Intent mIntent = new Intent(this, CitTestResult.class);
		startActivity(mIntent);
	}

	public void softversion(View v) {
		Intent mIntent = new Intent(this, SoftwareVersion.class);
		startActivity(mIntent);
	}
	public void setLanguageButtonVisible() {
		String[] locales = getResources().getAssets().getLocales();
		for(String locale : locales	) {
			Log.i(TAG, "setLanguageButtonVisible:Language="+locale);
			if(locale.equals("zh_CN"))
				return;
		}
		//changeLanguage.setVisibility(View.GONE);
	}
	public void initLanguage() {
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		isClickChangeLanguage = sp.getBoolean("isClickChangeLanguage", false);
		Log.i(TAG, "initLanguage:isClickChangeLanguage="+isClickChangeLanguage);
		if(isClickChangeLanguage) {
			SharedPreferences.Editor editor = sp.edit();
			editor.putBoolean("isClickChangeLanguage", false);
			editor.commit();
			return;
		}
		Resources res = getResources();
		Configuration config = res.getConfiguration();
		Log.i(TAG, "initLanguage:"+config.locale+" defaultEnglish="+Util.defaultEnglish);
		if(getResources().getBoolean(R.bool.defaultEnglish) || KeyCodeReceiver.customOrder) {
			KeyCodeReceiver.customOrder = false;
			config.locale = Locale.ENGLISH;
		} else {
			config.locale = Locale.SIMPLIFIED_CHINESE;
		}		
		DisplayMetrics dm = res.getDisplayMetrics();
		res.updateConfiguration(config, dm);
	}
	public void changeLanguage(View v) {
		Resources res = getResources();
		Configuration config = res.getConfiguration();
		Log.i(TAG, "changeLanguage:"+config.locale);
		if(!config.locale.equals(Locale.SIMPLIFIED_CHINESE)) {
			config.locale = Locale.SIMPLIFIED_CHINESE;
		} else {
			config.locale = Locale.ENGLISH;
		}		
		DisplayMetrics dm = res.getDisplayMetrics();
		res.updateConfiguration(config, dm);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean("isClickChangeLanguage", true);
		editor.commit();
		super.recreate();
	}

	public void resetPhone(View v) {
		showDialog();			    
	}

	@Override
	public void onDestroy() {
		mAutoTest = false;
		super.onDestroy();
	}
	
	private void writeVernoToNV() {
		String internalVersion = SystemProperties.get("ro.internal.version","");
		String externalVersion = SystemProperties.get("ro.build.display.id","");
		NvRAMAgentHelper.writeVernoToNV(64*3+40,internalVersion);
		NvRAMAgentHelper.writeVernoToNV(64*6+40,externalVersion);
	}
	
    private void showDialog() {
        new AlertDialog.Builder(this)
        .setTitle(R.string.reset_phone_title)
        .setMessage(R.string.reset_phone_message)
        .setPositiveButton(R.string.reset_phone_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
            	sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
            }
        })
        .setNegativeButton(R.string.reset_phone_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
            }
        }).show();
    }
    
    class MyAsyncTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			Log.i(TAG, "doInBackground()");
			new Util(MainActivity.this).initValue();
			writeVernoToNV();
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			Log.i(TAG, "onPostExecute():result="+result);
			super.onPostExecute(result);
		}
    	
    }
}
