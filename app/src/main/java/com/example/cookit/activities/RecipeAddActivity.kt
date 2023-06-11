package com.example.cookit.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.cookit.databinding.ActivityRecipeAddBinding
import com.example.cookit.models.ModelCategory
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class RecipeAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeAddBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private lateinit var  categoryArrayList: ArrayList<ModelCategory>

    private var imageUri: Uri? = null

    private val TAG = "IMAGE_ADD_TAG"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeAddBinding.inflate(layoutInflater)
        setContentView(binding.root)



        firebaseAuth = FirebaseAuth.getInstance()
        loadRecipeCategories()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor aguarde")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.imagepick.setOnClickListener {
            imagePickIntent()
        }

        binding.categoryTv.setOnClickListener {

            categoryPickDialog()
        }

        binding.submitBtn.setOnClickListener {

            validateData()
        }

    }

    private var title = ""
    private var description = ""
    private var category = ""
    private var ingredients = ""
    private var time = ""
    private var equipment = ""
    private var steps = ""
    private var youtube= ""

    private fun validateData() {
        Log.d(TAG, "validateData: validating data")

        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()
        ingredients = binding.ingredientsEt.text.toString().trim()
        time = binding.cookingtimeEt.text.toString().trim()
        equipment = binding.equipamentoEt.text.toString().trim()
        steps = binding.passosreceitaEt.text.toString().trim()
        youtube = binding.youtubeEt.text.toString().trim()

        if(title.isEmpty()){
            Toast.makeText(this, "Introduza o nome da receita", Toast.LENGTH_SHORT).show()
        }
        else if(description.isEmpty()){
            Toast.makeText(this, "Introduza a descrição da receita", Toast.LENGTH_SHORT).show()
        }
        else if(category.isEmpty()){
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
        else if(imageUri == null){
            Toast.makeText(this, "Introduza uma imagem", Toast.LENGTH_SHORT).show()
        }
        else{
            uploadRecipeToStorage()
        }

    }

    private fun uploadRecipeToStorage() {
        Log.d(TAG, "uploadRecipeToStorage: uploading to storage... ")

        progressDialog.setMessage("A guardar Receita...")
        progressDialog.show()

        val timestamp = System.currentTimeMillis()

        val filePathAndName = "Recipes/$timestamp"

        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(imageUri!!)
            .addOnSuccessListener {taskSnapshot ->
                Log.d(TAG, "uploadImageToStorage: Image uploaded now getting url...")

                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while(!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"

                uploadRecipeInfoToDb(uploadedImageUrl, timestamp)
            }
            .addOnFailureListener{e ->
                Log.d(TAG,"uploadImageToStorage: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Falha a inserir imagem devido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadRecipeInfoToDb(uploadedImageUrl: String, timestamp: Long) {
        Log.d(TAG, "uploadRecipeInfoToDb: Uploading to db")
        progressDialog.setMessage("A guardar informação da receita")

        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectCategoryId"
        hashMap["ingredients"] = "$ingredients"
        hashMap["time"] = "$time"
        hashMap["equipment"] = "$equipment"
        hashMap["steps"] = "$steps"
        hashMap["url"] = "$uploadedImageUrl"
        hashMap["youtube"] = "$youtube"
        hashMap["timestamp"] = timestamp
        hashMap["viewscount"] = 0

        val ref = FirebaseDatabase.getInstance().getReference("Recipes")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG,"uploadRecipeInfoToDb: uploaded to db")
                progressDialog.dismiss()
                Toast.makeText(this, "Inserido... ", Toast.LENGTH_SHORT).show()
                imageUri = null
            }
            .addOnFailureListener {e->
                Log.d(TAG,"uploadRecipeInfoToDb: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Falha a inserir imagem devido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadRecipeCategories() {
        Log.d(TAG, "loadRecipeCategories: A carregar as categorias das receitas")

        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                categoryArrayList.clear()
                for( ds in snapshot.children){

                    val model = ds.getValue(ModelCategory::class.java)

                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private var selectCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryPickDialog(){
        Log.d(TAG, "categoryPickDialog: A mostrar a categoria da receita")

        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for( i in categoryArrayList.indices){
            categoriesArray[i] = categoryArrayList[i].category
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Escolher categoria")
            .setItems(categoriesArray){dialog, which ->

                selectedCategoryTitle = categoryArrayList[which].category
                selectCategoryId = categoryArrayList[which].id

                binding.categoryTv.text = selectedCategoryTitle

                Log.d(TAG, "categoryPickDialog: ID da categoria selecionada: $selectCategoryId")
                Log.d(TAG, "categoryPickDialog: Título da categoria selecionado: $selectedCategoryTitle")


            }
            .show()
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
            Toast.makeText(this, "Imagem escolhida", Toast.LENGTH_SHORT).show()
        } else {
            Log.d(TAG, "Image pick cancelled")
            Toast.makeText(this, "Imagem escolhida cancelada", Toast.LENGTH_SHORT).show()
        }
    })

}