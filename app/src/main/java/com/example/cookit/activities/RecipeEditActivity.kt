package com.example.cookit.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.cookit.databinding.ActivityRecipeEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class RecipeEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeEditBinding

    private var recipeId = ""

    private lateinit var progressDialog: ProgressDialog

    private lateinit var categoryTitleArrayList:ArrayList<String>

    private lateinit var categoryIdArrayList:ArrayList<String>

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeEditBinding.inflate(layoutInflater)
        setContentView(binding.root)


        recipeId = intent.getStringExtra("recipeId")!!



        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor aguarde")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadRecipeInfo()



        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.imagepick.setOnClickListener {
            imagePickIntent()
        }

        binding.categoryTv.setOnClickListener {
            categoryDialog()
        }

        binding.editBtn.setOnClickListener {
            validateData()
        }

    }

    private fun loadRecipeInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Recipes")
        ref.child(recipeId)
            .addListenerForSingleValueEvent(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()
                    val ingredients = snapshot.child("ingredients").value.toString()
                    val time = snapshot.child("time").value.toString()
                    val equipment = snapshot.child("equipment").value.toString()
                    val steps = snapshot.child("steps").value.toString()


                    binding.descriptionEt.setText(description)
                    binding.titleEt.setText(title)
                    binding.ingredientsEt.setText(ingredients)
                    binding.cookingtimeEt.setText(time)
                    binding.equipamentoEt.setText(equipment)
                    binding.passosreceitaEt.setText(steps)

                    val refRecipeCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refRecipeCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val category = snapshot.child("category").value

                                binding.categoryTv.text  = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })

                }

                override fun onCancelled(error: DatabaseError) {

                }


            })
    }

    private fun imagePickIntent() {
        Log.d(TAG, "imagePickIntent: Starting image picker intent")

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imageActivityResultLauncher.launch(intent)
    }

    val imageActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{ result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "Image picked")
                val imageUri = result.data?.data
                // Set the value of imageUri
                this.imageUri = imageUri
                // Load the image into an ImageView
                binding.imageIv.setImageURI(imageUri)
                Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "Image pick cancelled")
                Toast.makeText(this, "Image pick cancelled", Toast.LENGTH_SHORT).show()
            }
        })


    private var title = ""
    private var description = ""
    private var ingredients = ""
    private var time = ""
    private var equipment = ""
    private var steps = ""

    private fun validateData() {
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        ingredients = binding.ingredientsEt.text.toString().trim()
        time = binding.cookingtimeEt.text.toString().trim()
        selectedCategoryId = binding.categoryTv.text.toString().trim()
        equipment = binding.equipamentoEt.text.toString().trim()
        steps = binding.passosreceitaEt.text.toString().trim()


        if(title.isEmpty()){
            Toast.makeText(this, "Introduza o nome da receita", Toast.LENGTH_SHORT).show()
        }
        else if(description.isEmpty()){
            Toast.makeText(this, "Introduza a descrição da receita", Toast.LENGTH_SHORT).show()
        }
        else if(selectedCategoryId.isEmpty()){
            Toast.makeText(this, "Introduza uma categoria", Toast.LENGTH_SHORT).show()
        }
        else if(ingredients.isEmpty()){
            Toast.makeText(this, "Introduza os ingredientes da receita", Toast.LENGTH_SHORT).show()
        }
        else if(time.isEmpty()){
            Toast.makeText(this, "Introduza o tempo para cozinhar a receita", Toast.LENGTH_SHORT).show()
        }
        else if(equipment.isEmpty()){
            Toast.makeText(this, "Introduza o equipamento necessário", Toast.LENGTH_SHORT).show()
        }
        else if(steps.isEmpty()){
            Toast.makeText(this, "Introduza os passos necessários", Toast.LENGTH_SHORT).show()
        }
        else{
            updateRecipe()
        }
    }

    private fun updateRecipe() {

        progressDialog.setMessage("A fazer alterações...")
        progressDialog.show()

        val hashMap = HashMap<String, Any>()
        hashMap["title"] = title
        hashMap["description"] = description
        hashMap["category"] = selectedCategoryId
        hashMap["ingredients"] = ingredients
        hashMap["time"] = time
        hashMap["equipment"] = equipment
        hashMap["steps"] = steps

        val ref = FirebaseDatabase.getInstance().getReference("Recipes").child(recipeId)
        ref.updateChildren(hashMap)
            .addOnSuccessListener {

                if (imageUri != null) {
                    val storageRef = FirebaseStorage.getInstance().getReference().child("Recipes").child(recipeId)
                    storageRef.putFile(imageUri!!)
                        .addOnSuccessListener { taskSnapshot ->
                            Log.d(TAG, "Image uploaded successfully: ${taskSnapshot.metadata?.path}")
                            storageRef.downloadUrl.addOnSuccessListener { uri ->
                                val imageUrl = uri.toString()
                                ref.child("url").setValue(imageUrl)
                                    .addOnSuccessListener {
                                        progressDialog.dismiss()
                                        Toast.makeText(this, "Alterações feitas com sucesso...", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        progressDialog.dismiss()
                                        Log.e(TAG, "Failed to update image URL: ${e.message}")
                                        Toast.makeText(this, "Falha a fazer as alterações devido a ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Log.e(TAG, "Failed to upload image: ${e.message}")
                            Toast.makeText(this, "Falha a fazer as alterações devido a ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Alterações feitas com sucesso...", Toast.LENGTH_SHORT).show()
                }

            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Falha a fazer as alterações devido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryDialog() {

        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for(i in categoryTitleArrayList.indices){
            categoriesArray[i] = categoryTitleArrayList[i]
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Escolha uma categoria")
            .setItems(categoriesArray){ dialog, position ->

                selectedCategoryId = categoryIdArrayList[position]
                selectedCategoryTitle = categoryTitleArrayList[position]

                binding.categoryTv.text = selectedCategoryTitle

            }
            .show()

    }

    private fun loadCategories() {

        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()

                for(ds in snapshot.children){
                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}