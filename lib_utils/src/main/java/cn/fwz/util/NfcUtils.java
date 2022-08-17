package cn.fwz.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Build;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

public class NfcUtils {
//
//    //nfc
//    public static NfcAdapter mNfcAdapter;
//    public static IntentFilter[] mIntentFilter = null;
//    public static PendingIntent mPendingIntent = null;
//    public static String[][] mTechList = null;
//
//    /**
//     * 构造函数，用于初始化nfc
//     */
//    public NfcUtils(Activity activity) {
//        mNfcAdapter = NfcCheck(activity);
//        mNfcAdapter = NfcCheck(activity);
//        //  PendingIntent是您提供给外部应用程序(例如NotificationManager,AlarmManager主屏幕AppWidgetManager或其他第三方应用程序)的令牌,它允许外部应用程序使用您的应用程序的权限来执行预定义的代码段.
//        mPendingIntent = PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
//        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
//        IntentFilter filter2 = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
//        try {
//            filter.addDataType("*/*");
//        } catch (IntentFilter.MalformedMimeTypeException e) {
//            e.printStackTrace();
//        }
//        mIntentFilter = new IntentFilter[]{filter, filter2};
//        mTechList = null;
//    }
//
//    public void destory(){
//         mNfcAdapter = null;
//         mIntentFilter = null;
//         mPendingIntent = null;
//         mTechList = null;
//    }

//    /**
//     * 检查NFC是否打开
//     */
//    public static NfcAdapter NfcCheck(Activity activity) {
//        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);
//        if (mNfcAdapter == null) {
//            return null;
//        } else {
//            if (!mNfcAdapter.isEnabled()) {
//                Intent setNfc = new Intent(Settings.ACTION_NFC_SETTINGS);
//                activity.startActivity(setNfc);
//            }
//        }
//        return mNfcAdapter;
//    }

    /**
     * 获取NFC开关情况
     */
    public static boolean isEnabled(Activity activity) {
        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (mNfcAdapter == null) {
            return false;
        }
        return mNfcAdapter.isEnabled();
    }

    /**
     * 开启NFC
     */
    public static boolean openNFC(Activity activity) {
        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (mNfcAdapter == null) {
            return false;
        }
        if (mNfcAdapter.isEnabled()) {
            return true;
        }
        Intent setNfc = new Intent(Settings.ACTION_NFC_SETTINGS);
        activity.startActivity(setNfc);
        return true;
    }

    /**
     * 读取NFC的数据
     */
    public static String readNFCFromTag(Intent intent) throws UnsupportedEncodingException {
        Parcelable[] rawArray = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawArray != null) {
            NdefMessage mNdefMsg = (NdefMessage) rawArray[0];
            NdefRecord mNdefRecord = mNdefMsg.getRecords()[0];
            if (mNdefRecord != null) {
                String readResult = new String(mNdefRecord.getPayload(), "UTF-8");
                return readResult;
            }
        }
        return "";
    }

    /**
     * 往nfc写入数据
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void writeNFCToTag(String data, Intent intent) throws IOException, FormatException {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        NdefRecord ndefRecord = NdefRecord.createTextRecord(null, data);
        NdefRecord[] records = {ndefRecord};
        NdefMessage ndefMessage = new NdefMessage(records);
        ndef.writeNdefMessage(ndefMessage);
    }

    /**
     * 读取nfcID
     */
    public static String readNFCId(Intent intent) throws UnsupportedEncodingException {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String id = ByteArrayToHexString(tag.getId());
        return id;
    }

    /**
     * 将字节数组转换为字符串
     */
    private static String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String out = "";

        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }


    /**
     * 创建NDEF文本数据
     *
     * @param text
     * @return
     */
    public static NdefRecord createTextRecord(String text) {
        byte[] langBytes = Locale.CHINA.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = Charset.forName("UTF-8");
        //将文本转换为UTF-8格式
        byte[] textBytes = text.getBytes(utfEncoding);
        //设置状态字节编码最高位数为0
        int utfBit = 0;
        //定义状态字节
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        //设置第一个状态字节，先将状态码转换成字节
        data[0] = (byte) status;
        //设置语言编码，使用数组拷贝方法，从0开始拷贝到data中，拷贝到data的1到langBytes.length的位置
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        //设置文本字节，使用数组拷贝方法，从0开始拷贝到data中，拷贝到data的1 + langBytes.length
        //到textBytes.length的位置
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        //通过字节传入NdefRecord对象
        //NdefRecord.RTD_TEXT：传入类型 读写
        NdefRecord ndefRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);
        return ndefRecord;
    }

    //往Ndef标签中写数据
    public static void writeNdef(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
//            Toast.makeText(this,"不能识别的标签类型！",Toast.LENGTH_SHORT);
//            finish();
            return;
        }
        Ndef ndef = Ndef.get(tag);//获取ndef对象
        if (!ndef.isWritable()) {
//            Toast.makeText(this,"该标签不能写入数据!",Toast.LENGTH_SHORT);
            return;
        }
        NdefRecord ndefRecord = createTextRecord("this.is.write");//创建一个NdefRecord对象
        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{ndefRecord});//根据NdefRecord数组，创建一个NdefMessage对象
        int size = ndefMessage.getByteArrayLength();
        if (ndef.getMaxSize() < size) {
//            Toast.makeText(this,"标签容量不足！",Toast.LENGTH_SHORT);
            return;
        }
        try {
            ndef.connect();//连接
            ndef.writeNdefMessage(ndefMessage);//写数据
//            Toast.makeText(this,"数据写入成功！",Toast.LENGTH_SHORT);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        } finally {
            try {
                ndef.close();//关闭连接
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //读取Ndef标签中数据
    public static String readNdef(Intent intent) {
        String result = "";
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            return result;
        }
        Ndef ndef = Ndef.get(tag);//获取ndef对象
        try {
            ndef.connect();//连接
            NdefMessage ndefMessage = ndef.getNdefMessage();//获取NdefMessage对象
            result = parseTextRecord(ndefMessage.getRecords()[0]);
            return result;
//            Toast.makeText(this,"数据读取成功！",Toast.LENGTH_SHORT);
        } catch (Exception e) {
            e.printStackTrace();
//            Utils.saveToFile(e);
        } finally {
            try {
                ndef.close();//关闭链接
            } catch (Exception e) {
                e.printStackTrace();
//                Utils.saveToFile(e);
            }
            return result;
        }
    }

    /**
     * 解析NDEF文本数据，从第三个字节开始，后面的文本数据
     *
     * @param ndefRecord
     * @return
     */
    public static String parseTextRecord(NdefRecord ndefRecord) {
        /**
         * 判断数据是否为NDEF格式
         */
        //判断TNF
        if (ndefRecord.getTnf() != NdefRecord.TNF_WELL_KNOWN) {
            return null;
        }
        //判断可变的长度的类型
        if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
            return null;
        }
        try {
            //获得字节数组，然后进行分析
            byte[] payload = ndefRecord.getPayload();
            //下面开始NDEF文本数据第一个字节，状态字节
            //判断文本是基于UTF-8还是UTF-16的，取第一个字节"位与"上16进制的80，16进制的80也就是最高位是1，
            //其他位都是0，所以进行"位与"运算后就会保留最高位
            String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8" : "UTF-16";
            //3f最高两位是0，第六位是1，所以进行"位与"运算后获得第六位
            int languageCodeLength = payload[0] & 0x3f;
            //下面开始NDEF文本数据第二个字节，语言编码
            //获得语言编码
            String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            //下面开始NDEF文本数据后面的字节，解析出文本
            String textRecord = new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);
            return textRecord;
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    public static void writeTag(Tag tag, NdefMessage message) {
        if (tag != null) {
            try {
                Ndef ndefTag = Ndef.get(tag);
                if (ndefTag == null) {
                    // Let's try to format the Tag in NDEF
                    NdefFormatable nForm = NdefFormatable.get(tag);
                    if (nForm != null) {
                        nForm.connect();
                        nForm.format(message);
                        nForm.close();
                    }
                } else {
                    ndefTag.connect();
                    ndefTag.writeNdefMessage(message);
                    ndefTag.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static NdefMessage createTextMessage(String content) {
        try {
            // Get UTF-8 byte
            byte[] lang = Locale.getDefault().getLanguage().getBytes("UTF-8");
            byte[] text = content.getBytes("UTF-8"); // Content in UTF-8

            int langSize = lang.length;
            int textLength = text.length;

            ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + langSize + textLength);
            payload.write((byte) (langSize & 0x1F));
            payload.write(lang, 0, langSize);
            payload.write(text, 0, textLength);
            NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                    NdefRecord.RTD_TEXT, new byte[0],
                    payload.toByteArray());
            return new NdefMessage(new NdefRecord[]{record});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readValue(Intent intent) {
        String readValue = null;
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                readValue = readNFCFromTag(intent);
                if (!TextUtils.isEmpty(readValue)) {
                    readValue = readValue.substring(3);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                readValue = readNdef(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return readValue;
    }
}
