package com.example.mobile_smart_pantry_project_iv

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.mobile_smart_pantry_project_iv.databinding.ActivityMainBinding
import com.example.mobile_smart_pantry_project_iv.model.Product
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private var inventoryList = mutableListOf<Product>()

    private var selectedListElement = -1

    private var filteredProducts = mutableListOf<Product>() // var aby można było jednocześnie filtrować po kategorii i nazwie
    private var selectedCategory = "All"
    private var filterText = ""


    private fun dataParser() {
        try {
            val jsonString = try {
                openFileInput("pantry.json").bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                resources.openRawResource(R.raw.pantry)
                    .bufferedReader()
                    .use { it.readText() }
            }

            val json = Json {
                ignoreUnknownKeys = true
                prettyPrint = true // ładne formatowanie JSON
            }
            val loadedList = json.decodeFromString<List<Product>>(jsonString)

            inventoryList.clear()
            inventoryList.addAll(loadedList)
            filteredProducts = inventoryList

        } catch (e: Exception) {
            Toast.makeText(this, "File read error!", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        dataParser()

        Log.v("inventoryList", inventoryList.toString())


//---------------------------------------|     Przypisanie produktów do listy z użyciem adaptera     |---------------------------------------------------------------------------------------------
        val productsListView : ListView = binding.productListView
        val productsAdapter = PantryAdapter(this, inventoryList)

        productsListView.adapter = productsAdapter
        productsListView.setOnItemClickListener { _, _, position, _ ->
            productsListView.setItemChecked(position, true)
            selectedListElement = position
        }


        // -------------------|     Funkcja filtrująca    |-----------------------------------------------------------------------------------------------------------------

        fun filterProducts(){
            filteredProducts = inventoryList.filter { product ->
                val categoryFilter = selectedCategory == "All" || product.Category == selectedCategory // jeśli wybrano all to zwraca true, jeśli nie to sprawdza jakie produkty mają tą kategorie (tak: true | nie: false)

                val nameMatch = product.Name.contains(filterText, ignoreCase = true) // zwraca true jeśli produkt zawiera dany fragment tekstu

                categoryFilter && nameMatch // produkt zostanie dodany jeśli obie zmienne mają true
            }.toMutableList() //aby typy się zgadzały, filter zwraca listOf a  filtered products jest mutableListOf

            productsListView.adapter = PantryAdapter(this, filteredProducts)

        }


//------------------------ |      Usuwanie elementów z listy      |------------------------------------------------------------------------------------------------------------

        binding.deleteBtn.setOnClickListener{
            if (selectedListElement == -1)
            {
                Toast.makeText(this, "Najpierw zaznacz element", Toast.LENGTH_SHORT).show()
            }
            else{
                val productToRemove = filteredProducts[selectedListElement] // pobiera zaznaczony element przy filtrowaniu np. Drill
                inventoryList.remove(productToRemove) // Usuwa konkretnie Drill a nie np. produkt o indeksie [2]
                filterProducts() // aktualizacja widoku po usunięciu


                selectedListElement = -1 //usunięcie z pamięci indeksu starego zaznaczenia
                productsListView.clearChoices()
            }
        }


        //------------------------ |      Zapisywanie do JSON     |------------------------------------------------------------------------------------------------------------

        binding.saveBtn.setOnClickListener {
            try {
                val json = Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true // ładne formatowanie JSON
                }
                val jsonString = json.encodeToString(inventoryList)

                openFileOutput("pantry.json", MODE_PRIVATE).use {
                    it.write(jsonString.toByteArray())
                }

            } catch (e: Exception) {
                Toast.makeText(this, "File save error!", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }






        // -------------------|     Filtrowanie po kategori z użyciem spinnera     |-----------------------------------------------------------------------------------------------------------------
        val categorySpinner = binding.categoryFilterSpinner

        val categoriesForSpinner = listOf("All")+inventoryList.map { it.Category }.distinct() // pobranie wszystkich kategori z listy produktów
        Log.i("Kategorie", categoriesForSpinner.toString())


        val categoriesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoriesForSpinner)
        categoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        categorySpinner.adapter = categoriesAdapter



        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                p1: View?,
                position: Int,
                p3: Long
            ) {
                selectedCategory = parent?.getItemAtPosition(position).toString()
                filterProducts()

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //nothing
            }

        }


//--------------------------|      Filtrowanie produktów po nazwie z EditText     |----------------------------------------------------------------------------------------------------------
        binding.productNameFilterEditText.addTextChangedListener {

            filterText = it.toString()
            filterProducts()
        }


    }
}