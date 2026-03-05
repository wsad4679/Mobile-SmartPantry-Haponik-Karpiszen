package com.example.mobile_smart_pantry_project_iv

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mobile_smart_pantry_project_iv.databinding.ActivityMainBinding
import com.example.mobile_smart_pantry_project_iv.model.Product
import kotlinx.serialization.json.Json
import java.io.File

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private val inventoryList = mutableListOf<Product>()


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


//-----------------------|     Parsowanie danych z json do listy     |-------------------------------------------------------------------------------------------------------
        fun dataParser() {
            try {
                val inputStream = resources.openRawResource(R.raw.pantry)
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                val json = Json { ignoreUnknownKeys = true }
                val loadedList = json.decodeFromString<List<Product>>(jsonString)

                inventoryList.clear()
                inventoryList.addAll(loadedList)

            } catch (e: java.lang.Exception) {
                Toast.makeText(
                    this,
                    "Błąd odczytu pliku!",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        }

        dataParser()
        Log.v("inventoryList", inventoryList.toString())
//------------------------------------------------------------------------------------------------------------------------------------



        val productsListView : ListView = binding.productListView
        val productsAdapter = PantryAdapter(this, inventoryList)

        productsListView.adapter = productsAdapter
        productsListView.setOnItemClickListener { _, _, position, _ ->
            productsListView.setItemChecked(position, true)
        }

        val incBtn = binding.increaseProductButton
        val decrBtn = binding.decreaseProductButton

        incBtn.setOnClickListener {
            val checkedProduct = productsListView.checkedItemPosition
            Log.i("Debug", checkedProduct.toString())
        }

        decrBtn.setOnClickListener {

        }


        val categorySpinner = binding.categoryFilterSpinner

        val categoriesForSpinner = listOf("All")+inventoryList.map { it.Category }.distinct() // pobranie wszystkich kategori z listy produktów
        Log.i("Kategorie", categoriesForSpinner.toString())


        val categoriesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoriesForSpinner)
        categoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        categorySpinner.adapter = categoriesAdapter

        val filteredProducts = inventoryList.map {it}
        Log.i("Produkty", filteredProducts.toString())

        // TODO naprawić filtrowanie po kategorii
//        categorySpinner.setOnClickListener {
//            if(categorySpinner.selectedItem.toString() != "All")
//            {
//                filteredProducts.filter { it.Category == categorySpinner.selectedItem.toString() }
//                productsListView.adapter = PantryAdapter(this, filteredProducts)
//            }
//            else{
//                productsListView.adapter = PantryAdapter(this, filteredProducts)
//            }
//
//        }





        binding.productNameFilterEditText.setOnKeyListener { view, i, event ->

            val filteringText = binding.productNameFilterEditText.text.toString()

            val filteredProducts = inventoryList.filter {
                it.Name.contains(filteringText, ignoreCase = true)
            }

            productsListView.adapter = PantryAdapter(this, filteredProducts)

            false
        }
    }
}