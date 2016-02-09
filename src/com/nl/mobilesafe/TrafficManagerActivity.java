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
		//1.��ȡ��������
		pm = getPackageManager();
		//2.���������ֻ�ϵͳ����ȡ���е�Ӧ�ó����uid
		applicationInfos = pm.getInstalledApplications(0);
		infos = new ArrayList<TrafficInfo>();
		new Thread(){
			public void run() {
				for (ApplicationInfo applicationInfo : applicationInfos) {
					TrafficInfo info;
					int uid = applicationInfo.uid;
					long tx = TrafficStats.getUidTxBytes(uid);//���͵ģ��ϴ�������byte,�õ�Ӧ�ó�����ϴ�����
					long rx = TrafficStats.getUidRxBytes(uid);//���ص�����byte
					//��������ֵ��Ϊ-1����ʾӦ�ó���û�в������������߲���ϵͳ��֧������ͳ��
					View view = View.inflate(getApplicationContext(), R.layout.traffic_item_count, null);
					iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
					tv_name = (TextView) view.findViewById(R.id.tv_name);
					tv_traffic_size = (TextView) view.findViewById(R.id.tv_traffic_size);
					iv_icon.setImageDrawable(applicationInfo.loadIcon(pm));
					tv_name.setText(applicationInfo.loadLabel(pm).toString());
					tv_traffic_size.setText("���ã�"+Formatter.formatFileSize(getApplicationContext(), (rx+tx)));
					info = new TrafficInfo();
					info.setView(view);
					long rt = tx + rx;
					info.setRt(rt);//����������ķ�ǰ��
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
		
		//������һ�������������д򿪣�ÿ��1���Ӽ��һ����������һ��С���壬��ʾ��
		flag = true;
		new Thread(){
			public void run() {
				while(flag){
					
					final long totalTx = TrafficStats.getMobileTxBytes();//�ֻ�2g/3g�ϴ���������
					final long totalRx = TrafficStats.getMobileRxBytes();//�ֻ�2g/3g���ص�������
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
		
		TrafficStats.getTotalTxBytes();//�ֻ�ȫ������ӿڣ�����wifi,2g,3g���ϴ���������
		TrafficStats.getTotalRxBytes();//�ֻ�ȫ������ӿڣ�����wifi,2g,3g�����ص�������
		
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		flag = false;
	}
	
}