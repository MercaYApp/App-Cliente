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
    private double precio=0;
    private double peso=0;

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
                Log.d("Elimino: ", "Quiere elminar:"+ producto+ " i: "+i);
                copiaLista.remove(i);
                banderita=true;
            }/*else{
                Log.d("NO elimino: ", "producto");
            }*/
        }
        setPeso();
        setPrecio();
    }

    public void addListaProductos(JSONObject producto) {
        setPrecio();
        setPeso();
        this.listaProductos.put(producto);
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio() {
        precio=0;
        JSONArray lista =new JSONArray();
        lista = getListaProductos();
        for(int i=0; i<lista.length(); i++){
            try {
                double compra = lista.getJSONObject(i).getDouble("buyPrice");
                double porcentaje = lista.getJSONObject(i).getDouble("percentage");
                double venta = compra*(1+(porcentaje/100));
                Log.d("CLIENTE precio: ", ""+precio+ " i: "+i);
                precio+= venta;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d("Precio: ", ""+precio);
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso() {
        peso=0;
        JSONArray lista = getListaProductos();
        for(int i=0; i<lista.length(); i++){
            try {
                peso+=lista.getJSONObject(i).getDouble("weight");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
