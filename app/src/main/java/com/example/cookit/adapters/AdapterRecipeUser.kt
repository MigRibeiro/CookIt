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
import com.example.cookit.filters.FilterRecipeUser
import com.example.cookit.MyApplication
import com.example.cookit.activities.RecipeDetailActivity
import com.example.cookit.activities.RecipeEditActivity
import com.example.cookit.databinding.RowRecipeUserBinding
import com.example.cookit.models.ModelRecipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterRecipeUser:RecyclerView.Adapter<AdapterRecipeUser.HolderRecipeUser>, Filterable{

    private var context: Context
    public var recipeArrayList: ArrayList<ModelRecipe>
    private val filterList: ArrayList<ModelRecipe>

    private lateinit var binding: RowRecipeUserBinding

    private var filter: FilterRecipeUser? = null

    constructor(context: Context, recipeArrayList: ArrayList<ModelRecipe>) : super() {
        this.context = context
        this.recipeArrayList = recipeArrayList
        this.filterList = recipeArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderRecipeUser {

        binding = RowRecipeUserBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderRecipeUser(binding.root)
    }

    override fun onBindViewHolder(holder: HolderRecipeUser, position: Int) {

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

        val ref = FirebaseDatabase.getInstance().getReference("Recipes")
        ref.child(recipeId).child("Ratings")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var totalRating = 0.0
                        var ratingCount = 0

                        for (childSnapshot in snapshot.children) {
                            val rating = childSnapshot.getValue(Float::class.java)
                            if (rating != null) {
                                totalRating += rating
                                ratingCount++
                            }
                        }

                        val overallRating = if (ratingCount > 0) {
                            totalRating / ratingCount
                        } else {
                            totalRating // Display the single rating value
                        }

                        holder.rating.text = String.format("%.1f", overallRating)
                    } else {
                        holder.rating.text = "0.0"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    holder.rating.text = "N/A"
                }
            })





    }


    private fun moreOptionsDialog(model: ModelRecipe, holder: HolderRecipeUser) {
        val recipeId = model.id
        val recipeUrl = model.url
        val recipeTitle = model.title
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid // Get the current user's UID

        val options = if (model.uid == currentUserUid) {
            arrayOf("Editar", "Apagar") // Show edit and delete options if the current user is the recipe owner
        } else {
            arrayOf("Ver detalhes") // Show view details option if the current user is not the recipe owner
        }

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Escolha uma opção")
            .setItems(options) { dialog, position ->
                if (position == 0 && model.uid == currentUserUid) { // Check if the current user is the recipe owner
                    val intent = Intent(context, RecipeEditActivity::class.java)
                    intent.putExtra("recipeId", recipeId)
                    context.startActivity(intent)
                } else if (position == 1 && model.uid == currentUserUid) { // Check if the current user is the recipe owner
                    AlertDialog.Builder(context)
                        .setTitle("Confirmar eliminação")
                        .setMessage("Tem a certeza que quer eliminar esta receita?")
                        .setPositiveButton("Sim") { _, _ ->
                            MyApplication.deleteRecipe(context, recipeId, recipeUrl, recipeTitle)
                        }
                        .setNegativeButton("Não", null)
                        .show()
                } else { // The current user is not the recipe owner
                    val intent = Intent(context, RecipeDetailActivity::class.java)
                    intent.putExtra("RecipeId", recipeId) // Used to retrieve recipe information
                    context.startActivity(intent)
                }
            }
            .show()
    }



    override fun getItemCount(): Int {
        return recipeArrayList.size
    }


    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterRecipeUser(filterList, this)
        }

        return filter as FilterRecipeUser
    }

    inner class HolderRecipeUser(itemView: View) : RecyclerView.ViewHolder(itemView){

        //vir aqui por mais coisas no viewholder caso seja necessario mais tarde

        val recipeView = binding.recipeView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val moreBtn = binding.moreBtn
        val rating = binding.avaliacao

    }
}