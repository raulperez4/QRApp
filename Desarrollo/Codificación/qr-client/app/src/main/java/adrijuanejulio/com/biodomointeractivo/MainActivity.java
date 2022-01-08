package adrijuanejulio.com.biodomointeractivo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

// Esta es nuestra actividad principal y es el medio para poder ir hacia otras actividades hechas, es el eje principal y donde esta nuestro asistente.
// Dentro de esta actividad esta el acceso a las otras, además de un algoritmo para detectar cuando el dispositivo es puesto boca abajo para que el asistente pare
// de hablar.
// ************* COMPLETAR *************//

public class MainActivity extends VoiceActivity implements SensorEventListener {
    // ASR/TTS fields wwwww
    private static final String LOGTAG = "CHATBOT";
    private static final Integer ID_PROMPT_QUERY = 0;
    private static final Integer ID_PROMPT_INFO = 1;
    private static final String LANGUAGECODE = "es_ES";

    private long startListeningTime = 0; // To skip errors (see processAsrError method)

    //Connection to DialogFlow
    private AIDataService aiDataService = null;
    // https://dialogflow.com/docs/reference/agent/#obtaining_access_tokens)

    // Parameter to  search in Wikipedia
    private String parameterWikipedia;

    private boolean greenButton = true;

    // END ASR/TTS Fields

    // Botones para las otras actividades
    private ImageButton qrButton;
    private ImageButton speechButton;
    private ImageButton nfcButton;
    private ImageButton mapaButton;
    private ImageButton exploraButton;
    private ImageButton tarifasButton;

    // Botones para las redes sociales
    private ImageButton fbButton;
    private ImageButton twitterButton;
    private ImageButton webButton;
    private ImageButton ytButton;

    // Acelerometer
    private SensorManager sensorManager;
    private float mGZ = 0;//gravity acceleration along the z axis

    // Acelerometer on/off
    private Boolean escuchaGiro = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Esto es para el flip down and up
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Asignamos los botones
        qrButton = findViewById(R.id.qr_button);
        speechButton = findViewById(R.id.speech_button);
        mapaButton = findViewById(R.id.mapa_button);
        exploraButton = findViewById(R.id.explora_button);

        fbButton = findViewById(R.id.fb_button);
        twitterButton = findViewById(R.id.twitter_button);
        webButton = findViewById(R.id.web_button);
        ytButton = findViewById(R.id.yt_button);

        //Set up the qr button
        setQrButton();

        //Initialize the speech recognizer and synthesizer
        initSpeechInputOutput(this);

        //Set up the speech button
        setSpeakButton();

        //Set up the Explora button
        setExploraButton();

        //Set up the mapa button
        setMapaButton();


        //Set up the twitter button
        setTwitterButton();

        //Set up the yt button
        setYtButton();

        //Set up the web button
        setWebButton();

        //Set up the Fb button
        setFbButton();


        //Dialogflow configuration parameters
        final String ACCESS_TOKEN = "99a6982faf8c46a5b165b5067c6f4598";
        final AIConfiguration config = new AIConfiguration(ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.Spanish,
                AIConfiguration.RecognitionEngine.System);

        aiDataService = new AIDataService(config);

        Log.i(LOGTAG, "END On create");
    }

    /**
     * Initializes the search button and its listener. When the button is pressed, a feedback is shown to the user
     * and the recognition starts
     */
    private void setSpeakButton() {
        // gain reference to speak button
        speechButton = findViewById(R.id.speech_button);
        speechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.e(LOGTAG, "Pulsando boton..");

                    //Ask the user to speak
                    if (greenButton) {
                        Log.e(LOGTAG, "BOTON: Hablame de nuevo..");
                        speak(getResources().getString(R.string.listen_again), LANGUAGECODE, ID_PROMPT_QUERY);
                        escuchaGiro = true;
                    } else {
                        Log.e(LOGTAG, "BOTON: Para de escuchar");
                        stopListening();
                        changeButtonAppearanceToDefault();
                    }

                } catch (Exception e) {
                    Log.e(LOGTAG, "TTS not accessible");
                }
            }
        });
    }

    /**
     * Inicializamos el botón QR y su listener. Cuando el botón es pulsado, se abre la cámara para leer QR
     */
    private void setQrButton() {
        // gain reference to qr button
        final Activity activity = this;

        qrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Escaneando QR");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();

            }

        });
    }

    /* Devuelve los resultados obtenidos al leer el QR */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        // Si el resultado no es nulo
        if (result != null) {

            // Si cancelamos el escaneo
            if (result.getContents() == null) {
                Toast.makeText(this, "Has cancelado el escaneo", Toast.LENGTH_LONG).show();
            } else {
//
                String contentQR = result.getContents();

                // Si el QR nos lleva a una página web
                if (contentQR.contains("http")) {
                    Uri web = Uri.parse(contentQR);
                    Intent gotoWeb = new Intent(Intent.ACTION_VIEW, web);
                    startActivity(gotoWeb);

                    // En caso contrario, nos llevará a un animal o vegetal del Explora
                } else {
                    // Low cifrate
                    if (contentQR.toLowerCase().contains("biodomointeractivo")) {
                        String id = contentQR.substring(contentQR.indexOf(":") + 1);

                        Intent intent = new Intent(this, ExploraViewActivity.class);
                        intent.putExtra("id", id);
                        startActivity(intent);
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    /**
     * Inicializamos el botón de explora y su listener
     */
    private void setExploraButton() {
        exploraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchExploraActivity();
            }

        });
    }

    /* LAunch Explora activity*/
    private void launchExploraActivity() {
        Intent intent = new Intent(this, ExploraActivity.class);
        startActivity(intent);
    }


    /**
     * Inicializamos el botón del mapa y su listener. Cuando el botón es pulsado, se abre un mapa del cual podemos hacer zoom
     */
    private void setMapaButton() {
        mapaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchMapaActivity();
            }

        });
    }

    /* Launch mapa activity*/
    private void launchMapaActivity() {
        Intent intent = new Intent(this, MapaActivity.class);
        startActivity(intent);
    }


    /**
     * Inicializamos el botón de twitter y su listener. Cuando el botón es pulsado, se activa el navegador con el twitter de San Marcos
     */
    private void setTwitterButton() {
        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://twitter.com/UNMSM_"));
                startActivity(intent);

            }

        });
    }

    /**
     * Inicializamos el botón de Facebook y su listener. Cuando el botón es pulsado, se activa el navegador con el facebook de San Marcos
     */
    private void setFbButton() {
        fbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String bg = "https://www.facebook.com/1551UNMSM";
                Uri webbiodomo = Uri.parse(bg);

                Intent gotoBG = new Intent(Intent.ACTION_VIEW, webbiodomo);
                startActivity(gotoBG);
            }

        });
    }

    /**
     * Inicializamos el botón de la pagina principal y su listener. Cuando el botón es pulsado, se activa el navegador con la web de San Marcos
     */
    private void setWebButton() {
        webButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String bg = "https://unmsm.edu.pe/";
                Uri webbiodomo = Uri.parse(bg);

                Intent gotoBG = new Intent(Intent.ACTION_VIEW, webbiodomo);
                startActivity(gotoBG);
            }

        });
    }


    /**
     * Inicializamos el botón de twitter y su listener. Cuando el botón es pulsado, se activa el navegador con el canal de youtube del biodomo
     */
    private void setYtButton() {
        ytButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://www.youtube.com/user/RTVSanMarcos"));
                startActivity(intent);
            }

        });
    }


    /******************************************************************
     *
     *
     * Movil boca abajo y que el asistente pare
     * https://stackoverflow.com/questions/17774070/android-detect-when-the-phone-flips-around
     * http://www.vogella.com/tutorials/AndroidSensor/article.html
     *
     *
     ******************************************************************/


    /**
     * Detect if the device is face up or face down
     */

    private void getAccelerometer(SensorEvent event) {
        float gz;
        int type = event.sensor.getType();
        if (type == Sensor.TYPE_ACCELEROMETER) {
            gz = event.values[2];
            if (mGZ == 0) {
                mGZ = gz;
            } else {
                if ((mGZ * gz) < 0) {
                    mGZ = gz;
                    if (gz < 0) {
                        stop();
                        escuchaGiro = false;
                    }
                }
            }
        }
    }

    /**
     * Listen to event and past to getAccelerometer
     */

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (escuchaGiro && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register this class as a listener for the orientation and
        // accelerometer sensors
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    /* END Detect if the device is face up or face down */


    /* Voice Methods*/

    /**
     * On destroy activity. Shuts down the TTS engine when finished
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        shutdown();
    }

    /************** START IMPLEMENTATION VOICE ACTIVITY **********************/
    /**
     * Explain to the user why we need their permission to record audio on the device
     * See the checkASRPermission in the VoiceActivity class
     */
    public void showRecordPermissionExplanation() {
        Toast.makeText(getApplicationContext(), R.string.asr_permission, Toast.LENGTH_SHORT).show();
    }

    /**
     * If the user does not grant permission to record audio on the device, a message is shown and the app finishes
     */
    public void onRecordAudioPermissionDenied() {
        Toast.makeText(getApplicationContext(), R.string.asr_permission_notgranted, Toast.LENGTH_SHORT).show();
        System.exit(0);
    }

    /**
     * Invoked when the ASR is ready to start listening. Provides feedback to the user to show that the app is listening:
     * * It changes the color and the message of the speech button
     */
    @Override
    public void processAsrReadyForSpeech() {
        changeButtonAppearanceToListening();
    }

    /**
     * Provides feedback to the user (by means of a Toast and a synthesized message) when the ASR encounters an error
     */
    @Override
    public void processAsrError(int errorCode) {

        changeButtonAppearanceToDefault();

        Log.e(LOGTAG, "ERROR ASR:" + errorCode);

        //Possible bug in Android SpeechRecognizer: NO_MATCH errors even before the the ASR
        // has even tried to recognized. We have adopted the solution proposed in:
        // http://stackoverflow.com/questions/31071650/speechrecognizer-throws-onerror-on-the-first-listening
        long duration = System.currentTimeMillis() - startListeningTime;
        if (duration < 500 && errorCode == SpeechRecognizer.ERROR_NO_MATCH) {
            Log.e(LOGTAG, "Doesn't seem like the system tried to listen at all. duration = " + duration + "ms. Going to ignore the error");
            stopListening();
        } else {
            int errorMsg;
            switch (errorCode) {
                case SpeechRecognizer.ERROR_AUDIO:
                    errorMsg = R.string.asr_error_audio;
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    errorMsg = R.string.asr_error_client;
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    errorMsg = R.string.asr_error_permissions;
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    errorMsg = R.string.asr_error_network;
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    errorMsg = R.string.asr_error_networktimeout;
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    errorMsg = R.string.asr_error_nomatch;
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    errorMsg = R.string.asr_error_recognizerbusy;
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    errorMsg = R.string.asr_error_server;
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    Log.e(LOGTAG, "ERROR: END TIMEOUT....");
                    errorMsg = R.string.asr_error_speechtimeout;
                    break;
                default:
                    errorMsg = R.string.asr_error; //Another frequent error that is not really due to the ASR, we will ignore it
                    break;
            }
            String msg = getResources().getString(errorMsg);
            /*this.runOnUiThread(new Runnable() { //Toasts must be in the main thread
                public void run() {
                    Toast.makeText(getApplicationContext(), R.string.asr_error, Toast.LENGTH_LONG).show();
                }
            });*/

            Log.e(LOGTAG, "Error when attempting to listen: " + msg);
            try {
                speak(msg, LANGUAGECODE, ID_PROMPT_INFO);
            } catch (Exception e) {
                Log.e(LOGTAG, "TTS not accessible");
            }
        }
    }

    /**
     * Synthesizes the best recognition result
     */
    @Override
    public void processAsrResults(ArrayList<String> nBestList, float[] nBestConfidences) {

        if (nBestList != null) {

            Log.d(LOGTAG, "ASR best result: " + nBestList.get(0));

            if (nBestList.size() > 0) {
                changeButtonAppearanceToDefault();
                Log.d(LOGTAG, "Enviando mensaje..: ");
                sendMsgToChatBot(nBestList.get(0)); //Send the best recognition hypothesis to the chatbot
                Log.d(LOGTAG, "Mensaje enviado..: ");
            }
        }
    }

    /**
     * Invoked when the TTS has finished synthesizing.
     * <p>
     * In this case, it starts recognizing if the message that has just been synthesized corresponds to a question (its id is ID_PROMPT_QUERY),
     * and does nothing otherwise.
     * <p>
     * According to the documentation the speech recognizer must be invoked from the main thread. onTTSDone callback from TTS engine and thus
     * is not in the main thread. To solve the problem, we use Androids native function for forcing running code on the UI thread
     * (runOnUiThread).
     *
     * @param uttId identifier of the prompt that has just been synthesized (the id is indicated in the speak method when the text is sent
     *              to the TTS engine)
     */
    @Override
    public void onTTSDone(String uttId) {

        Log.d(LOGTAG, "TTS done");
        if (uttId.equals(ID_PROMPT_QUERY.toString())) {
            runOnUiThread(new Runnable() {
                public void run() {
                    startListening();
                }
            });
        }
    }

    /**
     * Invoked when the TTS encounters an error.
     * <p>
     * In this case it just writes in the log.
     */
    @Override
    public void onTTSError(String uttId) {
        Log.e(LOGTAG, "TTS error");
    }

    /**
     * Invoked when the TTS starts synthesizing
     * <p>
     * In this case it just writes in the log.
     */
    @Override
    public void onTTSStart(String uttId) {
        Log.d(LOGTAG, "TTS starts speaking");
    }

    /************** END IMPLEMENTATION VOICE ACTIVITY **********************/

    /**
     * Starts listening for any user input.
     * When it recognizes something, the <code>processAsrResults</code> method is invoked.
     * If there is any error, the <code>onAsrError</code> method is invoked.
     */
    private void startListening() {
        if (deviceConnectedToInternet()) {
            try {
                /*Start listening, with the following default parameters:
                 * Language = English
                 * Recognition model = Free form,
                 * Number of results = 1 (we will use the best result to perform the search)
                 */
                startListeningTime = System.currentTimeMillis();
                Locale spanish = new Locale("es", "ES");
                listen(spanish, 1); //Start listening
            } catch (Exception e) {
                this.runOnUiThread(new Runnable() {  //Toasts must be in the main thread
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.asr_notstarted, Toast.LENGTH_SHORT).show();
                        changeButtonAppearanceToDefault();
                    }
                });

                Log.e(LOGTAG, "ASR could not be started");
                try {
                    speak(getResources().getString(R.string.asr_notstarted), LANGUAGECODE, ID_PROMPT_INFO);
                } catch (Exception ex) {
                    Log.e(LOGTAG, "TTS not accessible");
                }

            }
        } else {

            this.runOnUiThread(new Runnable() { //Toasts must be in the main thread
                public void run() {
                    Toast.makeText(getApplicationContext(), R.string.check_internet_connection, Toast.LENGTH_SHORT).show();
                    changeButtonAppearanceToDefault();
                }
            });
            try {
                speak(getResources().getString(R.string.check_internet_connection), "EN", ID_PROMPT_INFO);
            } catch (Exception ex) {
                Log.e(LOGTAG, "TTS not accessible");
            }
            Log.e(LOGTAG, "Device not connected to Internet");

        }
    }

    /**
     * Provides feedback to the user to show that the app is listening:
     * * It changes the color and the message of the speech button
     */
    private void changeButtonAppearanceToListening() {
        if (speechButton != null) {
            speechButton.setBackgroundResource(R.drawable.micro_on);
            greenButton = false;
        }
    }

    /**
     * Provides feedback to the user to show that the app is idle:
     * * It changes the color and the message of the speech button
     */
    private void changeButtonAppearanceToDefault() {
        if (speechButton != null) {
            speechButton.setBackgroundResource(R.drawable.micro_off);
            greenButton = true;
        }
    }


    /**
     * Connects to DialogFlow sending the user input in text form
     *
     * @param userInput recognized utterance
     */
    @SuppressLint("StaticFieldLeak")
    private void sendMsgToChatBot(String userInput) {

        //final AIRequest aiRequest = new AIRequest();
        //aiRequest.setQuery(userInput);

        new AsyncTask<String, Void, AIResponse>() {
            /**
             * Connects to the DialogFlow service
             * @param strings Contains the user request
             * @return language understanding result from DialogFlow
             */
            @Override
            protected AIResponse doInBackground(String... strings) {
                final String request = strings[0];
                Log.d(LOGTAG, "RequestBackPre: " + strings[0]);
                try {
                    final AIRequest aiRequest = new AIRequest(request);
                    final AIResponse response = aiDataService.request(aiRequest);
                    Log.d(LOGTAG, "RequestBack: " + aiRequest);
                    Log.d(LOGTAG, "ResponseBack: " + response);


                    return response;
                } catch (AIServiceException e) {
                    try {
                        speak("Could not retrieve a response from DialogFlow", LANGUAGECODE, ID_PROMPT_INFO);
                        Log.e(LOGTAG, "Problems retrieving a response");
                    } catch (Exception ex) {
                        Log.e(LOGTAG, "English not available for TTS, default language used instead");
                    }
                }
                return null;
            }

            /**
             * The semantic parsing is decomposed and the text corresponding to the chatbot
             * response is synthesized
             * @param response parsing corresponding to the output of DialogFlow
             */
            @Override
            protected void onPostExecute(AIResponse response) {
                if (response != null) {
                    // process aiResponse here
                    // More info for a more detailed parsing on the response: https://github.com/dialogflow/dialogflow-android-client/blob/master/apiAISampleApp/src/main/java/ai/api/sample/AIDialogSampleActivity.java

                    final Result result = response.getResult();

                    final String chatbotResponse = result.getFulfillment().getSpeech();
                    final String intentGotten = result.getMetadata().getIntentName();
                    Log.d(LOGTAG, "REspuesta BOT: " + chatbotResponse);

                    if (intentGotten.toLowerCase().equals("hablame de x")) {
                        // Extract parameters of Wikipedia Intent
                        final HashMap<String, JsonElement> params = result.getParameters();
                        if (params != null && !params.isEmpty()) {
                            for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                                if (entry.getKey().equals("any")) {
                                    parameterWikipedia = entry.getValue().toString();

                                    // perform HTTP POST request
                                    if (deviceConnectedToInternet()) {
                                        String url = "https://es.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro&explaintext&redirects=1&titles=" + parameterWikipedia.replaceAll("\"", "");
                                        new MainActivity.HTTPAsyncTask().execute(url);
                                    } else
                                        Toast.makeText(getApplicationContext(), "Not Connected!", Toast.LENGTH_SHORT).show();

                                }
                            }
                        }
                    } else {
                        Log.d(LOGTAG, "ResultPost: " + result.getResolvedQuery());
                        Log.d(LOGTAG, "ActionPost: " + result.getAction());

                        try {
                            speak(chatbotResponse, LANGUAGECODE, ID_PROMPT_QUERY); //It always starts listening after talking, it is neccessary to include a special "last_exchange" intent in dialogflow and process it here
                            //so that the last system answer is synthesized using ID_PROMPT_INFO.
                        } catch (Exception e) {
                            Log.e(LOGTAG, "TTS not accessible");
                        }
                    }
                }
            }
        }.execute(userInput);

    }

    /**
     * Checks whether the device is connected to Internet (returns true) or not (returns false)
     * From: http://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html
     */
    private boolean deviceConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = Objects.requireNonNull(cm).getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnected());
    }

    /*Inner Async Task class to send msg to chat bot */


    /************************Connection with APIs********************/

    /*Inner Async TAsk class to send HTTP POST Request to Wikipedia*/
    @SuppressLint("StaticFieldLeak")
    private class HTTPAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                try {
                    return HttpPost(urls[0]);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "Error!";
                }

            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                Log.i(LOGTAG, "Intentando hablar sobre wikipedia.. " + result);
                if (result.isEmpty())
                    result = "No tengo ninguna información disponible, lo siento";
                speak(result, LANGUAGECODE, ID_PROMPT_INFO);
            } catch (Exception e) {
                Log.e(LOGTAG, "Not WIKIPEDIA speak..");
            }
        }
    }

    private String HttpPost(String myUrl) throws IOException, JSONException {
        StringBuilder result = new StringBuilder();

        URL url = new URL(myUrl);

        // 1. create HttpURLConnection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        // 2. build JSON object
        //JSONObject jsonObject = buildJsonObject();

        // 3. add JSON content to POST request body
        //setPostRequestContent(conn, jsonObject);

        // make POST request to the given URL
        conn.connect();

        // Obtain JSON
        JSONObject paginas = new JSONObject(convertStreamToString(conn.getInputStream())).getJSONObject("query").getJSONObject("pages");

        for (Iterator<String> it = paginas.keys(); it.hasNext(); ) {
            String key = it.next();
            JSONObject page = paginas.getJSONObject(key);
            String extract = page.getString("extract");
            result.append(extract);
        }

        // Contain the first paragraph of Wikipedia extract..
        result = new StringBuilder(result.substring(0, result.indexOf(".") + 1));

        // return response message
        return result.toString();
    }

    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

/*
private JSONObject buildJsonObject() throws JSONException {

//Create json here

return new JSONObject();
}
*/

    private void setPostRequestContent(HttpURLConnection conn,
                                       JSONObject jsonObject) throws IOException {

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(jsonObject.toString());
        Log.i(MainActivity.class.toString(), jsonObject.toString());
        writer.flush();
        writer.close();
        os.close();
    }

    /*********************** END Connection with APIs ********************/
    /* END Voice Methods*/
}
