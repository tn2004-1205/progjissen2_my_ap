package jp.ac.gifu_u.info.takaharu.myapplication_guitar_sound;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.pitch.PitchDetector;


public class Pitch {
    // このクラスが使うファイルパスを保持する変数
    private String inputFilePath;
    private AudioDispatcher dispatcher;
    private Thread audioThread;
    private PitchDetector listener;
    // このクラスのインスタンスを作るときに、必ずファイルパスを渡すように強制する
    public Pitch(String filePath) {
        // 受け取ったファイルパスを自分の変数に保存
        this.inputFilePath = filePath;
    }
    private String Input_mp4 = this.inputFilePath;
    // リスナーの設定
    public void setListener(PitchDetectionListener listener){
        this.listener = listener;
    }
}
