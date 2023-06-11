package com.example.cookit.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.cookit.filters.FilterCategory
import com.example.cookit.models.ModelCategory
import com.example.cookit.activities.RecipeListAdminActivity
import com.example.cookit.databinding.RowCategoryBinding
import com.google.firebase.database.FirebaseDatabase

class AdapterCategory :RecyclerView.Adapter<AdapterCategory.HolderCategory>,  Filterable{

    private val context: Context
    public var categoryArrayList: ArrayList<ModelCategory>
    private var filterlist: ArrayList<ModelCategory>

    private var filter : FilterCategory? = null

    private lateinit var binding: RowCategoryBinding

    constructor(context: Context, categoryArrayList: ArrayList<ModelCategory>){

        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterlist = categoryArrayList

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderCategory(binding.root)
    }


    override fun onBindViewHolder(holder: HolderCategory, position: Int) {

        val model = categoryArrayList[position]
        val id = model.id
        val category = model.category
        val uid = model.uid
        val timestamp = model.timestamp

        holder.categoryTv.text = category

        holder.deleteBtn.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Apagar")
                .setMessage("Tem a certeza qeu quer apagar esta categoria?")
                .setPositiveButton("Confirmar"){a, d->
                    Toast.makeText(context, "A apagar...", Toast.LENGTH_SHORT).show()
                    deleteCategory(model, holder)
                }
                .setNegativeButton("cancelar"){a, d ->
                    a.dismiss()
                }
                .show()
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, RecipeListAdminActivity::class.java)
            intent.putExtra("categoryId", id)
            intent.putExtra("category", category)
            context.startActivity(intent)
        }
    }

    private fun deleteCategory(model: ModelCategory, holder: HolderCategory) {
        val id = model.id

        val ref  = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Apagado...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Toast.makeText(context, "Não foi possível apagar devido a ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }


    inner class HolderCategory(itemView: View): RecyclerView.ViewHolder(itemView){

        var categoryTv:TextView = binding.categoryTv
        var deleteBtn:ImageButton = binding.deleteBtn

    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterCategory(filterlist, this)

        }
        return filter as FilterCategory
    }


}