package com.epush.apns;

import com.epush.apns.exception.ApnsException;

import java.util.concurrent.Future;

/**
 * 推送接口
 *
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public interface Push<T, E> {

	/**
	 * 同步单个推送
	 * 
	 * @param e
	 * @return
	 * @throws ApnsException
	 */
	T syncPush(E e) throws ApnsException;

	/**
	 * 异步单个推送
	 * 
	 * @param e
	 * @return
	 * @throws ApnsException
	 */
	Future<T> asynPush(E e) throws ApnsException;

}
