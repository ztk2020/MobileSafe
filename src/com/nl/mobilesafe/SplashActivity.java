package com.nl.mobilesafe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import com.nl.mobilesafe.utils.StreamTools;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;
/**
 *=============================================================
 * 
 * 作者 ：祝天康
 * 
 * 版本 ：1.0
 * 
 * 创建日期 ：  2015-3-10 上午10：30：00
 * 
 * 描述 ：
 * 
 * 全局接口公共部分 修改历史 ：
 * 
 *=============================================================
 */
public class SplashActivity extends Activity {
	protected static final String TAG = "SplashActivity";
	protected static final int ENTER_HOME = 0;
	protected static final int SHOW_UPDATE_DIALOG = 1;
	protected static final int URL_ERROR = 2;
	protected static final int NETWORK_ERROR = 3;
	protected static final int JSON_ERROR = 4;
	private TextView tv_splash_version;
	private String description;
	/**
	 * 新APK下载地址
	 */
	private String apkurl;
	//升级进度显示
	private TextView tv_update_info;
	
	private SharedPreferences sp;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case ENTER_HOME: // 进入主界面，
				enterHome();
				break;
			case SHOW_UPDATE_DIALOG: // 显示升级对话框
				Log.i(TAG, "显示升级对话框");
				showUpdateDialog();
				break;
			case URL_ERROR: // URL错误
				enterHome();
				Toast.makeText(getApplicationContext(), "URL错误", 0).show();
				break;
			case NETWORK_ERROR: // 网络异常
				enterHome();
				Toast.makeText(getApplicationContext(), "网络异常", 0).show();
				break;
			case JSON_ERROR: // JSON解析异常
				enterHome();
				Toast.makeText(SplashActivity.this, "JSON解析异常", 0).show();
				break;
			default:
				break;
			}
		}

	};
	/**
	 * 从Assets目录拷贝到data/data/com.itheima.moblesafe/files/address.db数据库的代码；
	 */
	private void copyDB(String dbname){
		
		File file = new File(getFilesDir(),dbname);
		try {
			
			if(file.exists()&&file.length()>0){
				
				Log.i(TAG, "数据库文件只需要拷贝一下，如果拷贝了，不需要重新拷贝了");
			}else{
				
				InputStream is = getAssets().open(dbname);
				
				FileOutputStream fos = new FileOutputStream(file);
				
				byte[] buffer = new byte[1024];
				int len = -1;
				while((len=is.read(buffer))!=-1){
					fos.write(buffer, 0, len);
				}
				fos.close();
				is.close();
			}
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		//拷贝数据库
		copyDB("address.db");
		copyDB("antivirus.db");
		tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
		tv_splash_version.setText("版本: " + getVersionName());
		tv_update_info = (TextView) findViewById(R.id.tv_update_info);
		AlphaAnimation aa = new AlphaAnimation(0.2f, 1.f);
		aa.setDuration(1000);
		findViewById(R.id.rl_root_splash).setAnimation(aa);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		boolean flag = sp.getBoolean("update", false);
		if(flag){
			// 检查升级
			checkUpdate();
		}else{
			//自动升级关闭
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					//进入主页面
					enterHome();
				}
			}, 2000);
		}
	}

	/**
	 * 弹出升级对话框
	 */
	protected void showUpdateDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("提示升级");
//		builder.setCancelable(false);//强制升级，出重大bug时才用，否则用户体验不好
		//用下面该方法代替上面一句
		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				//进入主页面
				enterHome();
				dialog.dismiss();
			}
		});
		
		builder.setMessage(description);
		builder.setPositiveButton("立即升级", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//下载APK，替换安装
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
					FinalHttp finals = new FinalHttp();
					//target为String，Environment.getExternalStorageDirectory()返回File
					//只有.getAbsolutePath()才会变为String
					finals.download(apkurl,
							Environment.getExternalStorageDirectory().getAbsolutePath() + "/mobilesafe2.0.apk" , 
							new AjaxCallBack<File>() {

								@Override
								public void onFailure(Throwable t, int errorNo,
										String strMsg) {
									t.printStackTrace();
									Log.i(TAG, "下载失败");
									super.onFailure(t, errorNo, strMsg);
								}

								@Override
								public void onLoading(long count, long current) {
									super.onLoading(count, current);
									//升级才显示，升级的描述信息
									tv_update_info.setVisibility(View.VISIBLE);
									long progress = current * 100 / count;
									tv_update_info.setText("升级进度："+ progress +"%");
								}

								@Override
								public void onSuccess(File t) {
//									
									super.onSuccess(t);
									installAPK(t);
								}

								private void installAPK(File t) {
									//<intent-filter>
//					                <action android:name="android.intent.action.VIEW" />
//					                <category android:name="android.intent.category.DEFAULT" />
//					                <data android:scheme="content" />
//					                <data android:scheme="file" />
//					                <data android:mimeType="application/vnd.android.package-archive" />
//					            </intent-filter>
									Intent intent = new Intent();
									intent.setAction("android.intent.action.VIEW");
									intent.addCategory("android.intent.category.DEFAULT");
									intent.setDataAndType(Uri.fromFile(t), "application/vnd.android.package-archive");
									startActivity(intent);
									//t.delete();
								}
						
					});
				}else{
					Toast.makeText(getApplicationContext(), "没有sdcard，请安装再试", 1).show();
					return;
				}
			}
		});
		builder.setNegativeButton("下次再说", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				//关闭对话框，进入主界面
				dialog.dismiss();
				enterHome();
			}
		});
		builder.show();
	}

	protected void enterHome() {
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		// 一定要关闭当前界面
		finish();
	}

	/**
	 * 检查是否有新版本，如果有就升级
	 */
	private void checkUpdate() {
		new Thread() {
			public void run() {
				// URL:http://10.0.2.2:8080/update.html
				Message msg = Message.obtain();
				long startTime = System.currentTimeMillis();
				try {
					URL url = new URL(getString(R.string.serverurl));
					// 联网
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(4000);
					int code = conn.getResponseCode();
					if (code == 200) {
						// 联网成功
						InputStream is = conn.getInputStream();
						// 从流中获取String
						String result = StreamTools.readFromStream(is);
						Log.i(TAG, "联网成功：" + result);

						// 将String变为JSON对象
						JSONObject json = new JSONObject(result);
						String version = json.getString("version");
						description = json.getString("description");
						apkurl = json.getString("apkurl");

						if (getVersionName().equals(version)) {
							// 不需升级，进入主界面
							msg.what = ENTER_HOME;
						} else {
							// 需要升级，弹出升级对话框
							msg.what = SHOW_UPDATE_DIALOG;
						}

					}
				} catch (MalformedURLException e) {
					msg.what = URL_ERROR;
					e.printStackTrace();
				} catch (IOException e) {
					msg.what = NETWORK_ERROR;
					e.printStackTrace();
				} catch (JSONException e) {
					msg.what = JSON_ERROR;
					e.printStackTrace();
				} finally {
					long endTime = System.currentTimeMillis();
					long dTime = endTime - startTime;
					if (dTime < 2000) {
						SystemClock.sleep(2000 - dTime);
					}
					handler.sendMessage(msg);
				}
			};
		}.start();
	}

	/**
	 * 得到应用程序的版本名称
	 * 
	 * @return
	 */
	private String getVersionName() {
		// 可以用来管理手机的APK，安装的，没有安装的
		PackageManager pm = getPackageManager();
		try {
			// 得到APK的功能清单文件
			PackageInfo info = pm.getPackageInfo(getPackageName(), 0);// flag为常量一般赋值给0就可以
			return info.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}

	}

}
