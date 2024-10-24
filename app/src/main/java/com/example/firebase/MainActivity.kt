package com.example.firebase

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {
    private lateinit var txtN: EditText
    private lateinit var txtDe: EditText
    private lateinit var btnRegis: Button
    private lateinit var listaV: ListView

    private lateinit var listaData:MutableList<String>
    private lateinit var listaIds:MutableList<String>
    private lateinit var listaAdapter: ArrayAdapter<String>

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firetorage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        cargarR()
        dataEvenFirestore()
    }
    fun cargarR(){
        txtN = findViewById(R.id.txtNombreCategoria)
        txtDe = findViewById(R.id.txtDescripcionCategoria)
        btnRegis = findViewById(R.id.btnRegistrarCategoria)
        listaV = findViewById(R.id.listaCategoria)
    }

    fun dataEvenFirestore(){
        listaData = ArrayList()
        listaIds = ArrayList()
        listaAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaData)
        listaV.adapter = listaAdapter

        //vento
        btnRegis.setOnClickListener { addProduct() }
        listaV.setOnItemClickListener { _, _, i, _ ->  removeProduct(i) }


        firestore = Firebase.firestore
        loadProduct()


    }
    fun loadProduct(){
        btnRegis.isEnabled = false
        firestore.collection("producto")
            .get()
            .addOnSuccessListener { result ->
                for (document in result){
                    val category_data = document.getString("nombre")
                    category_data?.let {
                        listaData.add(it)
                        listaIds.add(document.id)
                    }
                }
                //refesh
                listaAdapter.notifyDataSetChanged()
                btnRegis.isEnabled = true

            }
            .addOnFailureListener {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                btnRegis.isEnabled = true
            }

    }

    fun addProduct(){
        val categoN = txtN.text.toString()
        val categoD = txtDe.text.toString()
        if(categoN.isNotBlank() && categoD.isNotBlank()){
            btnRegis.isEnabled = false
            val pair = hashMapOf("nombre" to categoN, "descripcion" to categoD)
            firestore.collection("producto")
                .add(pair)
                .addOnSuccessListener { documentReference ->
                    val docId = documentReference.id
                    if (docId != null) {
                        // Verificamos si el ID se obtiene correctamente
                        listaIds.add(docId) // Almacenamos el ID
                        listaData.add(categoN)
                        listaAdapter.notifyDataSetChanged()

                        // Limpiamos los campos de entrada
                        txtN.text.clear()
                        txtDe.text.clear()
                        Toast.makeText(this, listaIds.toString(), Toast.LENGTH_SHORT).show()
                        Toast.makeText(this, listaData.toString(), Toast.LENGTH_SHORT).show()
                        // Habilitamos el botón
                        btnRegis.isEnabled = true
                    } else {
                        Toast.makeText(this, "Error: No se pudo obtener el ID del documento", Toast.LENGTH_SHORT).show()
                        btnRegis.isEnabled = true
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                    btnRegis.isEnabled = true
                }
        }
    }

    fun removeProduct(i: Int){
        btnRegis.isEnabled = false
        val documentId =  listaIds[i]
        firestore.collection("producto")
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                listaData.removeAt(i)
                listaIds.removeAt(i)
                listaAdapter.notifyDataSetChanged()
                btnRegis.isEnabled = true
            }
            .addOnFailureListener {
                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()
                btnRegis.isEnabled = true
            }
    }
}