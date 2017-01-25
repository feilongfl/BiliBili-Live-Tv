package feilongcom.bilive.bilibililivetv;

import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LivePlayer extends AppCompatActivity {

    private Button playButton;
    private EditText idInput;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_player);
        /**********************warning!!!****************************/
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        /************************************************/

        //find id input box
        idInput = (EditText) findViewById(R.id.editText);

        //find button
        playButton = (Button) findViewById(R.id.button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idInput.getText().length() != 0) {
                    //Toast.makeText(LivePlayer.this, idInput.getText().toString(), Toast.LENGTH_LONG).show();
                    String roomId = getRoomId(idInput.getText().toString());
                    String playurl = getMp4PlayUrls(roomId);

                    //openFile(Uri.parse(playurl));

                    /*
                    Intent it = new Intent(Intent.ACTION_VIEW);
                    it.setDataAndType(Uri.parse(playurl),"flv-application/octet-stream");
                    Toast.makeText(LivePlayer.this, "run activity", Toast.LENGTH_LONG).show();
                    startActivity(it);
                    */
                    if(playurl != null) {
                        String extension = MimeTypeMap.getFileExtensionFromUrl(playurl);
                        String mimeType = "video/mp4";
                        Intent mediaIntent = new Intent(Intent.ACTION_VIEW);
                        mediaIntent.setDataAndType(Uri.parse(playurl), mimeType);
                        startActivity(mediaIntent);
                    }else{
                        Toast.makeText(LivePlayer.this, "playurl error", Toast.LENGTH_LONG).show();
                    }
                } else
                    Toast.makeText(LivePlayer.this, "input error", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 打开文件
     * @param file
     */
    private void openFile(Uri uri){

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //设置intent的Action属性
        intent.setAction(Intent.ACTION_VIEW);
        //获取文件file的MIME类型
        //String type = getMIMEType(file);
        String type = "flv-application/octet-stream";
        //设置intent的data和Type属性。
        intent.setDataAndType(uri, type);
        //跳转
        startActivity(intent);
    }

    /**
     * 根据文件后缀名获得对应的MIME类型。
     * @param file
     */
    private String getMIMEType(File file) {

        String type="*/*";
        String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if(dotIndex < 0){
            return type;
        }
    /* 获取文件的后缀名 */
        String end=fName.substring(dotIndex,fName.length()).toLowerCase();
        if(end=="")return type;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for(int i=0;i<MIME_MapTable.length;i++){ //MIME_MapTable??在这里你一定有疑问，这个MIME_MapTable是什么？
            if(end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }

    private  String getMp4PlayUrls(String roomId)
    {
        String playUrlHtml = get("http://api.live.bilibili.com/api/playurl?platform=h5&cid=" + roomId);
        if(playUrlHtml != null) {
            try {
                JSONObject jsonObject = new JSONObject(playUrlHtml);
                if(jsonObject.getInt("code") == 0)
                    return jsonObject.getString("data");
                else
                    Toast.makeText(LivePlayer.this, "json return code error", Toast.LENGTH_LONG).show();

            }catch (JSONException ex){
                Toast.makeText(LivePlayer.this, "json analyse error", Toast.LENGTH_LONG).show();
            }
        }
        return null;
    }

    private String getPlayUrls(String roomId)
    {
        String playUrlHtml = get("http://live.bilibili.com/api/playurl?player=1&cid=" + roomId + "&quality=0");
        if(playUrlHtml != null){
            Pattern pattern = Pattern.compile("(http[\\:\\/0-9A-Za-z_\\.=\\?&\\-]+)");
            Matcher matcher = pattern.matcher(playUrlHtml);
            //Random r = new Random();
            if (matcher.find()){
                //int a = matcher.groupCount();
                //String playUrl = matcher.group(r.nextInt(matcher.groupCount()));
                String playUrl = matcher.group(1);
                //Toast.makeText(LivePlayer.this, playUrl, Toast.LENGTH_LONG).show();
                return playUrl;
            }
        }
        else
            Toast.makeText(LivePlayer.this, "get play url error", Toast.LENGTH_LONG).show();
        return null;
    }

    private String getRoomId (String liveId)
    {
        String roomIdHtml = get("http://live.bilibili.com/" + liveId);//get roomid

        if (roomIdHtml != null) {
            Pattern pattern = Pattern.compile("ROOMID = (\\d+);");
            Matcher matcher = pattern.matcher(roomIdHtml);
            if (matcher.find()) {
                String roomidtext = matcher.group(1);//find room id
                Toast.makeText(LivePlayer.this, "Room Id:" + roomidtext, Toast.LENGTH_LONG).show();
                return roomidtext;
            } else
                Toast.makeText(LivePlayer.this, "regex error", Toast.LENGTH_LONG).show();
        } else
            Toast.makeText(LivePlayer.this, "network error", Toast.LENGTH_LONG).show();
        return null;
    }

    public String get(String urlPath) {
        HttpURLConnection connection = null;
        InputStream is = null;
        try {
            URL url = new URL(urlPath);
            //获得URL对象
            connection = (HttpURLConnection) url.openConnection();
            //获得HttpURLConnection对象
            connection.setRequestMethod("GET");
            // 默认为GET
            connection.setUseCaches(false);
            //不使用缓存
            connection.setConnectTimeout(10000);
            //设置超时时间
            connection.setReadTimeout(10000);
            //设置读取超时时间
            connection.setDoInput(true);
            //设置是否从httpUrlConnection读入，默认情况下是true;
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                //相应码是否为200
                is = connection.getInputStream();
                //获得输入流
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                //包装字节流为字符流
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
                connection = null;
            }
            if (is != null) {
                try {
                    is.close();
                    is = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private final String[][] MIME_MapTable={
            //{后缀名， MIME类型}
            {".3gp",    "video/3gpp"},
            {".apk",    "application/vnd.android.package-archive"},
            {".asf",    "video/x-ms-asf"},
            {".avi",    "video/x-msvideo"},
            {".bin",    "application/octet-stream"},
            {".bmp",    "image/bmp"},
            {".c",  "text/plain"},
            {".class",  "application/octet-stream"},
            {".conf",   "text/plain"},
            {".cpp",    "text/plain"},
            {".doc",    "application/msword"},
            {".docx",   "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls",    "application/vnd.ms-excel"},
            {".xlsx",   "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe",    "application/octet-stream"},
            {".flv",    ""},
            {".gif",    "image/gif"},
            {".gtar",   "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h",  "text/plain"},
            {".htm",    "text/html"},
            {".html",   "text/html"},
            {".jar",    "application/java-archive"},
            {".java",   "text/plain"},
            {".jpeg",   "image/jpeg"},
            {".jpg",    "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log",    "text/plain"},
            {".m3u",    "audio/x-mpegurl"},
            {".m4a",    "audio/mp4a-latm"},
            {".m4b",    "audio/mp4a-latm"},
            {".m4p",    "audio/mp4a-latm"},
            {".m4u",    "video/vnd.mpegurl"},
            {".m4v",    "video/x-m4v"},
            {".mov",    "video/quicktime"},
            {".mp2",    "audio/x-mpeg"},
            {".mp3",    "audio/x-mpeg"},
            {".mp4",    "video/mp4"},
            {".mpc",    "application/vnd.mpohun.certificate"},
            {".mpe",    "video/mpeg"},
            {".mpeg",   "video/mpeg"},
            {".mpg",    "video/mpeg"},
            {".mpg4",   "video/mp4"},
            {".mpga",   "audio/mpeg"},
            {".msg",    "application/vnd.ms-outlook"},
            {".ogg",    "audio/ogg"},
            {".pdf",    "application/pdf"},
            {".png",    "image/png"},
            {".pps",    "application/vnd.ms-powerpoint"},
            {".ppt",    "application/vnd.ms-powerpoint"},
            {".pptx",   "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop",   "text/plain"},
            {".rc", "text/plain"},
            {".rmvb",   "audio/x-pn-realaudio"},
            {".rtf",    "application/rtf"},
            {".sh", "text/plain"},
            {".tar",    "application/x-tar"},
            {".tgz",    "application/x-compressed"},
            {".txt",    "text/plain"},
            {".wav",    "audio/x-wav"},
            {".wma",    "audio/x-ms-wma"},
            {".wmv",    "audio/x-ms-wmv"},
            {".wps",    "application/vnd.ms-works"},
            {".xml",    "text/plain"},
            {".z",  "application/x-compress"},
            {".zip",    "application/x-zip-compressed"},
            {"",        "*/*"}
    };

}
