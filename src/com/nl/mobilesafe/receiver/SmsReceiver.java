package com.nl.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

import com.nl.mobilesafe.LockScreenActivity;
import com.nl.mobilesafe.R;
import com.nl.mobilesafe.service.GPSService;

public class SmsReceiver extends BroadcastReceiver {

	private static final String TAG = "SmsReceiver";
	private SharedPreferences sp;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		Object[] objs = (Object[]) intent.getExtras().get("pdus");
		for (Object obj : objs) {
			SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);
			String sender = smsMessage.getDisplayOriginatingAddress();
			String body = smsMessage.getMessageBody();

			String safeNumber = sp.getString("safeNumber", "");
			if (sender.contains(safeNumber)) {
				if ("#*location*#".equals(body)) {
					// 获得手机位置
					Log.i(TAG, "获得手机位置地址");

					Intent i = new Intent(context, GPSService.class);
					context.startService(i);
					SharedPreferences sp = context.getSharedPreferences(
							"config", Context.MODE_PRIVATE);
					String lastLocation = sp.getString("lastLocation", null);
					if (TextUtils.isEmpty(lastLocation)) {
						SmsManager.getDefault().sendTextMessage(sender, null,
								"location getting...", null, null);
					} else {
						System.out.println(lastLocation);
						SmsManager.getDefault().sendTextMessage(sender, null,
								lastLocation, null, null);
					}

					abortBroadcast();
				} else if ("#*alarm*#".equals(body)) {
					// 手机报警
					Log.i(TAG, "手机报警");

					MediaPlayer mediaPlayer = MediaPlayer.create(context,
							R.raw.ylzs);
					mediaPlayer.setLooping(false);// 可设置循环播放
					mediaPlayer.setVolume(1.0f, 1.0f);
					mediaPlayer.start();

					abortBroadcast();

				} else if (body.contains("#*lockscreen*#")) {
					// 远程锁屏
					Log.i(TAG, "远程锁屏");
					String password = body.substring(body.lastIndexOf("#")+1);
					Intent i = new Intent(context, LockScreenActivity.class);
					
					//一定要加,服务与广播接收者是没有任务栈信息的，在它们中开启activity，要指定这个activity运行的任务栈
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//设置或者增加一个flag，让activity可以运行在新任务栈中
					i.putExtra("password", password);
					context.startActivity(i);
					
					abortBroadcast();
				} else if ("#*wipedata*#".equals(body)) {
					// 清除手机数据
					Log.i(TAG, "清除数据");

					Intent i = new Intent(context, LockScreenActivity.class);
					
					//一定要加
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					
					context.startActivity(i);
					abortBroadcast();

				}
			}
		}

	}

}
