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
 * ���� ��ף�쿵
 * 
 * �汾 ��1.0
 * 
 * �������� ��  2015-3-10 ����10��30��00
 * 
 * ���� ��
 * 
 * ȫ�ֽӿڹ������� �޸���ʷ ��
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
	 * ��APK���ص�ַ
	 */
	private String apkurl;
	//����������ʾ
	private TextView tv_update_info;
	
	private SharedPreferences sp;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case ENTER_HOME: // ���������棬
				enterHome();
				break;
			case SHOW_UPDATE_DIALOG: // ��ʾ�����Ի���
				Log.i(TAG, "��ʾ�����Ի���");
				showUpdateDialog();
				break;
			case URL_ERROR: // URL����
				enterHome();
				Toast.makeText(getApplicationContext(), "URL����", 0).show();
				break;
			case NETWORK_ERROR: // �����쳣
				enterHome();
				Toast.makeText(getApplicationContext(), "�����쳣", 0).show();
				break;
			case JSON_ERROR: // JSON�����쳣
				enterHome();
				Toast.makeText(SplashActivity.this, "JSON�����쳣", 0).show();
				break;
			default:
				break;
			}
		}

	};
	/**
	 * ��AssetsĿ¼������data/data/com.itheima.moblesafe/files/address.db���ݿ�Ĵ��룻
	 */
	private void copyDB(String dbname){
		
		File file = new File(getFilesDir(),dbname);
		try {
			
			if(file.exists()&&file.length()>0){
				
				Log.i(TAG, "���ݿ��ļ�ֻ��Ҫ����һ�£���������ˣ�����Ҫ���¿�����");
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
		//�������ݿ�
		copyDB("address.db");
		copyDB("antivirus.db");
		tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
		tv_splash_version.setText("�汾: " + getVersionName());
		tv_update_info = (TextView) findViewById(R.id.tv_update_info);
		AlphaAnimation aa = new AlphaAnimation(0.2f, 1.f);
		aa.setDuration(1000);
		findViewById(R.id.rl_root_splash).setAnimation(aa);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		boolean flag = sp.getBoolean("update", false);
		if(flag){
			// �������
			checkUpdate();
		}else{
			//�Զ������ر�
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					//������ҳ��
					enterHome();
				}
			}, 2000);
		}
	}

	/**
	 * ���������Ի���
	 */
	protected void showUpdateDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("��ʾ����");
//		builder.setCancelable(false);//ǿ�����������ش�bugʱ���ã������û����鲻��
		//������÷�����������һ��
		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				//������ҳ��
				enterHome();
				dialog.dismiss();
			}
		});
		
		builder.setMessage(description);
		builder.setPositiveButton("��������", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//����APK���滻��װ
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
					FinalHttp finals = new FinalHttp();
					//targetΪString��Environment.getExternalStorageDirectory()����File
					//ֻ��.getAbsolutePath()�Ż��ΪString
					finals.download(apkurl,
							Environment.getExternalStorageDirectory().getAbsolutePath() + "/mobilesafe2.0.apk" , 
							new AjaxCallBack<File>() {

								@Override
								public void onFailure(Throwable t, int errorNo,
										String strMsg) {
									t.printStackTrace();
									Log.i(TAG, "����ʧ��");
									super.onFailure(t, errorNo, strMsg);
								}

								@Override
								public void onLoading(long count, long current) {
									super.onLoading(count, current);
									//��������ʾ��������������Ϣ
									tv_update_info.setVisibility(View.VISIBLE);
									long progress = current * 100 / count;
									tv_update_info.setText("�������ȣ�"+ progress +"%");
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
					Toast.makeText(getApplicationContext(), "û��sdcard���밲װ����", 1).show();
					return;
				}
			}
		});
		builder.setNegativeButton("�´���˵", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				//�رնԻ��򣬽���������
				dialog.dismiss();
				enterHome();
			}
		});
		builder.show();
	}

	protected void enterHome() {
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		// һ��Ҫ�رյ�ǰ����
		finish();
	}

	/**
	 * ����Ƿ����°汾������о�����
	 */
	private void checkUpdate() {
		new Thread() {
			public void run() {
				// URL:http://10.0.2.2:8080/update.html
				Message msg = Message.obtain();
				long startTime = System.currentTimeMillis();
				try {
					URL url = new URL(getString(R.string.serverurl));
					// ����
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(4000);
					int code = conn.getResponseCode();
					if (code == 200) {
						// �����ɹ�
						InputStream is = conn.getInputStream();
						// �����л�ȡString
						String result = StreamTools.readFromStream(is);
						Log.i(TAG, "�����ɹ���" + result);

						// ��String��ΪJSON����
						JSONObject json = new JSONObject(result);
						String version = json.getString("version");
						description = json.getString("description");
						apkurl = json.getString("apkurl");

						if (getVersionName().equals(version)) {
							// ��������������������
							msg.what = ENTER_HOME;
						} else {
							// ��Ҫ���������������Ի���
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
	 * �õ�Ӧ�ó���İ汾����
	 * 
	 * @return
	 */
	private String getVersionName() {
		// �������������ֻ���APK����װ�ģ�û�а�װ��
		PackageManager pm = getPackageManager();
		try {
			// �õ�APK�Ĺ����嵥�ļ�
			PackageInfo info = pm.getPackageInfo(getPackageName(), 0);// flagΪ����һ�㸳ֵ��0�Ϳ���
			return info.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}

	}

}