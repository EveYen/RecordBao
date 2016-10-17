package com.eveyen.RecordBao.Record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Record_implement {
    private static final String TAG = Record_implement.class.getName();
    private static final int TIMER_INTERVAL = 120;

    private int audioSource; //聲音來源
    private int sampleRate; //音頻採樣率(每秒採樣幾次)
    private int audioFormat; //PCM編碼，通常為16bit或8bit
    private int channelConfig; //聲道，MONO是單聲道，STEREO是立體聲
    private short nChannels; //聲道數
    private short nFormat; //編碼對應的數字

    private String filePath; //檔案url
    private AudioRecord audioRecorder = null;
    private RandomAccessFile randomAccessWriter; //寫入檔案用

    private int framePeriod; //每個time interval的頻率
    private int bufferSize; // 每次錄音進來要用的buffer的大小

    private byte[] buffer; //讀進來的片段
    private int payloadSize; //已讀進來的長度

    public State state;
    public enum State {
        INITIALIZING,// 初始化
        RECORDING,// 正在記錄
        STOPPED,// 需要重置
        ERROR// 需要重建
    }

    public Record_implement(String path){
        initParameters();
        filePath = path;
        try {
            audioRecorder = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSize);
            if (audioRecorder.getState() != AudioRecord.STATE_INITIALIZED)
                throw new Exception("AudioRecord initialization failed");
            audioRecorder.setRecordPositionUpdateListener(updateListener);
            audioRecorder.setPositionNotificationPeriod(framePeriod);//呼叫updateListener的頻率
            
            state = State.INITIALIZING;
        }catch (Exception e) {
            Log.e(TAG, e.getMessage());
            state = State.ERROR;
        }
    }


    public void initParameters(){
        audioSource = MediaRecorder.AudioSource.MIC;
        sampleRate = 8000;
        audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        nFormat = (short) ((audioFormat == AudioFormat.ENCODING_PCM_16BIT) ? 16 : 8);
        nChannels = (short) ((channelConfig == AudioFormat.CHANNEL_CONFIGURATION_MONO) ? 1 : 2);

        framePeriod = sampleRate * TIMER_INTERVAL / 1000; //每個time interval的頻率
        bufferSize = framePeriod * 2 * nFormat * nChannels / 8; //每個time interval的頻率＊2byte＊編碼數＊聲道數/8
        if (bufferSize < AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)) { //檢查是否小於最小buffer
            bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
            framePeriod = bufferSize / (2 * nFormat * nChannels / 8); //用最小buffer重算framePeriod
        }
    }
    private AudioRecord.OnRecordPositionUpdateListener updateListener = new AudioRecord.OnRecordPositionUpdateListener() {
        @Override
        public void onMarkerReached(AudioRecord recorder) {}

        @Override
        public void onPeriodicNotification(AudioRecord recorder) {
            audioRecorder.read(buffer, 0, buffer.length); // Fill buffer
            try {
                randomAccessWriter.write(buffer); // Write buffer to file
                payloadSize += buffer.length;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    };

    private void prepare() {
        try {
            if ((audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) & (filePath != null)) {
                // 写文件头
                randomAccessWriter = new RandomAccessFile(filePath, "rw");
                randomAccessWriter.setLength(0); // Set file length to 0, to prevent unexpected behavior in case the file already existed
                randomAccessWriter.writeBytes("RIFF");
                randomAccessWriter.writeInt(0); // Final file size not known yet, write 0
                randomAccessWriter.writeBytes("WAVE");
                randomAccessWriter.writeBytes("fmt ");
                randomAccessWriter.writeInt(Integer.reverseBytes(16)); // Sub-chunk size, 16 for PCM
                randomAccessWriter.writeShort(Short.reverseBytes((short) 1)); // AudioFormat, 1 for PCM
                randomAccessWriter.writeShort(Short.reverseBytes(nChannels));// Number of channels, 1 for mono, 2 for stereo
                randomAccessWriter.writeInt(Integer.reverseBytes(sampleRate)); // Sample rate
                randomAccessWriter.writeInt(Integer.reverseBytes(sampleRate * nFormat * nChannels / 8)); // Byte rate, SampleRate*NumberOfChannels*BitsPerSample/8
                randomAccessWriter.writeShort(Short.reverseBytes((short) (nChannels * nFormat / 8))); // Block align, NumberOfChannels*BitsPerSample/8
                randomAccessWriter.writeShort(Short.reverseBytes(nFormat)); // Bits per sample
                randomAccessWriter.writeBytes("data");
                randomAccessWriter.writeInt(0); // Data chunk size not known yet, write 0

                buffer = new byte[framePeriod * nFormat / 8 * nChannels];

            } else {
                Log.e(TAG, "prepare() method called on uninitialized recorder");
                state = State.ERROR;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            state = State.ERROR;
        }
    }

    public void start() {
        if (state == State.STOPPED) {
            try {
                if (randomAccessWriter != null)
                    randomAccessWriter.close();
                (new File(filePath)).delete();
                state = State.INITIALIZING;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                state = State.ERROR;
            }
        }
        if (state == State.INITIALIZING) {
            prepare();
            payloadSize = 0;
            audioRecorder.startRecording();
            audioRecorder.read(buffer, 0, buffer.length);
            state = State.RECORDING;
        }

    }

    public void stop() {
        if (state == State.RECORDING) {
            audioRecorder.stop();
            state = State.STOPPED;
            try {
                randomAccessWriter.seek(4); // Write size to RIFF header
                randomAccessWriter.writeInt(Integer.reverseBytes(36 + payloadSize)); //長度

                randomAccessWriter.seek(40); // Write size to Subchunk2Size field
                randomAccessWriter.writeInt(Integer.reverseBytes(payloadSize)); //錄音的長度，比整份文件小36

                randomAccessWriter.close();
            } catch (IOException e) {
                Log.e(TAG, "I/O exception occured while closing output file");
                state = State.ERROR;
            }
        } else {
            Log.e(TAG, "stop() called on illegal state");
            state = State.ERROR;
        }
    }

    public void release() {
        if (audioRecorder != null) {
            audioRecorder.release();
        }
    }
}
