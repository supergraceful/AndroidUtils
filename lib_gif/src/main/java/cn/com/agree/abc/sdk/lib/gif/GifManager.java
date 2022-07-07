package cn.com.agree.abc.sdk.lib.gif;

import cn.com.agree.abc.sdk.common.utils.Base64Util;
import cn.com.agree.abc.sdk.common.utils.FileIOUtil;
import cn.com.agree.abc.sdk.common.utils.ImageUtil;
import cn.com.agree.abc.sdk.log.xlog.XLog;


import android.content.Context;
import android.graphics.*;
import android.util.Log;

import java.io.ByteArrayOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;


public class GifManager {

	private Map<String, AnimatedGifEncoder> gifMap;

	public GifManager(){
		gifMap= new HashMap<>();
	}

	public String createGif(String id, int delay, int quality) {
		if (id == null) {
			id = IDGenerator.nextGlobal();
		}
		AnimatedGifEncoder animatedGifEncoder = new AnimatedGifEncoder();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		animatedGifEncoder.start(out);
		animatedGifEncoder.setRepeat(0);
		animatedGifEncoder.setDelay(delay);
		animatedGifEncoder.setQuality(quality); // 设置图片质量
		gifMap.put(id, animatedGifEncoder);
		XLog.i("生成gif:"+id);
		return id;
	}

	public void addBitmapFrame(String id, Bitmap im){
		AnimatedGifEncoder animatedGifEncoder = gifMap.get(id);
		if (animatedGifEncoder == null)
			throw new NullPointerException("不存在gif，id:" + id);
		if (im == null)
			throw new NullPointerException("图片为空，id:" + id);
		animatedGifEncoder.addFrame(im);

	}

	public void addBase64Frame(String id, String base64){
		AnimatedGifEncoder animatedGifEncoder = gifMap.get(id);
		if (animatedGifEncoder == null) {
			XLog.i("不存在gif，id:" + id);
			return;
		}
		XLog.i("gif:"+id+"添加frame");
		String base64Str;
		if (base64.startsWith("data:image/")) {
			base64Str = base64.substring(base64.indexOf(","));
		} else {
			base64Str = base64;
		}
		animatedGifEncoder.addFrame(Base64Util.base64ToBitmap(base64Str));
	}

	public void addFileFrame(String id, String path){
		AnimatedGifEncoder animatedGifEncoder = gifMap.get(id);
		if (animatedGifEncoder == null) {
			XLog.i("不存在gif，id:" + id);
			return;
		}
		XLog.i("gif:"+id+"添加frame");
		animatedGifEncoder.addFrame(ImageUtil.getBitmap(path));
	}

	public String getGifBase64(String id, Context context){
		AnimatedGifEncoder animatedGifEncoder = gifMap.get(id);
		boolean finish = animatedGifEncoder.finish();
		if (finish) {
			ByteArrayOutputStream out = (ByteArrayOutputStream) animatedGifEncoder.getOutputStream();
			byte[] byteArray = out.toByteArray();
			String encodeBase64 = Base64Util.byteToBase64(byteArray);
			gifMap.remove(id);
			return encodeBase64;
		} else {
			gifMap.remove(id);
			throw new RuntimeException("渲染失败，finish为false");
		}
	}

	public String getAndSaveGif(String id, Context context){
		AnimatedGifEncoder animatedGifEncoder = gifMap.get(id);
		boolean finish = animatedGifEncoder.finish();
		if (finish) {
			ByteArrayOutputStream out = (ByteArrayOutputStream) animatedGifEncoder.getOutputStream();
			byte[] byteArray = out.toByteArray();
			String filepath=""+context.getCacheDir()+System.currentTimeMillis()+".gif";
			FileIOUtil.writeFileFromBytesByStream(filepath, byteArray);
			Log.e("getAndRenderGif: ","保存成功:"+ filepath );
			return filepath;
		} else {
			gifMap.remove(id);
			throw new RuntimeException("渲染失败，finish为false");
		}

		//			FileOutputStream  outFile= null;
//			try {
//				outFile=context.openFileOutput(System.currentTimeMillis() +".gif",MODE_PRIVATE);                       //打开要保存的文件路径
//				outFile.write(byteArray);				//写入数据
//				Log.e("Main","save is success");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			finally {
//				if(out!=null){
//					try {
//						outFile.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//			}
	}
	
	public void deleteGif(String id ) {
		gifMap.remove(id);
	}

	public void clear(){
		if (gifMap!=null&&gifMap.size()>0){
			gifMap.clear();
		}
	}
}
