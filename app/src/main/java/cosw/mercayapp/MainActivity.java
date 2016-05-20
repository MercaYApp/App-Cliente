package cosw.mercayapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by Felipe Gomez on 1/5/2016.
 */
public class MainActivity extends ActionBarActivity implements LocationListener {
    private String user, password;
    private EditText campoUser, campoPassword;
    private Button btnIngresar;
    private JSONObject jo = null;
    private JSONArray ja = null;
    private boolean error = false;
    private Cliente cliente;
    private double latitud;
    private double longitud;
    private boolean ubicado=false;
    private Context context = this;
    private View progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        inicializarCampos();

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1,   // periodo en segundos de actualización
                0, //cambio mínimo en metros para actualizar
                this);

    }

    private void inicializarCampos(){
        campoUser = (EditText)findViewById(R.id.campoUser);
        campoPassword = (EditText)findViewById(R.id.campoPassword);
        btnIngresar = (Button)findViewById(R.id.btnIngresar);
        progress = findViewById(R.id.login_progress);
    }

    /**
     * Accion que se realizara al darle clic en ingresar
     * @param v
     */
    public void ingresar(View v) {
        user = campoUser.getText().toString();
        password = campoPassword.getText().toString();
        try {
            GetUbicacionAsync geoLocalizacion = new GetUbicacionAsync();
            geoLocalizacion.execute();

            buscarCliente(user);
//            mensaje("Usuario: "+user+" \nPassword: "+password);

            cliente=Cliente.demeDatos();
            cliente.setUser(user);
            cliente.setPassword(password);
            /*Intent intent = new Intent(this, GetProducts.class);
            intent.putExtra("user", user);
            intent.putExtra("password", password);
            startActivity(intent);*/
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void mensaje(String mensaje) {
        Toast toast1 = Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT);
        toast1.show();
    }

    /**
     * carga los datos del cliente que esté intentando logearse
     * @param id
     * @throws JSONException
     */
    public void buscarCliente(String id) throws JSONException {
        showProgress(true);
        GetClienteAsync cli = new GetClienteAsync();
        String url = "http://mercayapp1.herokuapp.com/clientsApp/"+id;
        cli.execute(url);
    }

    /**
     * asigna los datos al cliente (singletone )que está logeado
     */
    public void agregarDatosAlCliente(){
        try {
            cliente.setNombre(jo.getString("nameClientApp"));
        } catch (JSONException e) {
            error = true;
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            Log.e(MainActivity.class.toString(),
                    "Login request failed " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitud= location.getLatitude();
        longitud= location.getLongitude();
        ubicado=true;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    /**
     * Hace get del producto en el API
     */
    private class GetClienteAsync extends AsyncTask<String, Integer, JSONObject> {
        protected JSONObject doInBackground(String... url) {
            StringBuilder builder = new StringBuilder();
            try {
                Intent intent = getIntent();
                HttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url[0]);
                httpGet.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(user, password), "UTF-8", false));
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
//                ja = jo.getJSONArray("productses");
            } catch (Exception e){
                error = true;
                e.printStackTrace();
                Log.e(MainActivity.class.toString(),
                        "GET request failed " + e.getLocalizedMessage());
            }
//            return ja;
            return jo;
        }

        protected void onProgressUpdate(Integer... progress) {
            mensaje("Enviando mensaje");
        }

        protected void onPostExecute(JSONObject result) {
            agregarDatosAlCliente();
            if (error == true) {
                mensaje("Error en la autenticación");
            } else {
                Intent intent = new Intent(context, GetProducts.class);
                intent.putExtra("user", user);
                intent.putExtra("password", password);
                startActivity(intent);

                showProgress(false);
                mensaje("Bienvenido!");
            }
        }
    }


    private class GetUbicacionAsync extends AsyncTask<Void, Integer, JSONArray> {
        protected JSONArray doInBackground(Void... nada) {
            StringBuilder builder = new StringBuilder();
            JSONArray tiendasRespuesta = new JSONArray();
            JSONObject tienda = new JSONObject();
            double distancia;
            try {
                //No hace nada hasta que tenga la localizacion
                while(!ubicado) {
                }
                String obj = obj = "http://mercayapp1.herokuapp.com/stores";
                HttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(obj);
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

                ////////////////////////////////////////////////
                Location localizacionActual=new Location("ubicacionCliente");
                localizacionActual.setLatitude(latitud);
                localizacionActual.setLongitude(longitud);

                Location localizacionTiendas=new Location("ubicacionTiendas");

                for(int index=0;index<ja.length();index++){
                    tienda= (JSONObject) ja.get(index);
                    localizacionTiendas.setLatitude(tienda.getDouble("latitud"));
                    localizacionTiendas.setLongitude(tienda.getDouble("longitud"));
                    distancia=localizacionActual.distanceTo(localizacionTiendas);

                    tienda.put("distancia",distancia);
                    tiendasRespuesta.put(tienda);
                }

                tiendasRespuesta=sortArray(tiendasRespuesta);
            } catch (Exception e){
                e.printStackTrace();
                error=true;
                Log.e(GetProducts.class.toString(),
                        "GET request failed " + e.getLocalizedMessage());
            }
            return tiendasRespuesta;
        }

        protected void onProgressUpdate(Integer... progress) {
            mensaje("Enviando mensaje");
        }

        protected void onPostExecute(JSONArray result) {
            try {
                mensaje("ESTÁ EN LA TIENDA: "+result.getJSONObject(0).getString("nameStore"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private JSONArray sortArray(JSONArray jsonArr) throws JSONException {
        JSONArray sortedJsonArray = new JSONArray();

        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < jsonArr.length(); i++) {
            jsonValues.add(jsonArr.getJSONObject(i));
        }
        Collections.sort(jsonValues, new Comparator<JSONObject>() {
            //You can change "Name" with "ID" if you want to sort by ID
            private static final String KEY_NAME = "distancia";

            @Override
            public int compare(JSONObject a, JSONObject b) {

                int resp = 0;

                double valA = 0;
                double valB = 0;

                try {
                    valA = a.getDouble(KEY_NAME);
                    valB = b.getDouble(KEY_NAME);
                    if (valA == valB) {
                        resp = 0;

                    } else if (valA < valB) {
                        resp = -1;
                    } else {
                        resp = 1;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return resp;
            }
        });

        for (int i = 0; i < jsonArr.length(); i++) {
            sortedJsonArray.put(jsonValues.get(i));
        }
        return sortedJsonArray;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            /*
            viewReposterias.setVisibility(show ? View.GONE : View.VISIBLE);
            viewReposterias.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    viewReposterias.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });*/

            progress.setVisibility(show ? View.VISIBLE : View.GONE);
            progress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progress.setVisibility(show ? View.VISIBLE : View.GONE);
            progress.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }



}
