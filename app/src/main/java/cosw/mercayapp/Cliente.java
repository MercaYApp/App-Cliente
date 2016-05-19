package cosw.mercayapp;

import android.location.Address;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

/**
 * Created by Felipe on 8/5/2016.
 */
public class Cliente {

    private String nombre;
    private String user;
    private String password;
    private JSONArray listaProductos;
    private static Cliente cliente;
    public Cliente(String nombre, String user, String password, JSONArray listaProductos){
        this.setNombre(nombre);
        this.setUser(user);
        this.setPassword(password);
        this.setListaProductos(listaProductos);
    }

    public Cliente(){
        listaProductos=new JSONArray();
    }

    public static Cliente demeDatos(){
        if(getCliente() ==null){
            setCliente(new Cliente());
        }

        return getCliente();
    }

    public static Cliente getCliente() {
        return cliente;
    }

    public static void setCliente(Cliente cliente) {
        Cliente.cliente = cliente;
    }


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public JSONArray getListaProductos() {
        return listaProductos;
    }

    public void setListaProductos(JSONArray listaProductos) {
        this.listaProductos = listaProductos;
    }

    public void eliminarDeListaProductos(String producto) throws JSONException {
        boolean banderita=false;
        JSONArray copiaLista = getListaProductos();
        for(int i=0; i<copiaLista.length() && !banderita; i++){
            if(copiaLista.getJSONObject(i).getString("idProductos").equals(producto)){
                Log.d("Elimino: ", "Quiere elminar:"+ producto);
                copiaLista.remove(0);
                banderita=true;
            }else{
                Log.d("NO elimino: ", "producto");
            }
            Log.d("ENTRO: ", ""+copiaLista.getJSONObject(i));
        }
    }

    public void addListaProductos(JSONObject producto) {
        this.listaProductos.put(producto);
    }
}
