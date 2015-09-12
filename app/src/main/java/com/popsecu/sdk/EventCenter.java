package com.popsecu.sdk;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.popsecu.sdk.Event.EventType;

public class EventCenter {
	private static EventCenter sEventCenter = null;
	private List<WeakReference<Receiver>> receivers = new ArrayList<WeakReference<Receiver>>();
	
	public static EventCenter getInstance(){
		if(sEventCenter == null){
			sEventCenter = new EventCenter();
		}
		return sEventCenter;
	}
	
	private EventCenter() {
		sEventCenter = this;
	}
	
	/**
	 * 接收事件需实现本接口
	 */
	public interface Receiver{
		public void onEvent(Event event);
	};
	
	/**
	 * 注册回调
	 */
	public void register(Receiver receiver){
		for(int i = 0;i < receivers.size();i++ ){
			Receiver it = receivers.get(i).get();
			if(it == receiver){
				Misc.logd("EventCenter:can not register twice");
				return;
			}
		}
		receivers.add(new WeakReference<Receiver>(receiver));
	}
	
	/**
	 * 注销回调
	 */
	public void unregister(Receiver receiver){
		for(int i = 0;i < receivers.size();i++ ){
			Receiver it = receivers.get(i).get();
			if(it == receiver) {
				receivers.remove(receivers.get(i));
				return;
			}
		}
	}
	
	private Handler handler = new Handler(Looper.getMainLooper()){
		public void handleMessage(Message msg) {
			procEvent((Event)msg.obj);
		};
	};  
	
	/**
	 * 发出事件通知
	 */
	public void notifyEvent(Event event){
		Message message = handler.obtainMessage(0, event);
		handler.sendMessage(message);
	}
	
//	public void notifyEvent(EventType type){
//		Event event = new Event(type);
//		notifyEvent(event);
//	}
	
	/**
	 * 直接发出和处理事件
	 */
//	public void procEvent(EventType type){
//		Event event = new Event(type);
//		procEvent(event);
//	}
	
	public void procEvent(Event event){
		for(int i = 0;i < receivers.size();i++ ){
			Receiver receiver = receivers.get(i).get();
			if(receiver != null){
				receiver.onEvent(event);
			}else{
				unregister(receiver);
				Misc.logd("some one forget unregister");
			}
		}
	}
}
