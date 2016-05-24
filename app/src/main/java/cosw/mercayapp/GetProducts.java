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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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
 * Created by Felipe Gomez on 30/4/2016.
 */
public class GetProducts extends ActionBarActivity {
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client2;
    private TableLayout tl;
    private JSONArray ja = null;
    private JSONObject jo = null;
    private Button scanBtn, btnAddCarrito;
    private double totalVenta, totalPeso = 0;
    private String idProducto = "";
    private boolean error = false;
    private Cliente cliente;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_products);
        client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        //traer cliente Singleton
        cliente=Cliente.demeDatos();
//        mensaje(""+cliente.getUser() + " PASSWORD:  "+cliente.getPassword());
        inicializarCampos();
    }

    private void inicializarCampos(){
        //Se Instancia el botón de Scan
        scanBtn = (Button)findViewById(R.id.scanButton);
        btnAddCarrito = (Button)findViewById(R.id.btnAddCarrito);
    }

    /**
     * Crear estructura de la tabla
     * @throws JSONException
     */
    private void crearTabla() throws JSONException {
        totalVenta=0;
        totalPeso = 0;
        TableLayout t1 = null;
        tl = (TableLayout) findViewById(R.id.main_table);
        tl.removeViews(1, tl.getChildCount()-1); //Eliminar anterior consulta de producto
        TableRow tr_head = new TableRow(this);
        tr_head.setId(new Integer(0));
        tr_head.setBackgroundColor(Color.GRAY);
        tr_head.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        TextView label_nombre = new TextView(this);
        label_nombre.setId(new Integer(1));// define id that must be unique
        label_nombre.setText("NOMBRE"); // set the text for the header
        label_nombre.setTextColor(Color.WHITE); // set the color
        label_nombre.setTypeface(null, Typeface.BOLD);
        label_nombre.setPadding(5, 5, 5, 5); // set the padding (if required)
        tr_head.addView(label_nombre); // add the column to the table row here

        TextView label_precio = new TextView(this);
        label_precio.setId(new Integer(2));// define id that must be unique
        label_precio.setText("PRECIO"); // set the text for the header
        label_precio.setTextColor(Color.WHITE); // set the color
        label_precio.setTypeface(null, Typeface.BOLD);
        label_precio.setPadding(5, 5, 5, 5); // set the padding (if required)
        tr_head.addView(label_precio); // add the column to the table row here

        TextView label_peso = new TextView(this);
        label_peso.setId(new Integer(3));
        label_peso.setText("PESO");
        label_peso.setTextColor(Color.WHITE);
        label_peso.setTypeface(null, Typeface.BOLD);
        label_peso.setPadding(5, 5, 5, 5);
        tr_head.addView(label_peso);// add the column to the table row here

        tl.addView(tr_head, new TableLayout.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

    }

    private void poblarTablaProducto() throws JSONException {

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
//            if(i%2!=0) tr.setBackgroundColor(Color.parseColor("#40BBCB"));
//            tr.setId(new Integer(200+i));
            tr.setId(new Integer(200));


            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TextView labelNombre = new TextView(this);
            labelNombre.setId(new Integer(201));
            labelNombre.setText(nombre);
            tr.addView(labelNombre);

            TextView labelPrecio = new TextView(this);
            labelPrecio.setId(new Integer(202));
            labelPrecio.setText(precioVenta+"");
            tr.addView(labelPrecio);

            TextView labelId = new TextView(this);
            labelId.setId(new Integer(203));
            labelId.setText(peso+"");
            labelId.setPadding(2, 0, 5, 0);
            tr.addView(labelId);

            // finally add this to the table row
            tl.addView(tr, new TableLayout.LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

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
     * Escanear producto
     * @param v
     */
    public void onClick(View v){
        //Se responde al evento click
        if(v.getId()==R.id.scanButton){
            //Se instancia un objeto de la clase IntentIntegrator
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            //Se procede con el proceso de scaneo
            scanIntegrator.initiateScan();
        }
    }

    /**
     * Agregar al carrito de compras del cliente
     * @param v
     */
    public void agregarAlCarrito(View v){
        if(jo!=null) {
            cliente.addListaProductos(jo);
            mensaje("Se ha agregado correctamente al carrito");
//            mensaje(cliente.getListaProductos().toString());
        }else{
            mensaje("No hay productos para agregar al carrito");
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //Se obtiene el resultado del proceso de scaneo y se parsea
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            //Quiere decir que se obtuvo resultado pro lo tanto:
            //Desplegamos en pantalla el contenido del código de barra scaneado
            String scanContent = scanningResult.getContents();
            idProducto= scanContent;
            try {
                error = false;
                buscarProducto(idProducto);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else{
            //Quiere decir que NO se obtuvo resultado
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No se ha recibido datos del scaneo!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Buscar en el API la producto con el id escaneado del código de barras
     * @param id
     * @throws JSONException
     */
    public void buscarProducto(String id) throws JSONException {
        tl = null;
        jo = null;
        GetProductoAsync producto = new GetProductoAsync();
        String url = "http://mercayapp1.herokuapp.com/products/"+id;
        producto.execute(url);
        if(error==true){
            mensaje("No se ha encontrado producto");
            //Intent i = new Intent(this, MainActivity.class);
            //startActivity(i);
        }
    }

    /**
     * Hace get del producto en el API
     */
    private class GetProductoAsync extends AsyncTask<String, Integer, JSONObject> {
        protected JSONObject doInBackground(String... url) {
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
                jo = new JSONObject(builder.toString());
                //ja = jo.getJSONArray("productses");
            } catch (Exception e){
                e.printStackTrace();
                error=true;
                Log.e(GetProducts.class.toString(),
                        "GET request failed " + e.getLocalizedMessage());
            }
            return jo;
            //return ja;
        }

        protected void onProgressUpdate(Integer... progress) {
            mensaje("Enviando mensaje");
        }

        protected void onPostExecute(JSONObject result) {
            try {
                if(jo!=null) {
                    crearTabla();
                    poblarTablaProducto();
                    mensaje("Ya se cargaron todos los productos del producto!");
                }else {
                    crearTabla();
                    mensaje("NO SE HA ENCONTRADO PRODUCTOoo");

                }

            } catch (JSONException e) {
                mensaje("NO SE HA ENCONTRADO EL PRODUCTO");
                Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                v.vibrate(3000);
                e.printStackTrace();
            }
        }
    }

    private void mensaje(String mensaje) {
        Toast toast1 = Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT);
        toast1.show();
    }

}
