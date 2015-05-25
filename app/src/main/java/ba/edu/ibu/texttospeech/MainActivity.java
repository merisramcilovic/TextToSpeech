package ba.edu.ibu.texttospeech;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity {
    private static final int REQUEST_VOICE_RECOGNITION = 1234;
    private static final int REQUEST_TTS_INSTALL = 4321;

    private TextToSpeech tts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button voiceButton = (Button) findViewById(R.id.btnVoice);
        Spinner spnLang = (Spinner) findViewById(R.id.spnLanguage);

        // Set Language spinner data
            // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.languages
                , android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
        spnLang.setAdapter(adapter);
        spnLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getBaseContext(), parent.getItemAtPosition(position) + " selected"
                        , Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Check If It is needed to download TTS data
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        startActivityForResult(checkIntent, REQUEST_TTS_INSTALL);

        // Disable button if no recognition service is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0) {
            voiceButton.setEnabled(false);
        }

        tts.speak("Hello",TextToSpeech.QUEUE_FLUSH, null);
    }

    /**
     * Handle the action of the speak button being clicked
     */
    public void onSpeakClick(View v){
        TextView mText = (TextView) findViewById(R.id.mText);
        Spinner spnLang = (Spinner) findViewById(R.id.spnLanguage);
        int l = spnLang.getSelectedItemPosition();
        String[] locales = getResources().getStringArray(R.array.locales);
        Log.d("selected pos", String.valueOf(l));
        Log.d("locale", locales[l]);
        tts.setLanguage(new Locale(locales[l]));
        tts.speak(mText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
    }

    /**
     * Handle the action of the voice button being clicked
     */
    public void onVoiceClick(View v){
        startVoiceRecognitionActivity();
    }

    /**
     * Fire an intent to start the voice recognition activity.
     */
    private void startVoiceRecognitionActivity(){
        Spinner spnLang = (Spinner) findViewById(R.id.spnLanguage);
        int l = spnLang.getSelectedItemPosition();
        String[] locales = getResources().getStringArray(R.array.locales);

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locales[l]);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice recognition Demo...");
        startActivityForResult(intent, REQUEST_VOICE_RECOGNITION);
    }

    /**
     * Handle the results from the voice recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == REQUEST_TTS_INSTALL){
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {

                        Log.d("ENGLISH_TTS", String.valueOf(tts.isLanguageAvailable(Locale.ENGLISH)));
                        Log.d("BOSNIAN_TTS", String.valueOf(tts.isLanguageAvailable(new Locale("hr", "hr"))));
                        Log.d("TURKISH_TTS", String.valueOf(tts.isLanguageAvailable(new Locale("tr", "TR"))));
                        Log.d("FRENCH_TTS", String.valueOf(tts.isLanguageAvailable(new Locale("fr"))));

                        tts.speak("Hello!", TextToSpeech.QUEUE_ADD, null);
                    }
                });
            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                ArrayList<String> languages = new ArrayList<String>();
                languages.add("tr_TR");
                languages.add("hr_HR");
                installIntent.putStringArrayListExtra(TextToSpeech.Engine.EXTRA_CHECK_VOICE_DATA_FOR
                        , languages);
                startActivity(installIntent);
            }
        }

        if (requestCode == REQUEST_VOICE_RECOGNITION && resultCode == RESULT_OK)
        {
            TextView mText = (TextView) findViewById(R.id.mText);

            // Populate the wordsList with the String values the recognition engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            Log.d("debug", matches.toString()); // Prints possible words
            Log.d("debug", matches.get(0)); // Prints the most probable word that was spoken
            mText.setText(matches.get(0));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
