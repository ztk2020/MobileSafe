package com.nl.mobilesafe;

import com.nl.mobilesafe.utils.MD5Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends Activity {
	protected static final String TAG = "HomeActivity";
	private GridView list_home;
	private MyAdapter adapter;
	private static String[] names = { "手机防盗", "通讯卫士", "软件管理", "进程管理", "流量统计",
			"手机杀毒", "缓存清理", "高级工具", "设置中心" };
	private static int[] ids = { R.drawable.safe, R.drawable.callmsgsafe,
			R.drawable.app, R.drawable.taskmanager, R.drawable.netmanager,
			R.drawable.trojan, R.drawable.sysoptimize, R.drawable.atools,
			R.drawable.settings };

	private SharedPreferences sp;
	private EditText et_setup_pwd;
	private EditText et_setup_confirm;
	private Button ok;
	private Button cancel;
	private AlertDialog dialog;
	private EditText et_enter_pwd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		list_home = (GridView) findViewById(R.id.list_home);
		adapter = new MyAdapter();
		list_home.setAdapter(adapter);

		list_home.setOnItemClickListener(new OnItemClickListener() {
			Intent intent;
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0: // 进入手机防盗
					showLostFindDialog();
					break;
				case 1: // 进入通讯卫士
					intent = new Intent(HomeActivity.this,
							CallSmsSafeActivity.class);
					startActivity(intent);
					break;
				case 2: // 进入软件管理
					intent = new Intent(HomeActivity.this,
							AppManagerActivity.class);
					startActivity(intent);
					break;
				case 3: // 进入进程管理
					intent = new Intent(HomeActivity.this,
							TaskManagerActivity.class);
					startActivity(intent);
					break;
				case 4: // 进入流量统计
					intent = new Intent(HomeActivity.this,
							TrafficManagerActivity.class);
					startActivity(intent);
					break;
				case 5: // 进入手机杀毒
					intent = new Intent(HomeActivity.this,
							AntiVirusActivity.class);
					startActivity(intent);
					break;
				case 6: // 进入缓存清理
					intent = new Intent(HomeActivity.this,
							CleanCacheActivity.class);
					startActivity(intent);
					break;
				case 7: // 进入高级工具
					intent = new Intent(HomeActivity.this,
							AtoolActivity.class);
					startActivity(intent);
					break;
				case 8: // 进入设置界面
					intent = new Intent(HomeActivity.this,
							SettingActivity.class);
					startActivity(intent);
					break;
				default:
					break;
				}
			}
		});
	}

	protected void showLostFindDialog() {
		// 判断是否设置过密码
		if (isSetupPwd()) {
			// 已经设置过密码，弹出输入对话框
			showEnterDialog();
		} else {
			// 没有设置过密码，弹出设置密码的对话框
			showSetupPwdDialog();
		}
	}

	/**
	 * 设置密码对话框
	 */
	private void showSetupPwdDialog() {
		AlertDialog.Builder builder = new Builder(HomeActivity.this);
		// 自定义一个布局文件
		View view = View.inflate(HomeActivity.this,
				R.layout.dialog_setup_password, null);
		// builder.setView(view);
		// builder.show();
		// 以下三行代码是为了适配低版本的模拟器，为了美观，一个小细节
		dialog = builder.create();
		dialog.setView(view, 0, 0, 0, 0);
		dialog.show();

		et_setup_pwd = (EditText) view.findViewById(R.id.et_setup_pwd);
		et_setup_confirm = (EditText) view.findViewById(R.id.et_setup_confirm);
		ok = (Button) view.findViewById(R.id.ok);
		cancel = (Button) view.findViewById(R.id.cancel);

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
					Toast.makeText(HomeActivity.this, "哥们，密码为空", 0).show();
					return;
				}
				// 判断是否一致，才去保存
				if (password.equals(password_confirm)) {
					// 一致的话
					/**
					 * 保存密码 消掉对话框 进入手机防盗界面
					 */
					Editor edit = sp.edit();
					edit.putString("password", MD5Utils.md5Password(password));
					edit.commit();

					dialog.dismiss();
					Log.i(TAG, "进入手机防盗界面");
					Intent intent = new Intent(HomeActivity.this,LostFindActivity.class);
					startActivity(intent);
				} else {
					Toast.makeText(HomeActivity.this, "哥们，密码不一致", 0).show();
					return;
				}
			}
		});
	}

	/**
	 * 输入对话框
	 */
	private void showEnterDialog() {
		AlertDialog.Builder builder = new Builder(HomeActivity.this);
		// 自定义一个布局文件
		View view = View.inflate(HomeActivity.this,
				R.layout.dialog_enter_password, null);
		// builder.setView(view);
		// dialog = builder.show();
		// 以下三行代码是为了适配低版本的模拟器，为了美观
		dialog = builder.create();
		dialog.setView(view, 0, 0, 0, 0);
		dialog.show();

		et_enter_pwd = (EditText) view.findViewById(R.id.et_enter_pwd);
		ok = (Button) view.findViewById(R.id.ok);
		cancel = (Button) view.findViewById(R.id.cancel);

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
				String password = et_enter_pwd.getText().toString().trim();
				if (TextUtils.isEmpty(password)) {
					Toast.makeText(HomeActivity.this, "哥们，密码为空", 0).show();
					return;
				}
				// 判断是否一致，才去保存
				String password_confirm = sp.getString("password", null);
				if (MD5Utils.md5Password(password).equals(password_confirm)) {
					// 一致的话
					/**
					 * 消掉对话框 
					 * 进入手机防盗界面
					 */
					dialog.dismiss();
					Log.i(TAG, "进入手机防盗界面");
					Intent intent = new Intent(HomeActivity.this,LostFindActivity.class);
					startActivity(intent);
				} else {
					Toast.makeText(HomeActivity.this, "哥们，密码错误", 0).show();
					et_enter_pwd.setText("");
					return;
				}
			}
		});
	}

	/**
	 * 判断是否设置过密码
	 * 
	 * @return
	 */
	private boolean isSetupPwd() {
		String password = sp.getString("password", null);
		if (TextUtils.isEmpty(password)) {
			return false;
		} else {
			return true;
		}
	}

	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return names.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = View.inflate(HomeActivity.this,
					R.layout.list_item_home, null);
			ImageView iv_item = (ImageView) view.findViewById(R.id.iv_item);
			TextView tv_item = (TextView) view.findViewById(R.id.tv_item);

			iv_item.setImageResource(ids[position]);
			tv_item.setText(names[position]);
			return view;
		}

	}
}
