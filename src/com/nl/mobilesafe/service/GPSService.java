package com.nl.mobilesafe.service;

import java.io.InputStream;

import com.nl.mobilesafe.utils.ModifyOffset;
import com.nl.mobilesafe.utils.PointDouble;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

public class GPSService extends Service {

	// λ�÷���
	private LocationManager lm;
	private MyLocationListener listener;
	private String provider;

	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		lm = (LocationManager) getSystemService(LOCATION_SERVICE);
		// List<String> providers = lm.getAllProviders();
		// for (String provider : providers) {
		// System.out.println("provider");3�ַ�ʽ�����磬gps,��վ
		// }
		
		// ע�����λ�÷��� gps��Ȼ�Ƚ�׼��������ס�˾��޷���λ
		// lm.setTestProviderEnabled("gps", true);//�Զ���gpsû���ô�
		// lm.requestLocationUpdates("gps", 60000, 50, listener);

		// ��λ���ṩ����������,�õ�һ����õ������ṩ��,,���ٵ�����gps���л�վ�����磬ʲô�����ʲô
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);

		// ���ò���ϸ����
		// criteria.setAccuracy(Criteria.ACCURACY_FINE);//����Ϊ��󾫶�
		// criteria.setAltitudeRequired(false);//��Ҫ�󺣰���Ϣ
		// criteria.setBearingRequired(false);//��Ҫ��λ��Ϣ
		// criteria.setCostAllowed(true);//�Ƿ���������
		// criteria.setPowerRequirement(Criteria.POWER_LOW);//�Ե�����Ҫ��

		provider = lm.getBestProvider(criteria, true);
		listener = new MyLocationListener();
		lm.requestLocationUpdates(provider, 0, 0, listener);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		lm.removeUpdates(listener);
		// lm.setTestProviderEnabled(provider, false);
		listener = null;
	}

	class MyLocationListener implements LocationListener {

		// ��λ�÷����ı�ʱ���ص�
		@Override
		public void onLocationChanged(Location location) {

			// ͨ���������괦������λ�ñ�׼ȷ

			ModifyOffset offset;
			PointDouble pointDouble = null;
			try {
				InputStream is = getAssets().open("axisoffset.dat");
				offset = ModifyOffset.getInstance(is);
				// �ѱ�׼�����������ת����china��ʵ���֮꣬���ڵ�ͼ�ϲ���
				pointDouble = offset.s2c(new PointDouble(location
						.getLongitude(), location.getLatitude()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			String longitude ="\nlongitude:" + pointDouble.getX() + "\n";
			String latitude = "latitude:" + pointDouble.getY() + "\n";
			String accuracy = "accuracy:" + location.getAccuracy();
//			String longitude =pointDouble.getX() + ",";
//			String latitude =pointDouble.getY() + ",";
//			String accuracy =location.getAccuracy()+"";
			// �����Ÿ���ȫ���룬����λ��

			SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
			Editor edit = sp.edit();
			edit.putString("lastLocation", longitude + latitude + accuracy);
			edit.commit();

		}

		// ��ĳһ��λ���ṩ��״̬�����仯��ʱ�� �ر�--������ ���߿���--���ر�
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		// ��ĳһλ���ṩ�߿���ʱ�ص�
		@Override
		public void onProviderEnabled(String provider) {

		}

		// ��ĳһλ���ṩ�߲�����ʱ�ص�
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

	}
}