package com.example.mobile_smart_pantry_project_iv

import android.os.Bundle
import android.util.Log
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

        val productsListView : ListView = binding.productListView
        val adapter = PantryAdapter(this, inventoryList)

        productsListView.adapter = adapter

        binding.productNameSearchEditText.setOnKeyListener { view, i, event ->

            val filteringText = binding.productNameSearchEditText.text.toString()

            val filteredProducts = inventoryList.filter {
                it.Name.contains(filteringText, ignoreCase = true)
            }

            val filteredAdapter = PantryAdapter(this, filteredProducts)
            productsListView.adapter = filteredAdapter

            false
        }
    }
}