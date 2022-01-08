package adrijuanejulio.com.biodomointeractivo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Collections;

public class ExploraViewActivity extends AppCompatActivity {


    // Arrays donde se almacenarán sólo los nombres, imágenes y textos de la zona que queremos explorar
    private ArrayList<String> specieNames;
    private ArrayList<Integer> specieImages;
    private ArrayList<String> specieTexts;

    // Array donde almacenaremos los índices que usaremos para mostrar las especies. De esta forma podemos mostrarlas de forma aleatoria
    private ArrayList<Integer> shuffleIndices;

    // Botones para pasar a la izquierda, derecha y el botón flotante lector de QR
    private Button leftButton;
    Button rightButton;
    FloatingActionButton floatingQRButton;

    TextView titleTextView;
    ImageView imageView;
    TextView textView;

    // Posición donde nos encontramos
    private int position;

    private Drawable leftButtonDrawable, rightButtonDrawable;


    /**
     * Simulate of a Database of animals and vegetables of Biodomo
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explora_view);

        shuffleIndices = new ArrayList<>();

        floatingQRButton = findViewById(R.id.floatingQRButton);
        //Set up the qr button
        setQrButton();

        position = 0;

        leftButton = findViewById(R.id.left);
        rightButton = findViewById(R.id.right);

        titleTextView = findViewById(R.id.title_image);
        imageView = findViewById(R.id.specieImage);
        textView = findViewById(R.id.animalDescription);

        leftButtonDrawable = getDrawable(R.drawable.button_left);
        rightButtonDrawable = getDrawable(R.drawable.button_right);


        // Insertamos datos según la zona

        if (getIntent().getStringExtra("zone") != null) {
            Log.e("RECIBIENDO INTENT", " ---------------> Viene de la pantalla de zonas");
            // Zona del biodomo que queremos explorar
            String zoneSelected = getIntent().getStringExtra("zone");

            specieNames = new ArrayList<>();
            specieImages = new ArrayList<>();
            specieTexts = new ArrayList<>();

            ImageView topscreen;
            ImageView bgText;

            /* INICIO SIMULACION BASE DE DATOS */
            insertSpecies(zoneSelected);
            /* FIN SIMULACION BASE DE DATOS */

            // Cargamos el color de la interfaz según en qué zona estemos
            switch (zoneSelected) {
                case "0":
                    topscreen = findViewById(R.id.bg_title_explora);
                    topscreen.setImageResource(R.drawable.explora_ama_topscreen);

                    bgText = findViewById(R.id.bg_text_explora);
                    bgText.setImageResource(R.drawable.explora_amaz_button);

                    leftButtonDrawable = getDrawable(R.drawable.explora_button_left_ama);
                    rightButtonDrawable = getDrawable(R.drawable.explora_button_right_ama);

                    break;
                case "1":
                    topscreen = findViewById(R.id.bg_title_explora);
                    topscreen.setImageResource(R.drawable.explora_mad_topscreen);

                    bgText = findViewById(R.id.bg_text_explora);
                    bgText.setImageResource(R.drawable.explora_mad_button);

                    leftButtonDrawable = getDrawable(R.drawable.explora_button_left_mad);
                    rightButtonDrawable = getDrawable(R.drawable.explora_button_right_mad);

                    break;
                case "2":
                    topscreen = findViewById(R.id.bg_title_explora);
                    topscreen.setImageResource(R.drawable.explora_ip_topscreen);

                    bgText = findViewById(R.id.bg_text_explora);
                    bgText.setImageResource(R.drawable.explora_ip_button);

                    leftButtonDrawable = getDrawable(R.drawable.explora_button_left_ip);
                    rightButtonDrawable = getDrawable(R.drawable.explora_button_right_ip);

                    break;
            }

            // Hacemos shuffle a los índices
            for (int i = 0; i < specieImages.size(); i++) {
                shuffleIndices.add(i);
            }

            Collections.shuffle(shuffleIndices);

            titleTextView.setText(specieNames.get(shuffleIndices.get(position)));
            imageView.setImageResource(specieImages.get(shuffleIndices.get(position)));
            textView.setText(specieTexts.get(shuffleIndices.get(position)));

            rightButton.setBackground(rightButtonDrawable);
            leftButton.setBackgroundResource(R.drawable.button_left_right_off);


            // Listener del botón derecho
            rightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (position < specieImages.size() - 1) {
                        position++;

                        int id = specieImages.get(shuffleIndices.get(position));

                        titleTextView.setText(specieNames.get(shuffleIndices.get(position)));

                        imageView.setImageResource(id);

                        textView.setText(specieTexts.get(shuffleIndices.get(position)));

                        if (position == specieImages.size() - 1)
                            rightButton.setBackgroundResource(R.drawable.button_left_right_off);
                        if (position == 1) leftButton.setBackground(leftButtonDrawable);

                    }
                }

            });

            // Listener del botón izquierdo
            leftButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (position > 0) {
                        position--;

                        int id = specieImages.get(shuffleIndices.get(position));

                        titleTextView.setText(specieNames.get(shuffleIndices.get(position)));
                        imageView.setImageResource(id);
                        textView.setText(specieTexts.get(shuffleIndices.get(position)));

                        if (position == 0)
                            leftButton.setBackgroundResource(R.drawable.button_left_right_off);


                        if (position == specieImages.size() - 2)
                            rightButton.setBackground(rightButtonDrawable);
                    }

                }

            });


            // Si obtenemos información a través del QR, desciframos el mensaje y lo cargamos en ExploraView
        } else if (getIntent().getStringExtra("id") != null) {
            Log.e("RECIBIENDO INTENT", " ---------------> Viene de la pantalla del lector QR");
            String id = getIntent().getStringExtra("id");

            rightButton.setVisibility(View.INVISIBLE);
            leftButton.setVisibility(View.INVISIBLE);

            int idTitle = getResources().getIdentifier(id + "_title", "string", getPackageName());
            int idImage = getResources().getIdentifier(id, "drawable", getPackageName());
            int idDesc = getResources().getIdentifier(id + "_text", "string", getPackageName());

            Log.e("ids", idTitle + " " + idImage + " " + idDesc);

            titleTextView.setText(idTitle);
            imageView.setImageResource(idImage);
            textView.setText(idDesc);

            // Si la información viene del lector nfc
        } else if (getIntent().getStringExtra("zone_NFC") != null) {

            int id = Integer.parseInt(getIntent().getStringExtra("zone_NFC"));
            Log.e("RECIBIENDO INTENT", " ---------------> Viene de la pantalla del lector NFC + " + id);
            rightButton.setVisibility(View.INVISIBLE);
            leftButton.setVisibility(View.INVISIBLE);

            switch (id) {
                case 0:
                    titleTextView.setText(R.string.zone_amazonia_title);
                    imageView.setImageResource(R.drawable.nfc_amazonia);
                    textView.setText(R.string.zone_amazonia_desc);
                    break;
                case 1:
                    titleTextView.setText(R.string.zone_madagascar_title);
                    imageView.setImageResource(R.drawable.nfc_madagascar);
                    textView.setText(R.string.zone_madagascar_desc);
                    break;
                case 2:
                    titleTextView.setText(R.string.zone_ip_title);
                    imageView.setImageResource(R.drawable.nfc_indopacifico);
                    textView.setText(R.string.zone_ip_desc);
                    break;
                default:
                    Toast.makeText(getApplicationContext(), "Zona no válida", Toast.LENGTH_SHORT).show();

            }
        } else {
            Toast.makeText(getApplicationContext(), "No es posible abrir Explora View", Toast.LENGTH_SHORT).show();
        }


    }


    /**
     * Initializes the qr button and its listener. When the button is pressed, a qr recognition is enabled.
     */
    private void setQrButton() {
        // gain reference to qr button
        final Activity activity = this;

        floatingQRButton.setOnClickListener(new View.OnClickListener() {
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

    /* Returns results of call intents (use of QR)*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Has cancelado el escaneo", Toast.LENGTH_LONG).show();
            } else {
//
                String contentQR = result.getContents();

                if (contentQR.contains("http")) {
                    Uri web = Uri.parse(contentQR);
                    Intent gotoWeb = new Intent(Intent.ACTION_VIEW, web);
                    startActivity(gotoWeb);
                } else {
                    // Low cifrate
                    if (contentQR.toLowerCase().contains("biodomointeractivo")) {

                        String id = contentQR.substring(contentQR.indexOf(":") + 1);
                        Log.e("ee33", "e33333333e");

                        Intent intent = new Intent(this, ExploraViewActivity.class);
                        intent.putExtra("id", id);
                        startActivity(intent);

                        // Si venimos de leer otro QR, matamos la actividad para no colapsar la memoria
                        if (getIntent().getStringExtra("id") != null) this.finish();
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    /* insertar especies segun la zona */
    private void insertSpecies(String zoneSelected) {

        switch (zoneSelected) {
            case "0":
                // titulos amazonas
                specieNames.add(getResources().getString(R.string.ama_a_0_title));
                specieNames.add(getResources().getString(R.string.ama_a_1_title));
                specieNames.add(getResources().getString(R.string.ama_a_2_title));
                specieNames.add(getResources().getString(R.string.ama_a_3_title));
                specieNames.add(getResources().getString(R.string.ama_a_4_title));
                specieNames.add(getResources().getString(R.string.ama_a_5_title));
                specieNames.add(getResources().getString(R.string.ama_a_6_title));
                specieNames.add(getResources().getString(R.string.ama_a_7_title));
                specieNames.add(getResources().getString(R.string.ama_a_8_title));
                specieNames.add(getResources().getString(R.string.ama_a_9_title));
                specieNames.add(getResources().getString(R.string.ama_a_10_title));
                specieNames.add(getResources().getString(R.string.ama_a_11_title));
                specieNames.add(getResources().getString(R.string.ama_a_12_title));
                specieNames.add(getResources().getString(R.string.ama_a_13_title));
                specieNames.add(getResources().getString(R.string.ama_a_14_title));
                specieNames.add(getResources().getString(R.string.ama_a_15_title));
                specieNames.add(getResources().getString(R.string.ama_a_16_title));
                specieNames.add(getResources().getString(R.string.ama_a_17_title));


                specieNames.add(getResources().getString(R.string.ama_v_0_title));
                specieNames.add(getResources().getString(R.string.ama_v_1_title));
                specieNames.add(getResources().getString(R.string.ama_v_2_title));
                specieNames.add(getResources().getString(R.string.ama_v_3_title));
                specieNames.add(getResources().getString(R.string.ama_v_4_title));
                specieNames.add(getResources().getString(R.string.ama_v_5_title));
                specieNames.add(getResources().getString(R.string.ama_v_6_title));
                specieNames.add(getResources().getString(R.string.ama_v_7_title));
                specieNames.add(getResources().getString(R.string.ama_v_8_title));
                specieNames.add(getResources().getString(R.string.ama_v_9_title));
                specieNames.add(getResources().getString(R.string.ama_v_10_title));
                specieNames.add(getResources().getString(R.string.ama_v_11_title));
                specieNames.add(getResources().getString(R.string.ama_v_12_title));
                specieNames.add(getResources().getString(R.string.ama_v_13_title));
                specieNames.add(getResources().getString(R.string.ama_v_14_title));
                specieNames.add(getResources().getString(R.string.ama_v_15_title));


                // imagenes amazonas
                specieImages.add(R.drawable.ama_a_0);
                specieImages.add(R.drawable.ama_a_1);
                specieImages.add(R.drawable.ama_a_2);
                specieImages.add(R.drawable.ama_a_3);
                specieImages.add(R.drawable.ama_a_4);
                specieImages.add(R.drawable.ama_a_5);
                specieImages.add(R.drawable.ama_a_6);
                specieImages.add(R.drawable.ama_a_7);
                specieImages.add(R.drawable.ama_a_8);
                specieImages.add(R.drawable.ama_a_9);
                specieImages.add(R.drawable.ama_a_10);
                specieImages.add(R.drawable.ama_a_11);
                specieImages.add(R.drawable.ama_a_12);
                specieImages.add(R.drawable.ama_a_13);
                specieImages.add(R.drawable.ama_a_14);
                specieImages.add(R.drawable.ama_a_15);
                specieImages.add(R.drawable.ama_a_16);
                specieImages.add(R.drawable.ama_a_17);


                specieImages.add(R.drawable.ama_v_0);
                specieImages.add(R.drawable.ama_v_1);
                specieImages.add(R.drawable.ama_v_2);
                specieImages.add(R.drawable.ama_v_3);
                specieImages.add(R.drawable.ama_v_4);
                specieImages.add(R.drawable.ama_v_5);
                specieImages.add(R.drawable.ama_v_6);
                specieImages.add(R.drawable.ama_v_7);
                specieImages.add(R.drawable.ama_v_8);
                specieImages.add(R.drawable.ama_v_9);
                specieImages.add(R.drawable.ama_v_10);
                specieImages.add(R.drawable.ama_v_11);
                specieImages.add(R.drawable.ama_v_12);
                specieImages.add(R.drawable.ama_v_13);
                specieImages.add(R.drawable.ama_v_14);
                specieImages.add(R.drawable.ama_v_15);


                // inserts descripciones amazonas
                specieTexts.add(getResources().getString(R.string.ama_a_0_text));
                specieTexts.add(getResources().getString(R.string.ama_a_1_text));
                specieTexts.add(getResources().getString(R.string.ama_a_2_text));
                specieTexts.add(getResources().getString(R.string.ama_a_3_text));
                specieTexts.add(getResources().getString(R.string.ama_a_4_text));
                specieTexts.add(getResources().getString(R.string.ama_a_5_text));
                specieTexts.add(getResources().getString(R.string.ama_a_6_text));
                specieTexts.add(getResources().getString(R.string.ama_a_7_text));
                specieTexts.add(getResources().getString(R.string.ama_a_8_text));
                specieTexts.add(getResources().getString(R.string.ama_a_9_text));
                specieTexts.add(getResources().getString(R.string.ama_a_10_text));
                specieTexts.add(getResources().getString(R.string.ama_a_11_text));
                specieTexts.add(getResources().getString(R.string.ama_a_12_text));
                specieTexts.add(getResources().getString(R.string.ama_a_13_text));
                specieTexts.add(getResources().getString(R.string.ama_a_14_text));
                specieTexts.add(getResources().getString(R.string.ama_a_15_text));
                specieTexts.add(getResources().getString(R.string.ama_a_16_text));
                specieTexts.add(getResources().getString(R.string.ama_a_17_text));


                specieTexts.add(getResources().getString(R.string.ama_v_0_text));
                specieTexts.add(getResources().getString(R.string.ama_v_1_text));
                specieTexts.add(getResources().getString(R.string.ama_v_2_text));
                specieTexts.add(getResources().getString(R.string.ama_v_3_text));
                specieTexts.add(getResources().getString(R.string.ama_v_4_text));
                specieTexts.add(getResources().getString(R.string.ama_v_5_text));
                specieTexts.add(getResources().getString(R.string.ama_v_6_text));
                specieTexts.add(getResources().getString(R.string.ama_v_7_text));
                specieTexts.add(getResources().getString(R.string.ama_v_8_text));
                specieTexts.add(getResources().getString(R.string.ama_v_9_text));
                specieTexts.add(getResources().getString(R.string.ama_v_10_text));
                specieTexts.add(getResources().getString(R.string.ama_v_11_text));
                specieTexts.add(getResources().getString(R.string.ama_v_12_text));
                specieTexts.add(getResources().getString(R.string.ama_v_13_text));
                specieTexts.add(getResources().getString(R.string.ama_v_14_text));
                specieTexts.add(getResources().getString(R.string.ama_v_15_text));
                break;


            case "1":
                //titulos madagascar
                specieNames.add(getResources().getString(R.string.mad_a_0_title));
                specieNames.add(getResources().getString(R.string.mad_a_1_title));
                specieNames.add(getResources().getString(R.string.mad_a_2_title));

                specieNames.add(getResources().getString(R.string.mad_v_0_title));
                specieNames.add(getResources().getString(R.string.mad_v_1_title));
                specieNames.add(getResources().getString(R.string.mad_v_2_title));
                specieNames.add(getResources().getString(R.string.mad_v_3_title));
                specieNames.add(getResources().getString(R.string.mad_v_4_title));
                specieNames.add(getResources().getString(R.string.mad_v_5_title));
                specieNames.add(getResources().getString(R.string.mad_v_6_title));
                specieNames.add(getResources().getString(R.string.mad_v_7_title));
                specieNames.add(getResources().getString(R.string.mad_v_8_title));
                specieNames.add(getResources().getString(R.string.mad_v_9_title));
                specieNames.add(getResources().getString(R.string.mad_v_10_title));
                specieNames.add(getResources().getString(R.string.mad_v_11_title));
                specieNames.add(getResources().getString(R.string.mad_v_12_title));
                specieNames.add(getResources().getString(R.string.mad_v_13_title));
                specieNames.add(getResources().getString(R.string.mad_v_14_title));
                specieNames.add(getResources().getString(R.string.mad_v_15_title));

                //imagenes madagascar
                specieImages.add(R.drawable.mad_a_0);
                specieImages.add(R.drawable.mad_a_1);
                specieImages.add(R.drawable.mad_a_2);

                specieImages.add(R.drawable.mad_v_0);
                specieImages.add(R.drawable.mad_v_1);
                specieImages.add(R.drawable.mad_v_2);
                specieImages.add(R.drawable.mad_v_3);
                specieImages.add(R.drawable.mad_v_4);
                specieImages.add(R.drawable.mad_v_5);
                specieImages.add(R.drawable.mad_v_6);
                specieImages.add(R.drawable.mad_v_7);
                specieImages.add(R.drawable.mad_v_8);
                specieImages.add(R.drawable.mad_v_9);
                specieImages.add(R.drawable.mad_v_10);
                specieImages.add(R.drawable.mad_v_11);
                specieImages.add(R.drawable.mad_v_12);
                specieImages.add(R.drawable.mad_v_13);
                specieImages.add(R.drawable.mad_v_14);
                specieImages.add(R.drawable.mad_v_15);


                // inserts descripciones madagascar
                specieTexts.add(getResources().getString(R.string.mad_a_0_text));
                specieTexts.add(getResources().getString(R.string.mad_a_1_text));
                specieTexts.add(getResources().getString(R.string.mad_a_2_text));


                specieTexts.add(getResources().getString(R.string.mad_v_0_text));
                specieTexts.add(getResources().getString(R.string.mad_v_1_text));
                specieTexts.add(getResources().getString(R.string.mad_v_2_text));
                specieTexts.add(getResources().getString(R.string.mad_v_3_text));
                specieTexts.add(getResources().getString(R.string.mad_v_4_text));
                specieTexts.add(getResources().getString(R.string.mad_v_5_text));
                specieTexts.add(getResources().getString(R.string.mad_v_6_text));
                specieTexts.add(getResources().getString(R.string.mad_v_7_text));
                specieTexts.add(getResources().getString(R.string.mad_v_8_text));
                specieTexts.add(getResources().getString(R.string.mad_v_9_text));
                specieTexts.add(getResources().getString(R.string.mad_v_10_text));
                specieTexts.add(getResources().getString(R.string.mad_v_11_text));
                specieTexts.add(getResources().getString(R.string.mad_v_12_text));
                specieTexts.add(getResources().getString(R.string.mad_v_13_text));
                specieTexts.add(getResources().getString(R.string.mad_v_14_text));
                specieTexts.add(getResources().getString(R.string.mad_v_15_text));
                break;

            case "2":

                //titulos indopacifico
                specieNames.add(getResources().getString(R.string.ip_a_0_title));
                specieNames.add(getResources().getString(R.string.ip_a_1_title));
                specieNames.add(getResources().getString(R.string.ip_a_2_title));
                specieNames.add(getResources().getString(R.string.ip_a_3_title));
                specieNames.add(getResources().getString(R.string.ip_a_4_title));
                specieNames.add(getResources().getString(R.string.ip_a_5_title));
                specieNames.add(getResources().getString(R.string.ip_a_6_title));
                specieNames.add(getResources().getString(R.string.ip_a_7_title));
                specieNames.add(getResources().getString(R.string.ip_a_8_title));
                specieNames.add(getResources().getString(R.string.ip_a_9_title));
                specieNames.add(getResources().getString(R.string.ip_a_10_title));
                specieNames.add(getResources().getString(R.string.ip_a_11_title));
                specieNames.add(getResources().getString(R.string.ip_a_12_title));
                specieNames.add(getResources().getString(R.string.ip_a_13_title));
                specieNames.add(getResources().getString(R.string.ip_a_14_title));
                specieNames.add(getResources().getString(R.string.ip_a_15_title));


                specieNames.add(getResources().getString(R.string.ip_v_0_title));
                specieNames.add(getResources().getString(R.string.ip_v_1_title));
                specieNames.add(getResources().getString(R.string.ip_v_2_title));
                specieNames.add(getResources().getString(R.string.ip_v_3_title));
                specieNames.add(getResources().getString(R.string.ip_v_4_title));
                specieNames.add(getResources().getString(R.string.ip_v_5_title));
                specieNames.add(getResources().getString(R.string.ip_v_6_title));
                specieNames.add(getResources().getString(R.string.ip_v_7_title));
                specieNames.add(getResources().getString(R.string.ip_v_8_title));
                specieNames.add(getResources().getString(R.string.ip_v_9_title));
                specieNames.add(getResources().getString(R.string.ip_v_10_title));
                specieNames.add(getResources().getString(R.string.ip_v_11_title));
                specieNames.add(getResources().getString(R.string.ip_v_12_title));
                specieNames.add(getResources().getString(R.string.ip_v_13_title));
                specieNames.add(getResources().getString(R.string.ip_v_14_title));
                specieNames.add(getResources().getString(R.string.ip_v_15_title));


                //imagenes indo pacifico
                specieImages.add(R.drawable.ip_a_0);
                specieImages.add(R.drawable.ip_a_1);
                specieImages.add(R.drawable.ip_a_2);
                specieImages.add(R.drawable.ip_a_3);
                specieImages.add(R.drawable.ip_a_4);
                specieImages.add(R.drawable.ip_a_5);
                specieImages.add(R.drawable.ip_a_6);
                specieImages.add(R.drawable.ip_a_7);
                specieImages.add(R.drawable.ip_a_8);
                specieImages.add(R.drawable.ip_a_9);
                specieImages.add(R.drawable.ip_a_10);
                specieImages.add(R.drawable.ip_a_11);
                specieImages.add(R.drawable.ip_a_12);
                specieImages.add(R.drawable.ip_a_13);
                specieImages.add(R.drawable.ip_a_14);
                specieImages.add(R.drawable.ip_a_15);


                specieImages.add(R.drawable.ip_v_0);
                specieImages.add(R.drawable.ip_v_1);
                specieImages.add(R.drawable.ip_v_2);
                specieImages.add(R.drawable.ip_v_3);
                specieImages.add(R.drawable.ip_v_4);
                specieImages.add(R.drawable.ip_v_5);
                specieImages.add(R.drawable.ip_v_6);
                specieImages.add(R.drawable.ip_v_7);
                specieImages.add(R.drawable.ip_v_8);
                specieImages.add(R.drawable.ip_v_9);
                specieImages.add(R.drawable.ip_v_10);
                specieImages.add(R.drawable.ip_v_11);
                specieImages.add(R.drawable.ip_v_12);
                specieImages.add(R.drawable.ip_v_13);
                specieImages.add(R.drawable.ip_v_14);
                specieImages.add(R.drawable.ip_v_15);


                // descripciones indo pacifico

                specieTexts.add(getResources().getString(R.string.ip_a_0_text));
                specieTexts.add(getResources().getString(R.string.ip_a_1_text));
                specieTexts.add(getResources().getString(R.string.ip_a_2_text));
                specieTexts.add(getResources().getString(R.string.ip_a_3_text));
                specieTexts.add(getResources().getString(R.string.ip_a_4_text));
                specieTexts.add(getResources().getString(R.string.ip_a_5_text));
                specieTexts.add(getResources().getString(R.string.ip_a_6_text));
                specieTexts.add(getResources().getString(R.string.ip_a_7_text));
                specieTexts.add(getResources().getString(R.string.ip_a_8_text));
                specieTexts.add(getResources().getString(R.string.ip_a_9_text));
                specieTexts.add(getResources().getString(R.string.ip_a_10_text));
                specieTexts.add(getResources().getString(R.string.ip_a_11_text));
                specieTexts.add(getResources().getString(R.string.ip_a_12_text));
                specieTexts.add(getResources().getString(R.string.ip_a_13_text));
                specieTexts.add(getResources().getString(R.string.ip_a_14_text));
                specieTexts.add(getResources().getString(R.string.ip_a_15_text));


                specieTexts.add(getResources().getString(R.string.ip_v_0_text));
                specieTexts.add(getResources().getString(R.string.ip_v_1_text));
                specieTexts.add(getResources().getString(R.string.ip_v_2_text));
                specieTexts.add(getResources().getString(R.string.ip_v_3_text));
                specieTexts.add(getResources().getString(R.string.ip_v_4_text));
                specieTexts.add(getResources().getString(R.string.ip_v_5_text));
                specieTexts.add(getResources().getString(R.string.ip_v_6_text));
                specieTexts.add(getResources().getString(R.string.ip_v_7_text));
                specieTexts.add(getResources().getString(R.string.ip_v_8_text));
                specieTexts.add(getResources().getString(R.string.ip_v_9_text));
                specieTexts.add(getResources().getString(R.string.ip_v_10_text));
                specieTexts.add(getResources().getString(R.string.ip_v_11_text));
                specieTexts.add(getResources().getString(R.string.ip_v_12_text));
                specieTexts.add(getResources().getString(R.string.ip_v_13_text));
                specieTexts.add(getResources().getString(R.string.ip_v_14_text));
                specieTexts.add(getResources().getString(R.string.ip_v_15_text));

                break;


        }


    }


}
