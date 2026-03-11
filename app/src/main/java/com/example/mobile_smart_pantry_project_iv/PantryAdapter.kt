package com.example.mobile_smart_pantry_project_iv

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.mobile_smart_pantry_project_iv.model.Product

class PantryAdapter (private val context: Context,
                    private val products: List<Product>
    ): ArrayAdapter<Product>(context, 0, products) {

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val itemView = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.item_product, parent, false
        )

        val product = products[position]

        val resId = context.resources.getIdentifier(
            product.ImageRef,
            "drawable",
            context.packageName
        )


        val nameTextView = itemView.findViewById<TextView>(R.id.productNameTextView)
        val categoryTextView = itemView.findViewById<TextView>(R.id.productCategoryTextView)
        val quantityTextView = itemView.findViewById<TextView>(R.id.productQuantityTextView)
        val productImageView = itemView.findViewById<ImageView>(R.id.productImageView)

        nameTextView.text = product.Name
        categoryTextView.text = product.Category
        quantityTextView.text = "Quantity: ${product.Quantity}"

        if (resId !=0) productImageView.setImageResource(resId) else productImageView.setImageResource(R.drawable.error)

        if (product.Quantity<=5){
            Log.e("ProductData", product.Name)
            Log.e("ProductData", product.Quantity.toString())
            itemView.findViewById<LinearLayout>(R.id.backgroudLinearLayout).isEnabled = false
        }
        else{
            itemView.findViewById<LinearLayout>(R.id.backgroudLinearLayout).isEnabled = true
        }
        val incBtn = itemView.findViewById<ImageButton>(R.id.incBtn)
        val decrBtn = itemView.findViewById<ImageButton>(R.id.decrBtn)

        incBtn.setOnClickListener {
            product.Quantity++;
            notifyDataSetChanged()
        }

        decrBtn.setOnClickListener {
            if (product.Quantity > 0) {
                product.Quantity--;
                notifyDataSetChanged()
            }
        }

        return itemView
    }
}