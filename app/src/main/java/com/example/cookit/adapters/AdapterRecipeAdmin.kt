package com.example.cookit.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.cookit.filters.FilterRecipeAdmin
import com.example.cookit.MyApplication
import com.example.cookit.activities.RecipeDetailActivity
import com.example.cookit.activities.RecipeEditActivity
import com.example.cookit.databinding.RowRecipeAdminBinding
import com.example.cookit.models.ModelRecipe

class AdapterRecipeAdmin : RecyclerView.Adapter<AdapterRecipeAdmin.HolderRecipeAdmin>, Filterable{

    private var context: Context
    public var recipeArrayList: ArrayList<ModelRecipe>
    private val filterList: ArrayList<ModelRecipe>

    private lateinit var binding: RowRecipeAdminBinding

    private var filter: FilterRecipeAdmin? = null

    constructor(context: Context, recipeArrayList: ArrayList<ModelRecipe>) : super() {
        this.context = context
        this.recipeArrayList = recipeArrayList
        this.filterList = recipeArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderRecipeAdmin {

        binding = RowRecipeAdminBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderRecipeAdmin(binding.root)
    }

    override fun onBindViewHolder(holder: HolderRecipeAdmin, position: Int) {

        val model = recipeArrayList[position]
        val recipeId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val recipeUrl = model.url
        val timestamp = model.timestamp


        holder.titleTv.text = title
        holder.descriptionTv.text = description

        MyApplication.loadCategory(categoryId, holder.categoryTv)

        MyApplication.loadImageFromUrl(recipeUrl, context, holder.recipeView, holder.progressBar)

        holder.moreBtn.setOnClickListener {
            moreOptionsDialog(model, holder)

        }


        holder.itemView.setOnClickListener {
            val intent = Intent(context, RecipeDetailActivity::class.java)
            intent.putExtra("RecipeId", recipeId) //Usado para ir buscar as informações da receita
            context.startActivity(intent)

        }

    }

    private fun moreOptionsDialog(model: ModelRecipe, holder: HolderRecipeAdmin) {
        val recipeId = model.id
        val recipeUrl = model.url
        val recipeTitle = model.title

        val options = arrayOf("Editar", "Apagar")

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Escolha uma opção")
            .setItems(options){ dialog, position ->

                if(position == 0){
                    val intent = Intent(context, RecipeEditActivity::class.java)
                    intent.putExtra("recipeId", recipeId)
                    context.startActivity(intent)
                }
                else if (position == 1){

                    AlertDialog.Builder(context)
                        .setTitle("Confirmar eliminação")
                        .setMessage("Tem a certeza que quer eliminar esta receita?")
                        .setPositiveButton("Sim") { _, _ ->
                            MyApplication.deleteRecipe(context, recipeId, recipeUrl, recipeTitle)
                        }
                        .setNegativeButton("Não", null)
                        .show()
                }

            }
            .show()
    }

    override fun getItemCount(): Int {
        return recipeArrayList.size
    }



    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterRecipeAdmin(filterList, this)
        }

        return filter as FilterRecipeAdmin
    }

    inner class HolderRecipeAdmin(itemView: View) : RecyclerView.ViewHolder(itemView){

        //vir aqui por mais coisas no viewholder caso seja necessario mais tarde

        val recipeView = binding.recipeView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val moreBtn = binding.moreBtn

    }


}