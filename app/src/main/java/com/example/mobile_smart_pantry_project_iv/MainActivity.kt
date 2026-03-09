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
import com.example.mobile_smart_pantry_project_iv.databinding.ActivityMainBinding
import com.example.mobile_smart_pantry_project_iv.model.Product
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private var inventoryList = mutableListOf<Product>()

    private var selectedListElement = -1


    private fun dataParser() {
        try {
            val jsonString = try {
                openFileInput("pantry.json").bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                resources.openRawResource(R.raw.pantry)
                    .bufferedReader()
                    .use { it.readText() }
            }

            val json = Json { ignoreUnknownKeys = true }
            val loadedList = json.decodeFromString<List<Product>>(jsonString)

            inventoryList.clear()
            inventoryList.addAll(loadedList)

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
//------------------------------------------------------------------------------------------------------------------------------------


//---------------------------------------|     Przypisanie produktów do listy z użyciem adaptera     |---------------------------------------------------------------------------------------------
        val productsListView : ListView = binding.productListView
        val productsAdapter = PantryAdapter(this, inventoryList)

        productsListView.adapter = productsAdapter
        productsListView.setOnItemClickListener { _, _, position, _ ->
            productsListView.setItemChecked(position, true)
            selectedListElement = position
        }


//------------------------------------------------------------------------------------------------------------------------------------


// -------------------|     Filtrowanie po kategori z użyciem spinnera     |-----------------------------------------------------------------------------------------------------------------
        val categorySpinner = binding.categoryFilterSpinner

        val categoriesForSpinner = listOf("All")+inventoryList.map { it.Category }.distinct() // pobranie wszystkich kategori z listy produktów
        Log.i("Kategorie", categoriesForSpinner.toString())


        val categoriesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoriesForSpinner)
        categoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        categorySpinner.adapter = categoriesAdapter


        var filteredProducts = inventoryList.map { it } // var aby można było jednocześnie filtrować po kategorii i nazwie

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                p1: View?,
                position: Int,
                p3: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()

                if (selectedItem == "All") {
                    filteredProducts = inventoryList.map { it }
                    productsListView.adapter = PantryAdapter(this@MainActivity, filteredProducts)
                    return
                }

                filteredProducts = inventoryList.filter {it.Category == selectedItem}

                productsListView.adapter = PantryAdapter(this@MainActivity, filteredProducts)


            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //nothing
            }

        }

//------------------------ |      Usuwanie elementów z listy      |------------------------------------------------------------------------------------------------------------

        binding.deleteProductButton.setOnClickListener{
            if (selectedListElement == -1)
            {
                Toast.makeText(this, "Najpierw zaznacz element", Toast.LENGTH_SHORT).show()
            }
            else{
                inventoryList.removeAt(selectedListElement)
                productsListView.adapter = PantryAdapter (this,inventoryList)
                selectedListElement = -1
                productsListView.clearChoices()
            }
        }

        binding.saveBtn.setOnClickListener {
            try {
                val json = Json { ignoreUnknownKeys = true }
                val jsonString = json.encodeToString(inventoryList)

                openFileOutput("pantry.json", MODE_PRIVATE).use {
                    it.write(jsonString.toByteArray())
                }

            } catch (e: Exception) {
                Toast.makeText(this, "File save error!", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }



//--------------------------|      Filtrowanie produktów po nazwie z EditText     |----------------------------------------------------------------------------------------------------------
        binding.productNameFilterEditText.setOnKeyListener { view, i, event ->

            val filteringText = binding.productNameFilterEditText.text.toString()

            filteredProducts = inventoryList.filter {
                it.Name.contains(filteringText, ignoreCase = true) //ciągłe sprawdzanie czy nazwa zawiera podany ciąg string
            }

            productsListView.adapter = PantryAdapter(this, filteredProducts)

            false
        }


    }
}