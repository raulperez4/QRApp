package adrijuanejulio.com.biodomointeractivo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ExploraActivity extends AppCompatActivity {

    // Botones para abrir cada zona del biodomo
    private Button amaButton;
    private Button madButton;
    private Button ipButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explora);


        // Asignamos los botones y activamos el listener
        amaButton = findViewById(R.id.ama_button);
        madButton = findViewById(R.id.mad_button);
        ipButton = findViewById(R.id.ip_button);

        setAmaButton();
        setMadButton();
        setIpButton();
    }



    /**
     * Initializes the Amazonia button and its listener.
     */
    private void setAmaButton() {
       amaButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.e("PRUEBA", " ---------------> Lanzando activity amazonia");
                launchAmaActivity();
            }

        });
    }

    /* Launch Amazonia activity*/
    private void launchAmaActivity(){
        Intent intent = new Intent(this, ExploraViewActivity.class);
        // Código para cada zona del biodomo
        String ZONE_AMAZONIA = "0";
        intent.putExtra("zone", ZONE_AMAZONIA);
        startActivity(intent);
    }


    /**
     * Initializes the Madagascar button and its listener.
     */
    private void setMadButton() {
        madButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.e("PRUEBA", " ---------------> Lanzando activity madagascar");
                launchMadActivity();
            }

        });
    }

    /* Launch madagascar activity*/
    private void launchMadActivity(){
        Intent intent = new Intent(this, ExploraViewActivity.class);
        String ZONE_MADAGASCAR = "1";
        intent.putExtra("zone", ZONE_MADAGASCAR);
        startActivity(intent);
    }

    /**
     * Initializes the indopacifico button and its listener.
     */
    private void setIpButton() {
        ipButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.e("PRUEBA", " ---------------> Lanzando activity indo");
                launchIpActivity();
            }

        });
    }

    /* Launch madagascar activity*/
    private void launchIpActivity(){
        Intent intent = new Intent(this, ExploraViewActivity.class);
        String ZONE_INDOPACIFICO = "2";
        intent.putExtra("zone", ZONE_INDOPACIFICO);
        startActivity(intent);
    }






}
