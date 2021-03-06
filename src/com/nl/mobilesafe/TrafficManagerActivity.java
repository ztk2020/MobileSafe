package com.nl.mobilesafe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nl.mobilesafe.domain.TrafficInfo;

public class TrafficManagerActivity extends Activity {
	private LinearLayout my_content;
	private TextView tv_total_traffic;
	private boolean flag;
	private ImageView iv_icon;
	private TextView tv_name;
	private TextView tv_traffic_size;
	private List<TrafficInfo> infos;
	private List<ApplicationInfo> applicationInfos;
	private PackageManager pm;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_traffic_manager);
		my_content = (LinearLayout) findViewById(R.id.my_container);
		tv_total_traffic = (TextView) findViewById(R.id.tv_total_traffic);
		//1.获取包管理器
		pm = getPackageManager();
		//2.遍历整个手机系统，获取所有的应用程序的uid
		applicationInfos = pm.getInstalledApplications(0);
		infos = new ArrayList<TrafficInfo>();
		new Thread(){
			public void run() {
				for (ApplicationInfo applicationInfo : applicationInfos) {
					TrafficInfo info;
					int uid = applicationInfo.uid;
					long tx = TrafficStats.getUidTxBytes(uid);//发送的，上传的流量byte,得到应用程序的上传流量
					long rx = TrafficStats.getUidRxBytes(uid);//下载的流量byte
					//方法返回值若为-1，表示应用程序没有产生流量，或者操作系统不支持流量统计
					View view = View.inflate(getApplicationContext(), R.layout.traffic_item_count, null);
					iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
					tv_name = (TextView) view.findViewById(R.id.tv_name);
					tv_traffic_size = (TextView) view.findViewById(R.id.tv_traffic_size);
					iv_icon.setImageDrawable(applicationInfo.loadIcon(pm));
					tv_name.setText(applicationInfo.loadLabel(pm).toString());
					tv_traffic_size.setText("已用："+Formatter.formatFileSize(getApplicationContext(), (rx+tx)));
					info = new TrafficInfo();
					info.setView(view);
					long rt = tx + rx;
					info.setRt(rt);//排序将流量大的放前面
					infos.add(info);
					if(infos.size()==applicationInfos.size()){
						Collections.sort(infos, new Comparator<TrafficInfo>() {
							
							@Override
							public int compare(TrafficInfo lhs, TrafficInfo rhs) {
								return -lhs.getRt().compareTo(rhs.getRt());
							}
							
						});
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								for (TrafficInfo trafficInfo : infos) {
									my_content.addView(trafficInfo.getView());
								}
							}
						});
					}
				}
			};
		}.start();
		
		//可做成一个服务，在设置中打开，每隔1分钟检测一下流量，做一个小窗体，显示等
		flag = true;
		new Thread(){
			public void run() {
				while(flag){
					
					final long totalTx = TrafficStats.getMobileTxBytes();//手机2g/3g上传的总流量
					final long totalRx = TrafficStats.getMobileRxBytes();//手机2g/3g下载的总流量
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							tv_total_traffic.setText(Formatter.formatFileSize(getApplicationContext(), (totalRx+totalTx)));
						}
					});
					SystemClock.sleep(5000);
				}
				
			};
		}.start();
		
		TrafficStats.getTotalTxBytes();//手机全部网络接口，包括wifi,2g,3g等上传的总流量
		TrafficStats.getTotalRxBytes();//手机全部网络接口，包括wifi,2g,3g等下载的总流量
		
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		flag = false;
	}
	
}
