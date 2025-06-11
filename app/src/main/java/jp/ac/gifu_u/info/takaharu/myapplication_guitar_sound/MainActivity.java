package jp.ac.gifu_u.info.takaharu.myapplication_guitar_sound;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import android.media.MediaPlayer;
import android.util.Log;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;

import be.tarsos.dsp.pitch.PitchDetector;

public class MainActivity extends AppCompatActivity {
    private Pitch pitchDetector;
    // 録音開始ボタン
    private Button recordButton = null;
    // 録音停止ボタン
    private  Button stopButton = null;
    // 再生ボタン
    private Button playButton = null;
    // 次へボタン
    private Button nextButton = null;
    private static final String LOG_TAG = "AudioRecordTest";
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private boolean isRecordingAvailable = false;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    // 録音するファイルの定義
    private static String fileName = null;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // fileNameの初期化
        File cacheDir = getExternalCacheDir();
        if (cacheDir != null) {
            fileName = cacheDir.getAbsolutePath() + "/audiorecordtest.mp4";
        } else {
            Log.e("MainActivity", "External cache directory not available!");
            // エラー処理
            fileName = getFilesDir().getAbsolutePath() + "/audiorecordtest.mp4"; //
            Toast.makeText(this, "外部キャッシュが利用できないため内部ストレージを使用します。", Toast.LENGTH_LONG).show();
        }
        recordButton = findViewById(R.id.record); // XMLで定義したボタンID
        stopButton = findViewById(R.id.stop); // XMLで定義したボタンID
        playButton = findViewById(R.id.playback); // XMLで定義したボタンID
        nextButton = findViewById(R.id.next); // XMLで定義したボタンID
        updateButtonStates(); // 初期ボタン状態の設定
        recordButton.setOnClickListener(v -> {
            if (isRecording) return; //録音中はリターン
            if (isPlaying) return; // 再生中もリターン
                // 録音権限の確認
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            REQUEST_RECORD_AUDIO_PERMISSION);
                }
            // 権限があるならば録音開始
            else {
                    startRecordingAndProcessAudio();
                }

        });
        stopButton.setOnClickListener(v -> {
            if(isRecording) {
                stopRecordingAndProcessAudio();
            }
        });
        playButton.setOnClickListener(v -> {
            if(isPlaying) {
                stopPlayingAndProcessAudio();
            }
            else {
                if (isRecordingAvailable && !isRecording) { // 録音中でなく再生可能なファイルがある場合
                    startPlayingAndProcessAudio();
                }
            }
        });
        nextButton.setOnClickListener(v -> {
            if(isPlaying || fileName == null || !new File(fileName).exists()) {
                return;
            }
            else {
                // 3. Intentを作成して、次の画面（SubActivity）を指定
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                // 4. 画面遷移を開始
                startActivity(intent);
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 権限が許可された
                startRecordingAndProcessAudio();
            } else {
                // 権限が拒否された
                Toast.makeText(this, "録音の権限がありません", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void updateButtonStates() {
        recordButton.setEnabled(!isRecording && !isPlaying); // 録音ボタンは録音中でも再生中でもない場合に実行できる
        stopButton.setEnabled(isRecording && !isPlaying); // 録音中のみ有効

        if (isPlaying) {
            playButton.setText("再生停止");
            playButton.setEnabled(true);
        } else {
            playButton.setText("再生開始");
            playButton.setEnabled(isRecordingAvailable && !isRecording); // 再生可能でなく録音中でないとき
        }
    }
    // 録音処理についての関数
    private void startRecordingAndProcessAudio() {
        // 二重録音防止
        if (isRecording) return;
        // androidのdevelopersから引用
        // 録音するためのMediaRecorderオブジェクトを作成
        recorder = new MediaRecorder();
        // 録音する音声の出力先をマイクに指定
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // ファイルのフォーマットをMPEG4に設定
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        // 出力するファイル先を指定
        recorder.setOutputFile(fileName);
        // エンコード方式を設定
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            recorder.prepare();
            recorder.start();
            isRecording = true;
            isRecordingAvailable = false;
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        updateButtonStates(); // ボタン状態の更新
    }
    // 録音停止時の関数
    private void stopRecordingAndProcessAudio() {
        if (!isRecording || recorder == null) {
            isRecording = false;
            return;
        }
        try {
            recorder.stop();
            isRecordingAvailable = true;
        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, "MediaRecorder stop() failed", e);
            Toast.makeText(this, "録音停止に失敗しました。", Toast.LENGTH_SHORT).show();
        } finally {
            isRecording = false;
            recorder.release(); //リソースを開放
            recorder = null;
            updateButtonStates(); // ボタン状態の更新
        }
    }
    // 録音を再生する関数
    private void startPlayingAndProcessAudio() {
        if (isPlaying || fileName == null || !new File(fileName).exists()) {
            return;
        }
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.setOnCompletionListener(mp -> { // 再生完了リスナー
                isPlaying = false;
                player.release();
                player = null;
                updateButtonStates();
            });
            player.prepare();
            player.start();
            isPlaying = true;
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
            isPlaying = false;
            player.release();
            player = null;
        }
    }
    private void stopPlayingAndProcessAudio() {
        if(!isPlaying || player == null) {
            isPlaying = false;
            player.release();
            player = null;
            updateButtonStates();
            return;
        }
        try {
            if (player.isPlaying()) {
                player.stop();
            }
        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, "停止できませんでした", e);
        } finally {
            isPlaying = false;
            player.release();
            player = null;
            updateButtonStates();
        }
    }
    @Override
    // アクテビティが終了した時のリソース開放
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }

}