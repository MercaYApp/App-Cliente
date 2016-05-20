package cosw.mercayapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
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
public class GetFacturas  extends ActionBarActivity {

    private TableLayout tl;
    private JSONArray ja = null;
    private GoogleApiClient client2;
    private Cliente cliente;
    private boolean error = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_facturas);
        client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        //traer cliente Singleton
        cliente=Cliente.demeDatos();
//        mensaje(""+cliente.getUser() + " PASSWORD:  "+cliente.getPassword());

        try {
            buscarFacturas(cliente.getUser());
        } catch (JSONException e) {
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

    /**
     * Crear estructura de la tabla
     * @throws JSONException
     */
    private void crearTabla() throws JSONException {
        TableLayout t1 = null;
        tl = (TableLayout) findViewById(R.id.tableFacturas);
//        tl.removeViews(0, tl.getChildCount()-1); //Eliminar anterior consulta de factura
        TableRow tr_head = new TableRow(this);
        tr_head.setId(new Integer(0));
        tr_head.setBackgroundColor(Color.GRAY);
        tr_head.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        TextView label_Id= new TextView(this);
        label_Id.setId(new Integer(2));// define id that must be unique
        label_Id.setText("ID FACTURA"); // set the text for the header
        label_Id.setTextColor(Color.WHITE); // set the color
        label_Id.setTypeface(null, Typeface.BOLD);
        label_Id.setPadding(5, 5, 5, 5); // set the padding (if required)
        tr_head.addView(label_Id); // add the column to the table row here

        TextView label_Fecha = new TextView(this);
        label_Fecha.setId(new Integer(2));// define id that must be unique
        label_Fecha.setText("FECHA"); // set the text for the header
        label_Fecha.setTextColor(Color.WHITE); // set the color
        label_Fecha.setTypeface(null, Typeface.BOLD);
        label_Fecha.setPadding(5, 5, 5, 5); // set the padding (if required)
        tr_head.addView(label_Fecha); // add the column to the table row here

        tl.addView(tr_head, new TableLayout.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

    }

    private void poblarTabla() throws JSONException {

        for (int i= 0; i<ja.length(); i++) {
            //Crear el objeto json extraido
            final JSONObject jo = (JSONObject)ja.getJSONObject(i);
            String id = jo.getString("idInvoices");
            String fecha = jo.getString("dateInvoice");

            // Create the table row
            TableRow tr = new TableRow(this);
            if(i%2!=0) tr.setBackgroundColor(Color.parseColor("#40BBCB"));
            tr.setId(new Integer(200+i));
            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    1));

            TextView labelId = new TextView(this);
            labelId.setId(new Integer(300+i));
            labelId.setText(id);
            tr.addView(labelId);

            TextView labelDate = new TextView(this);
            labelDate.setId(new Integer(400+i));
            labelDate.setText(fecha);
            tr.setPadding(20,20,20,20);
            tr.addView(labelDate);

            tr.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    try {
                        crearCodigoDeBarras(jo.getString("idInvoices"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

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
     * @param id
     * @throws JSONException
     */
    public void buscarFacturas(String id) throws JSONException {
        tl = null;
        GetFacturasAsync factura = new GetFacturasAsync();
        String url = "http://mercayapp1.herokuapp.com/clientsApp/"+id+"/invoices/";
        factura.execute(url);
        if(error==true){
            mensaje("Error en la autenticación");
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }
    }

    /**
     * Hace get de las facturas del cliente en el API
     */
    private class GetFacturasAsync extends AsyncTask<String, Integer, JSONArray> {
        protected JSONArray doInBackground(String... url) {
            StringBuilder builder = new StringBuilder();

            try {
                Intent intent = getIntent();
                HttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url[0]);
                httpGet.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(cliente.getUser(), cliente.getPassword()), "UTF-8", false));
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(content));
                String line;

                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                ja = new JSONArray(builder.toString());
            } catch (Exception e){
                e.printStackTrace();
                error=true;
                Log.e(GetProducts.class.toString(),
                        "GET request failed " + e.getLocalizedMessage());
            }
            return ja;
        }

        protected void onProgressUpdate(Integer... progress) {
            mensaje("Enviando mensaje");
        }

        protected void onPostExecute(JSONArray result) {
            try {
                if(ja!=null) {
                    crearTabla();
                    poblarTabla();
                    mensaje("Ya se cargaron todas las facturas!");
                }else {
                    mensaje("NO SE HAN ENCONTRADO FACTURAS");
                    Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    v.vibrate(3000);
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
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

}
