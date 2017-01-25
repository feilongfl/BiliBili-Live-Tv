package feilongcom.bilive.bilibililivetv;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.VideoView;

public class player extends AppCompatActivity {

    public String liveUrl = null;
    private VideoView player;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*set it to be no title*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        /*set it to be full screen*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if(liveUrl == null)
        {
            Toast.makeText(player.this, "liveurl error", Toast.LENGTH_LONG).show();
            finish();
        }

        setContentView(R.layout.activity_player);

        player = (VideoView)findViewById(R.id.videoView);
        Uri playuri = Uri.parse(liveUrl);
        player.setVideoURI(playuri);
        player.start();
    }
}
