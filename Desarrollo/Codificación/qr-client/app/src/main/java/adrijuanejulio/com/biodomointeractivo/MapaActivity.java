package adrijuanejulio.com.biodomointeractivo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

// Actividad simple en la que mostramos un mapa en el que podemos hacer el gesto de pinch to zoom para aumentar o alejar

public class MapaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

    }
}
