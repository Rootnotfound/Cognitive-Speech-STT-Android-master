package com.microsoft.CognitiveServicesExample;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;
import android.content.Intent;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.microsoft.bing.speech.Conversation;
import com.microsoft.bing.speech.SpeechClientStatus;
import com.microsoft.cognitiveservices.speechrecognition.DataRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionStatus;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main3Activity extends Activity implements ISpeechRecognitionServerEvents {

    int m_waitSeconds = 0;
    DataRecognitionClient dataClient = null;
    MicrophoneRecognitionClient micClient = null;
    FinalResponseStatus isReceivedResponse = FinalResponseStatus.NotReceived;
    EditText _logText;
    //RadioGroup _radioGroup;
    Button _buttonSelectMode;
    Button _startButton;
    public static final String EXTRA_MESSAGE = "com.microsoft.CognitiveServicesExample.MESSAGE";
    String newMessage = new String();
    public enum FinalResponseStatus { NotReceived, OK, Timeout }

    /**
     * Gets the primary subscription key
     */
    public String getPrimaryKey() {
        return this.getString(R.string.primaryKey);
    }

    /**
     * Gets the LUIS application identifier.
     * @return The LUIS application identifier.
     */
    private String getLuisAppId() {
        return this.getString(R.string.luisAppID);
    }
    /**
     * Gets the LUIS subscription identifier.
     * @return The LUIS subscription identifier.
     */
    private String getLuisSubscriptionID() {
        return this.getString(R.string.luisSubscriptionID);
    }

    /**
     * Gets a value indicating whether or not to use the microphone.
     * @return true if [use microphone]; otherwise, false.
     */

    private Boolean getUseMicrophone() {
        //int id = this._radioGroup.getCheckedRadioButtonId();
        return true;
    }

    /**
     * Gets a value indicating whether LUIS results are desired.
     * @return true if LUIS results are to be returned otherwise, false.
     */

    private Boolean getWantIntent() {
        //int id = this._radioGroup.getCheckedRadioButtonId();
        return false;
    }

    /**
     * Gets the current speech recognition mode.
     * @return The speech recognition mode.
     */

    /**
     * Gets the default locale.
     * @return The default locale.
     */
    private String getDefaultLocale() {
        return "zh-CN";
    }

    /**
     * Gets the short wave file path.
     * @return The short wave file.
     */

    private String getAuthenticationUri() {
        return this.getString(R.string.authenticationUri);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        // Get the Intent that started this activity and extract the string
        getIntent();
        //String message = intent.getStringExtra(Main2Activity.EXTRA_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        //TextView textView = (TextView) findViewById(R.id.textView);
        //textView.setText(message);

        this._logText = (EditText) findViewById(R.id.editText2);
        this._buttonSelectMode = (Button)findViewById(R.id.button4);
        this._startButton = (Button) findViewById(R.id.button2);
        if (getString(R.string.primaryKey).startsWith("Please")) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.add_subscription_key_tip_title))
                    .setMessage(getString(R.string.add_subscription_key_tip))
                    .setCancelable(false)
                    .show();
        }
        final Main3Activity This = this;
        this._startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                This.StartButton_Click(arg0);
            }
        });

        this._buttonSelectMode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                reset_Everything();
            }
        });

        this.ShowMenu(true);

    }

    private void ShowMenu(boolean show) {
        if (show) {
            //this._radioGroup.setVisibility(View.VISIBLE);
            this._logText.setVisibility(View.INVISIBLE);
        } else {
            //this._radioGroup.setVisibility(View.INVISIBLE);
            this._logText.setText("");
            this._logText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Handles the Click event of the _startButton control.
     */
    private void StartButton_Click(View arg0) {
        this._startButton.setEnabled(false);
        //this._radioGroup.setEnabled(false);

        //this.m_waitSeconds = this.getMode() == SpeechRecognitionMode.ShortPhrase ? 20 : 200;

        this.ShowMenu(false);

        this.LogRecognitionStart();

        //if (this.getUseMicrophone()) {
        if (this.micClient == null) {
            if (this.getWantIntent()) {
                this.WriteLine("--- Start microphone dictation with Intent detection ----");

                this.micClient =
                        SpeechRecognitionServiceFactory.createMicrophoneClientWithIntent(
                                this,
                                this.getDefaultLocale(),
                                this,
                                this.getPrimaryKey(),
                                this.getPrimaryKey(),
                                this.getLuisAppId(),
                                this.getLuisSubscriptionID(),
                                "https://7b59cc355dd74957846e3b78a47946d5.api.cris.ai/ws/cris/speech/recognize/continuous");
            }
            else
            {
                this.micClient = SpeechRecognitionServiceFactory.createMicrophoneClient(
                        this,
                        SpeechRecognitionMode.LongDictation,
                        this.getDefaultLocale(),
                        this,
                        this.getPrimaryKey(),
                        this.getPrimaryKey(),
                        "https://7b59cc355dd74957846e3b78a47946d5.api.cris.ai/ws/cris/speech/recognize/continuous");
            }

            this.micClient.setAuthenticationUri(this.getAuthenticationUri());
        }

        this.micClient.startMicAndRecognition();
        //}
    /*else
        //{
            if (null == this.dataClient) {
                if (this.getWantIntent()) {
                    this.dataClient =
                            SpeechRecognitionServiceFactory.createDataClientWithIntent(
                                    this,
                                    this.getDefaultLocale(),
                                    this,
                                    this.getPrimaryKey(),
                                    this.getLuisAppId(),
                                    this.getLuisSubscriptionID());
                }
                else {
                    this.dataClient = SpeechRecognitionServiceFactory.createDataClient(
                            this.getMode(),
                            this.getDefaultLocale(),
                            this,
                            this.getPrimaryKey());
                }

                this.dataClient.setAuthenticationUri(this.getAuthenticationUri());
            }

            this.SendAudioHelper((this.getMode() == SpeechRecognitionMode.ShortPhrase) ? this.getShortWaveFile() : this.getLongWaveFile());
        }*/

    }

    /**
     * Logs the recognition start.
     */
    private void LogRecognitionStart() {
        String recoSource;
        if (this.getUseMicrophone()) {
            recoSource = "microphone";
        } /*else if (this.getMode() == SpeechRecognitionMode.ShortPhrase) {
            recoSource = "short wav file";
        } else {
            recoSource = "long wav file";
        }*/

        this.WriteLine("\n--- Start speech recognition using Chinese");
    }

    public void onFinalResponseReceived(final RecognitionResult response) {
        String message = new String();
        ArrayList<String> luisIntent = new ArrayList<String>(Arrays.asList("提交。", "完成。", "结束。", "搞定。", "Ok." ));
        ArrayList<String> luisCancel = new ArrayList<String>(Arrays.asList("退出。", "后退。", "返回。", "取消。"));
        String yes = "是。";
        String no = "不是。";
        boolean isFinalDicationMessage = (response.RecognitionStatus == RecognitionStatus.EndOfDictation ||
                response.RecognitionStatus == RecognitionStatus.DictationEndSilenceTimeout);
        if (null != this.micClient && this.getUseMicrophone() && (isFinalDicationMessage)) {
            // we got the final result, so it we can end the mic reco.  No need to do this
            // for dataReco, since we already called endAudio() on it as soon as we were done
            // sending all the data.
            this.micClient.endMicAndRecognition();
        }

        if (isFinalDicationMessage) {
            this._startButton.setEnabled(true);
            this.isReceivedResponse = FinalResponseStatus.OK;
        }

        if (!isFinalDicationMessage) {
            this.WriteLine("********* 您说的是 *********");
            for (int i = 0; i < response.Results.length; i++) {
                this.WriteLine(/*"[" + i + "]" + " Confidence=" + response.Results[i].Confidence +
                        " Text=\"" + */response.Results[i].DisplayText + "\"【是】，【不是】");
                message = response.Results[0].DisplayText;
            }
            if(luisIntent.contains(message)) {
                Intent intent = new Intent(this, MainActivity.class);
                //intent.putExtra(EXTRA_MESSAGE, newMessage + "请打分");
                this.micClient.endMicAndRecognition();
                startActivity(intent);
            }
            if(luisCancel.contains(message)) {
                Intent intent = new Intent(this, Main2Activity.class);
                this.micClient.endMicAndRecognition();
                startActivity(intent);
            }
            if(yes.contains(message)){
                this.WriteLine("请继续");
            }
            if(no.equals(message)){
                _logText.setText("\n--- Start speech recognition using Chinese");
                this.WriteLine("Please start speaking.");
            }
        }
    }

    /**
     * Called when a final response is received and its intent is parsed
     */
    public void onIntentReceived(final String payload) {
        this.WriteLine("--- Intent received by onIntentReceived() ---");
        this.WriteLine(payload);
        this.WriteLine();
    }

    public void onPartialResponseReceived(final String response) {
        //this.WriteLine("--- Partial result received by onPartialResponseReceived() ---");
        //this.WriteLine(response);
        //this.WriteLine();
    }

    public void onError(final int errorCode, final String response) {
        this._startButton.setEnabled(true);
        this.WriteLine("--- Error received by onError() ---");
        this.WriteLine("Error code: " + SpeechClientStatus.fromInt(errorCode) + " " + errorCode);
        this.WriteLine("Error text: " + response);
        this.WriteLine();
    }

    /**
     * Called when the microphone status has changed.
     * @param recording The current recording state
     */
    public void onAudioEvent(boolean recording) {
        //this.WriteLine("--- Microphone status change received by onAudioEvent() ---");
        //this.WriteLine("********* Microphone status: " + recording + " *********");
        if (recording) {
            this.WriteLine("Please start speaking.");
        }

        WriteLine();
        if (!recording) {
            this.micClient.endMicAndRecognition();
            this._startButton.setEnabled(true);
        }
    }

    /**
     * Writes the line.
     */
    private void WriteLine() {
        this.WriteLine("");
    }

    /**
     * Writes the line.
     * @param text The line to write.
     */
    private void WriteLine(String text) {
        this._logText.append(text + "\n");
    }

    private void reset_Everything(){
        if (this.micClient != null) {
            this.micClient.endMicAndRecognition();
            try {
                this.micClient.finalize();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            this.micClient = null;
        }
    }
}
