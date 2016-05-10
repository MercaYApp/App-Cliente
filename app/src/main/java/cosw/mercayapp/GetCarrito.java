package cosw.mercayapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Felipe Brasil on 9/5/2016.
 */
public class GetCarrito extends ActionBarActivity {

    private TableLayout tl;
    private JSONArray ja = null;
    private JSONObject jo = null;
    private GoogleApiClient client2;
    private Cliente cliente;
    private double totalVenta, totalPeso = 0;
    private boolean error = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_facturas);
        client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        //traer cliente Singleton
        cliente=Cliente.demeDatos();
        try {
            buscarCarrito();
            crearTabla();
            poblarTabla();
            mensaje("HA POBLADO CORRECTAMENTE");
        } catch (JSONException e) {
            mensaje("ERRORRRRRRRRRRR");
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.escanearProducto) {
            Intent intent = new Intent(this, GetProducts.class);
            startActivity(intent);
        }
        if (id == R.id.inicio) {
            cliente.setNombre("");
            cliente.setPassword("");
            cliente.setUser("");
            cliente.setListaProductos(new JSONArray());
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        if(id == R.id.consultarFacturas){
            Intent intent = new Intent(this, GetFacturas.class);
            startActivity(intent);
        }
        if (id == R.id.consultarCarrito) {
            Intent intent = new Intent(this, GetCarrito.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "GetProducts Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://cosw.mercayapp/http/host/path")
        );
        AppIndex.AppIndexApi.start(client2, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "GetProducts Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://cosw.mercayapp/http/host/path")
        );
        AppIndex.AppIndexApi.end(client2, viewAction);
        client2.disconnect();
    }

    public void pagar(){
        mensajeAlerta("Pagar", "¿Seguro desea pagar?");
    }

    /**
     * Crear estructura de la tabla
     * @throws JSONException
     */
    private void crearTabla() throws JSONException {
        totalVenta=0;
        totalPeso = 0;
        TableLayout t1 = null;
        tl = (TableLayout) findViewById(R.id.tableFacturas);
        tl.removeAllViews(); //Elimina tablos para que no se muestren repetidos
        TableRow tr_head = new TableRow(this);
        tr_head.setId(new Integer(0));
        tr_head.setBackgroundColor(Color.GRAY);
        tr_head.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        Button btnPagar = new Button(this);
        btnPagar.setId(new Integer(2));// define id that must be unique
        btnPagar.setText("PAGAR"); // set the text for the header
        btnPagar.setTextColor(Color.WHITE); // set the color
        btnPagar.setTypeface(null, Typeface.BOLD);
        btnPagar.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, 60));
        btnPagar.setTextSize(20);
        btnPagar.setPadding(5, 5, 5, 5); // set the padding (if required)
        btnPagar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pagar();
            }
        });
        tl.addView(btnPagar);

        TextView label_Fecha = new TextView(this);
        label_Fecha.setId(new Integer(2));// define id that must be unique
        label_Fecha.setText("NOMBRE"); // set the text for the header
        label_Fecha.setTextColor(Color.WHITE); // set the color
        label_Fecha.setTypeface(null, Typeface.BOLD);
        label_Fecha.setPadding(5, 5, 5, 5); // set the padding (if required)
        tr_head.addView(label_Fecha); // add the column to the table row here

        TextView label_precio = new TextView(this);
        label_precio.setId(new Integer(3));// define id that must be unique
        label_precio.setText("PRECIO"); // set the text for the header
        label_precio.setTextColor(Color.WHITE); // set the color
        label_precio.setTypeface(null, Typeface.BOLD);
        label_precio.setPadding(5, 5, 5, 5); // set the padding (if required)
        tr_head.addView(label_precio); // add the column to the table row here

        TextView label_peso = new TextView(this);
        label_peso.setId(new Integer(4));// define id that must be unique
        label_peso.setText("PESO"); // set the text for the header
        label_peso.setTextColor(Color.WHITE); // set the color
        label_peso.setTypeface(null, Typeface.BOLD);
        label_peso.setPadding(5, 5, 5, 5); // set the padding (if required)
        tr_head.addView(label_peso); // add the column to the table row here

        tl.addView(tr_head, new TableLayout.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

    }

    private void poblarTabla() throws JSONException {
        for (int i= 0; i<ja.length(); i++) {
            //Crear el objeto json extraido
            jo = (JSONObject)ja.getJSONObject(i);
            String id = jo.getString("idProductos");
            String nombre = jo.getString("nameProduct");
            int precio = jo.getInt("buyPrice");
            int peso = jo.getInt("weight");
            totalPeso+=peso;
            double porcentaje = jo.getDouble("percentage")/100;
            double precioVenta = precio*(1+porcentaje);
            totalVenta+=precioVenta;

            // Create the table row
            TableRow tr = new TableRow(this);
            if(i%2!=0) tr.setBackgroundColor(Color.parseColor("#40BBCB"));
            tr.setId(new Integer(200+i));
            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    1));

            TextView labelDate = new TextView(this);
            labelDate.setId(new Integer(400+i));
            labelDate.setText(nombre);
            tr.addView(labelDate);

            TextView labelPrecio = new TextView(this);
            labelPrecio.setId(new Integer(500+i));
            labelPrecio.setText(precioVenta+"");
            tr.addView(labelPrecio);

            TextView labelPeso = new TextView(this);
            labelPeso.setId(new Integer(600+i));
            labelPeso.setText(peso+"");
            tr.addView(labelPeso);

            // finally add this to the table row
            tl.addView(tr, new TableLayout.LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
        }
    }


    private void crearCodigoDeBarras(String id){
        Intent i = new Intent(this, TestBarCode.class);
        i.putExtra("codigo", id);
        startActivity(i);
    }

    /**
     * Buscar en el API la facturas con el id del cliente logeado
     * @throws JSONException
     */
    public void buscarCarrito() throws JSONException {
        tl = null;
        ja=cliente.getListaProductos();
        mensaje(ja.toString());
        if(error==true){
            mensaje("Error en la autenticación");
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }
    }


    /**
     * Mostrar mensajes informativos como alertas
     * @param mensaje
     */
    private void mensaje(String mensaje) {
        Toast toast1 = Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT);
        toast1.show();
    }

    //MOSTRAR MENSAJES
    private  void mensajeAlerta(String msjTit, String msj){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(""+msjTit);
        alertDialog.setMessage(""+msj);

        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mensaje("ACEPTO Aqui manda a la nueva actividad donde paga y genera factura");
                //Aqui deberia pagar y luego hacer el post de los productos, para generar una factura

            }
        });
        alertDialog.setButton(Dialog.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mensaje("CANCELO");
            }
        });
        alertDialog.show();
    }


}
