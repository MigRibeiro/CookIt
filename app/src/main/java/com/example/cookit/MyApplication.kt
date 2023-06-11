package com.example.cookit

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata

import java.util.Calendar
import java.util.Locale

class MyApplication: Application() {


    companion object {


        fun loadCategory(categoryId: String, categoryTv: TextView) {

            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val category = "${snapshot.child("category").value}"

                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }


                })

        }


        fun loadImageFromUrl(imageUrl: String, context: Context, imageView: ImageView, progressBar: ProgressBar?) {
            Glide.with(context)
                .load(imageUrl)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        progressBar?.visibility = View.GONE
                        return false
                    }
                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        progressBar?.visibility = View.GONE
                        return false
                    }
                })
                .into(imageView)
        }

        fun formatTimeStamp(timestamp:Long): String{
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp

            return DateFormat.format("dd/MM/yyyy", cal).toString()

        }

         fun removeFromFavorites(context:Context, recipeId: String){

            val firebaseAuth = FirebaseAuth.getInstance();

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(recipeId)
                .removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, "Removido dos favoritos", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {e->
                    Toast.makeText(context, "Falha ao remover dos favoritos devido a ${e.message}", Toast.LENGTH_SHORT).show()
                }

        }



        fun deleteRecipe(context: Context, recipeId: String, recipeUrl: String, recipeTitle: String){


            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Por favor aguarde")
            progressDialog.setMessage("A apagar $recipeTitle...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()


            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(recipeUrl)
            storageReference.delete()
                .addOnSuccessListener {

                    val ref = FirebaseDatabase.getInstance().getReference("Recipes")
                    ref.child(recipeId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Apagado com sucesso...", Toast.LENGTH_SHORT).show()


                        }
                        .addOnFailureListener {e->
                            Toast.makeText(context, "Falha ao apagar da base de dados devido a ${e.message}", Toast.LENGTH_SHORT).show()

                        }

                }

                .addOnFailureListener {e->
                    progressDialog.dismiss()
                    Toast.makeText(context, "Falha ao apagar da storage devido a ${e.message}", Toast.LENGTH_SHORT).show()
                }



        }
    }
}