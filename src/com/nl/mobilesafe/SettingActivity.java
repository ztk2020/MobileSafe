package com.nl.mobilesafe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nl.mobilesafe.service.AddressService;
import com.nl.mobilesafe.service.CallSmsSafeService;
import com.nl.mobilesafe.service.WatchDogService;
import com.nl.mobilesafe.ui.SettingClickView;
import com.nl.mobilesafe.ui.SettingItemView;
import com.nl.mobilesafe.utils.MD5Utils;
import com.nl.mobilesafe.utils.ServiceStatusUtils;

public class SettingActivity extends Activity {
	private SettingItemView siv_update;
	private SharedPreferences sp;
	private SettingItemView siv_show_address;
	private Intent showAddressIntent;

	private SettingClickView scv_changebg;

	private SettingItemView siv_callsms_safe;
	private Intent callSmsSafeIntent;

	// 看门狗设置
	private SettingItemView siv_watchdog;
	private Intent watchDogIntent;
	private AlertDialog dialog;
	private EditText et_setup_pwd;
	private EditText et_setup_confirm;
	@Override
	protected void onResume() {//可见，重新获取焦点时，回显，从小房子任务栈那里快速启动
		super.onResume();
		boolean isRunning = ServiceStatusUtils.isServiceRunning(this,
				"com.nl.mobilesafe.service.AddressService");
		siv_show_address.setChecked(isRunning);
		boolean Running = ServiceStatusUtils.isServiceRunning(this,
				"com.nl.mobilesafe.service.CallSmsSafeService");
		siv_callsms_safe.setChecked(Running);
		// 检验一个服务是否还运行的工具类比较好
		boolean dogRunning = ServiceStatusUtils.isServiceRunning(this,
				"com.nl.mobilesafe.service.WatchDogService");
		siv_watchdog.setChecked(dogRunning);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		/**
		 * 设置更新
		 */
		siv_update = (SettingItemView) findViewById(R.id.siv_update);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		boolean flag = sp.getBoolean("update", false);
		if (flag) {
			// 自动升级已经开启
			siv_update.setChecked(true);
			// siv_update.setText("自动更新已经开启");
		} else {
			// 自动升级已经关闭
			siv_update.setChecked(false);
			// siv_update.setText("自动更新已经关闭");
		}
		siv_update.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Editor edit = sp.edit();
				// 已经打开自动升级
				if (siv_update.isChecked()) {
					siv_update.setChecked(false);
					// siv_update.setText("自动更新已经关闭");
					edit.putBoolean("update", false);
				} else {
					// 没有打开自动升级
					siv_update.setChecked(true);
					// siv_update.setText("自动更新已经开启");
					edit.putBoolean("update", true);
				}
				edit.commit();
			}
		});

		/**
		 * 设置显示地址
		 */
		siv_show_address = (SettingItemView) findViewById(R.id.siv_show_address);
		// sharedPreference方法有弊端，如果在后台杀掉服务，show_address还为true，仍会重新开启服务
		// 用户体验不好
		// boolean show_address = sp.getBoolean("show_address", false);
		// siv_show_address.setChecked(show_address);
		// if(address){
		// //自动升级已经开启
		// siv_show_address.setChecked(true);
		// // siv_update.setText("自动更新已经开启");
		// }else{
		// //自动升级已经关闭
		// siv_show_address.setChecked(false);
		// // siv_update.setText("自动更新已经关闭");
		// }

		// 检验一个服务是否还运行的工具类比较好
		boolean isRunning = ServiceStatusUtils.isServiceRunning(this,
				"com.nl.mobilesafe.service.AddressService");
		siv_show_address.setChecked(isRunning);
		showAddressIntent = new Intent(this, AddressService.class);
		siv_show_address.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Editor edit = sp.edit();
				// 已经打开自动升级
				if (siv_show_address.isChecked()) {
					siv_show_address.setChecked(false);
					// siv_update.setText("自动更新已经关闭");
					// edit.putBoolean("show_address", false);
					stopService(showAddressIntent);
				} else {
					// 没有打开自动升级
					siv_show_address.setChecked(true);
					// siv_update.setText("自动更新已经开启");
					// edit.putBoolean("show_address", true);
					startService(showAddressIntent);
				}
				// edit.commit();
			}
		});

		/**
		 * 设置归属地提示框风格
		 */
		scv_changebg = (SettingClickView) findViewById(R.id.scv_changebg);
		final String[] items = { "半透明", "活力橙", "卫士蓝", "金属灰", "苹果绿" };
		int which = sp.getInt("which", 0);
		scv_changebg.setDesc(items[which]);
		scv_changebg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				AlertDialog.Builder builder = new Builder(SettingActivity.this);
				builder.setTitle("归属地边框风格");
				// 还要再得到一次which，开对话框前一次，开对话框后又要一次，不然默认为0
				int which = sp.getInt("which", 0);
				builder.setSingleChoiceItems(items, which,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								Editor edit = sp.edit();
								edit.putInt("which", which);
								scv_changebg.setDesc(items[which]);
								edit.commit();

								dialog.dismiss();
							}
						});
				builder.setNegativeButton("cancel", null);
				builder.show();
			}
		});

		/**
		 * 设置黑名单拦截
		 */
		siv_callsms_safe = (SettingItemView) findViewById(R.id.siv_callsms_safe);

		// 检验一个服务是否还运行的工具类比较好
		boolean Running = ServiceStatusUtils.isServiceRunning(this,
				"com.nl.mobilesafe.service.CallSmsSafeService");
		siv_callsms_safe.setChecked(Running);
		callSmsSafeIntent = new Intent(this, CallSmsSafeService.class);
		siv_callsms_safe.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (siv_callsms_safe.isChecked()) {
					siv_callsms_safe.setChecked(false);
					stopService(callSmsSafeIntent);
				} else {
					siv_callsms_safe.setChecked(true);
					startService(callSmsSafeIntent);
				}

			}
		});

		/**
		 * 设置程序锁
		 */
		siv_watchdog = (SettingItemView) findViewById(R.id.siv_watchdog);

		// 检验一个服务是否还运行的工具类比较好
		boolean dogRunning = ServiceStatusUtils.isServiceRunning(this,
				"com.nl.mobilesafe.service.WatchDogService");
		siv_watchdog.setChecked(dogRunning);
		watchDogIntent = new Intent(this, WatchDogService.class);
		siv_watchdog.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (siv_watchdog.isChecked()) {
					siv_watchdog.setChecked(false);
					stopService(watchDogIntent);
				} else {
					siv_watchdog.setChecked(true);
					startService(watchDogIntent);
					showSetupPwdDialog();
				}

			}
		});

	}
	/**
	 * 设置密码对话框
	 */
	private void showSetupPwdDialog() {
		AlertDialog.Builder builder = new Builder(SettingActivity.this);
		// 自定义一个布局文件
		View view = View.inflate(SettingActivity.this,
				R.layout.dialog_setup_password, null);
		// builder.setView(view);
		// builder.show();
		// 以下三行代码是为了适配低版本的模拟器，为了美观，一个小细节
		dialog = builder.create();
		dialog.setView(view, 0, 0, 0, 0);
		dialog.show();

		et_setup_pwd = (EditText) view.findViewById(R.id.et_setup_pwd);
		et_setup_confirm = (EditText) view.findViewById(R.id.et_setup_confirm);
		Button ok = (Button) view.findViewById(R.id.ok);
		Button cancel = (Button) view.findViewById(R.id.cancel);

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 把对话框消掉
				dialog.dismiss();
			}
		});
		ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 取出密码
				String password = et_setup_pwd.getText().toString().trim();
				String password_confirm = et_setup_confirm.getText().toString()
						.trim();
				if (TextUtils.isEmpty(password)
						|| TextUtils.isEmpty(password_confirm)) {
					Toast.makeText(SettingActivity.this, "哥们，密码为空", 0).show();
					return;
				}
				// 判断是否一致，才去保存
				if (password.equals(password_confirm)) {
					// 一致的话
					/**
					 * 保存密码 消掉对话框 进入手机防盗界面
					 */
					Editor edit = sp.edit();
					edit.putString("apkpwd", MD5Utils.md5Password(password));
					edit.commit();

					dialog.dismiss();
					
				} else {
					Toast.makeText(SettingActivity.this, "哥们，密码不一致", 0).show();
					return;
				}
			}
		});
	}

}
