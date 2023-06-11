package com.example.cookit.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cookit.MyApplication
import com.example.cookit.activities.RecipeDetailActivity
import com.example.cookit.databinding.RowRecipeFavoriteBinding
import com.example.cookit.models.ModelRecipe
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterRecipeFavorite: RecyclerView.Adapter<AdapterRecipeFavorite.HolderRecipeFavorite> {

    private val context: Context

    private var recipesArrayList: ArrayList<ModelRecipe>

    private lateinit var binding: RowRecipeFavoriteBinding

    constructor(context: Context, recipesArrayList: ArrayList<ModelRecipe>){

        this.context = context
        this.recipesArrayList = recipesArrayList

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderRecipeFavorite {

        binding = RowRecipeFavoriteBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderRecipeFavorite(binding.root)
    }

    override fun onBindViewHolder(holder: HolderRecipeFavorite, position: Int) {

        val model = recipesArrayList[position]

        loadRecipeDetails(model, holder)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, RecipeDetailActivity::class.java)
            intent.putExtra("recipeId", model.id)
            context.startActivity(intent)
        }

        holder.removeFavBtn.setOnClickListener {
            MyApplication.removeFromFavorites(context, model.id)

        }

    }

    private fun loadRecipeDetails(model: ModelRecipe, holder: AdapterRecipeFavorite.HolderRecipeFavorite) {

        val recipeId = model.id

        val ref = FirebaseDatabase.getInstance().getReference("Recipes")
        ref.child(recipeId)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val equipment = "${snapshot.child("equipment").value}"
                    val id = "${snapshot.child("id").value}"
                    val ingredients = "${snapshot.child("ingredients").value}"
                    val steps = "${snapshot.child("steps").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewcount = "${snapshot.child("viewcount").value}"

                    model.isFavorite = true
                    model.title = title
                    model.description = description
                    model.categoryId = categoryId
                    model.uid = uid
                    model.url = url

                    MyApplication.loadCategory(categoryId, binding.categoryTv)
                    MyApplication.loadImageFromUrl(url, context, holder.recipeView, holder.progressBar)

                    holder.titleTv.text = title
                    holder.descriptionTv.text = description

                    //FALTA IMAGEM COM O GLIDER
                    Glide.with(context)
                        .load(url)
                        .into(holder.recipeView)

                    holder.itemView.setOnClickListener {
                        val intent = Intent(context, RecipeDetailActivity::class.java)
                        intent.putExtra("RecipeId", recipeId) //Usado para ir buscar as informações da receita
                        context.startActivity(intent)

                    }
                }


                override fun onCancelled(error: DatabaseError) {

                }

            })
    }


    override fun getItemCount(): Int {
        return recipesArrayList.size
    }


    inner class HolderRecipeFavorite(itemView: View) : RecyclerView.ViewHolder(itemView){

        var recipeView = binding.recipeView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var removeFavBtn = binding.removeFavBtn
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv

    }


}