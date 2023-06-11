package com.example.cookit.activities

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.cookit.MyApplication
import com.example.cookit.R
import com.example.cookit.adapters.AdapterComment
import com.example.cookit.databinding.ActivityRecipeDetailBinding
import com.example.cookit.databinding.DialogCommentAddBinding
import com.example.cookit.models.ModelComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var binding:ActivityRecipeDetailBinding

    private var recipeId = ""

    private lateinit var context: Context

    private lateinit var firebaseAuth:FirebaseAuth

    private var isInMyFavorite = false

    private lateinit var progressDialog: ProgressDialog

    private lateinit var commentArrayList: ArrayList<ModelComment>

    private lateinit var adapterComment: AdapterComment

    private lateinit var ratingBar: RatingBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)




        recipeId = intent.getStringExtra("RecipeId")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor aguarde...")
        progressDialog.setCanceledOnTouchOutside(false)



        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        loadRecipeDetails()
        showComments()


        binding.favoriteBtn.setOnClickListener {
            if(firebaseAuth.currentUser == null){

                Toast.makeText(this, "Não entrou em nenhuma conta", Toast.LENGTH_SHORT).show()
            }
            else{

                if(isInMyFavorite){

                    MyApplication.removeFromFavorites(this, recipeId)
                }
                else{
                    addtoFavorites()
                }
            }
        }

        firebaseAuth = FirebaseAuth.getInstance()
        if(firebaseAuth.currentUser != null){
            checkIsFavorite()
        }

        ratingBar = findViewById(R.id.ratingBar)

        loadUserRating()

        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->

            savedUserRating(rating)
        }

        binding.addCommentBtn.setOnClickListener {

            if(firebaseAuth.currentUser == null){

                Toast.makeText(this, "Utilizador sem conta associada", Toast.LENGTH_SHORT).show()

            }
            else{

                addCommentDialog()


            }

        }

    }

    private fun savedUserRating(rating: Float) {
        val ref = FirebaseDatabase.getInstance().getReference("Recipes")
        ref.child(recipeId).child("Ratings").child(firebaseAuth.uid!!).setValue(rating)
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save rating: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserRating() {
        val ref = FirebaseDatabase.getInstance().getReference("Recipes")
        ref.child(recipeId).child("Ratings").child(firebaseAuth.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userRating = snapshot.getValue(Float::class.java)
                        ratingBar.rating = userRating ?: 0f
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled if needed
                }
            })
    }

    private fun showComments() {

        commentArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Recipes")
        ref.child(recipeId).child("Comments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    commentArrayList.clear()
                    for(ds in snapshot.children){

                        val model = ds.getValue(ModelComment::class.java)

                        commentArrayList.add(model!!)

                    }

                    adapterComment = AdapterComment(this@RecipeDetailActivity, commentArrayList)

                    binding.commentsRv.adapter = adapterComment

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })


    }

    private var comment = ""

    private fun addCommentDialog() {

        val commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this))

        val builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setView(commentAddBinding.root)

        val alertDialog = builder.create()
        alertDialog.show()

        commentAddBinding.backBtn.setOnClickListener { alertDialog.dismiss() }

        commentAddBinding.submitBtn.setOnClickListener {

            comment = commentAddBinding.commentEt.text.toString().trim()

            if(comment.isEmpty()){
                Toast.makeText(this, "Introduza um comentário...", Toast.LENGTH_SHORT).show()
            }
            else{
                alertDialog.dismiss()
                addComment()

            }
        }

    }

    private fun addComment() {

        progressDialog.setMessage("A adicionar comentário")
        progressDialog.show()

        val timestamp = "${System.currentTimeMillis()}"

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["recipeId"] = "$recipeId"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${firebaseAuth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Recipes")
        ref.child(recipeId).child("Comments").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {

                progressDialog.dismiss()
                Toast.makeText(this, "Comentário adicionado", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Falha ao adicionar comentário devido a ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun loadRecipeDetails() {

        val ref = FirebaseDatabase.getInstance().getReference("Recipes")
        ref.child(recipeId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val time = "${snapshot.child("time").value}"
                    val equipment = "${snapshot.child("equipment").value}"
                    val id = "${snapshot.child("id").value}"
                    val ingredientsString = "${snapshot.child("ingredients").value}"
                    val ingredientsList = ingredientsString.split(",")
                    val steps = "${snapshot.child("steps").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val youtubeLink = "${snapshot.child("youtube").value}"
                    val viewcount = "${snapshot.child("viewcount").value}"

                    binding.ingredientsContainer.removeAllViews()

                    MyApplication.loadCategory(categoryId, binding.categoryTv)

                    Glide.with(this@RecipeDetailActivity)
                        .load(url)
                        .into(binding.imageMealDetail)


                    binding.passosTv.text = steps
                    binding.ingredientsContainer.removeAllViews()
                    binding.tempoTv.text = time
                    binding.equipamentoTv.text = equipment


                    for (ingredient in ingredientsList) {
                        val checkBox = CheckBox(this@RecipeDetailActivity)
                        checkBox.text = ingredient
                        binding.ingredientsContainer.addView(checkBox)

                        // Retrieve and set the checkbox state for the current user
                        val uid = firebaseAuth.currentUser?.uid
                        if (uid != null) {
                            val userRef = FirebaseDatabase.getInstance().getReference("Users")
                            userRef.child(uid).child("IngredientCheckStates").child(recipeId).child(ingredient)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(checkSnapshot: DataSnapshot) {
                                        val isChecked = checkSnapshot.getValue(Boolean::class.java) ?: false
                                        checkBox.isChecked = isChecked
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        // Handle onCancelled if needed
                                    }
                                })
                        }

                        // Save the checkbox state when it is clicked
                        checkBox.setOnCheckedChangeListener { _, isChecked ->
                            val uid = firebaseAuth.currentUser?.uid
                            if (uid != null) {
                                val userRef = FirebaseDatabase.getInstance().getReference("Users")
                                userRef.child(uid).child("IngredientCheckStates").child(recipeId).child(ingredient)
                                    .setValue(isChecked)
                                    .addOnSuccessListener {
                                        // Checkbox state saved successfully
                                    }
                                    .addOnFailureListener { e ->
                                        // Handle failure to save checkbox state
                                    }
                            }
                        }
                    }


                    binding.youtubeBtn.setOnClickListener {
                        openYouTubeLink(youtubeLink)
                    }
                    binding.collapsingToolBar.title = title

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun openYouTubeLink(youtubeLink: String) {
        if (youtubeLink.isNotEmpty()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeLink))
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "No app available to play the video",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(this, "YouTube link not found", Toast.LENGTH_SHORT).show()
        }
    }




    private fun addtoFavorites(){
        val timestamp = System.currentTimeMillis()

        val hashMap = HashMap<String, Any>()
        hashMap["recipeId"] = recipeId
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(recipeId)
            .setValue(hashMap)
            .addOnSuccessListener {

                Toast.makeText(this, "Adicionado aos favoritos", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->

                Toast.makeText(this, "Falha a adicionar aos favoritos devido a ${e.message}", Toast.LENGTH_SHORT).show()

            }


    }



    private fun checkIsFavorite(){

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(recipeId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                   isInMyFavorite = snapshot.exists()
                    if(isInMyFavorite){
                        val favoriteIcon = ContextCompat.getDrawable(this@RecipeDetailActivity,
                            R.drawable.ic_filled_favorite
                        )
                        binding.favoriteBtn.setImageDrawable(favoriteIcon)
                    }
                    else{

                        val favoriteIcon = ContextCompat.getDrawable(this@RecipeDetailActivity,
                            R.drawable.ic_favorite
                        )
                        binding.favoriteBtn.setImageDrawable(favoriteIcon)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }


            })

    }
}