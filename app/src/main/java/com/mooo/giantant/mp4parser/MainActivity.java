package com.mooo.giantant.mp4parser;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ConvertAsyncTask tasks = null;
    TextView textView;
    Button  button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        button = findViewById(R.id.button);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheck1 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED && permissionCheck1 == PackageManager.PERMISSION_GRANTED ) {
            Toast.makeText(this, "READ/WRITE 권한 주어져 있음", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "READ/WRITE 권한 없음", Toast.LENGTH_LONG).show();
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //이미 사용자가 한번 거부 했으므로 권환 요청의 필요성을 설명할 필요가 있음
                Toast.makeText(this, "READ/WRITE 진짜 필요하니깐 주세요. ", Toast.LENGTH_LONG).show();
            }else{
                ActivityCompat.requestPermissions(this,new String [] {Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                ActivityCompat.requestPermissions(this,new String [] {Manifest.permission.READ_EXTERNAL_STORAGE},1);
            }
        }

    }

    void onClickConvert(View view){

        String[] videoUris = new String[]{
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/0.mp4",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/1.mp4",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/2.mp4",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/3.mp4",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/4.mp4",
        };
        if(tasks == null) {
            tasks = new ConvertAsyncTask();
            tasks.execute(videoUris);

        }
    }

    public class ConvertAsyncTask extends AsyncTask<String[],Integer,Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            textView.setText("변환중..");
            button.setEnabled(false);
        }

        @Override
        protected Void doInBackground(String[]... strings) {

            appendMovie(strings[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            textView.setText("대기중..");
            button.setEnabled(true);
            tasks = null;
        }
    }

    void appendMovie(String[] videoUris)
    {
        List<Movie> inMovies = new ArrayList<Movie>();
        for (String videoUri : videoUris) {
            try {
                inMovies.add(MovieCreator.build(videoUri));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        List<Track> videoTracks = new LinkedList<Track>();
        List<Track> audioTracks = new LinkedList<Track>();

        for (Movie m : inMovies) {
            for (Track t : m.getTracks()) {
                Log.d("TAG1",t.getHandler());
                if (t.getHandler().equals("soun")) {
                    audioTracks.add(t);
                }
                if (t.getHandler().equals("vide")) {
                    videoTracks.add(t);
                }
            }
        }

        Movie result = new Movie();

        if (!audioTracks.isEmpty()) {
            try {
                result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!videoTracks.isEmpty()) {
            try {
                result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Container out = new DefaultMp4Builder().build(result);

        FileChannel fc = null;
        try {
            String outpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/out.mp4";

            fc = new RandomAccessFile(String.format(outpath), "rw").getChannel();
            out.writeContainer(fc);
            fc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
