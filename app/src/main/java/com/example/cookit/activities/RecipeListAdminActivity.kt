package com.example.cookit.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.example.cookit.adapters.AdapterRecipeAdmin
import com.example.cookit.databinding.ActivityRecipeListAdminBinding
import com.example.cookit.models.ModelRecipe
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RecipeListAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeListAdminBinding

    private var categoryId = ""
    private var category = ""

    private lateinit var recipeArrayList: ArrayList<ModelRecipe>
    private lateinit var adapterRecipeAdmin: AdapterRecipeAdmin


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeListAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        binding.subTitleTv.text = category

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        loadRecipeList()

        binding.searchEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    try{
                        adapterRecipeAdmin.filter!!.filter(s)
                    }
                    catch (e: Exception){


                    }
            }

            override fun afterTextChanged(p0: Editable?) {

            }



        })

    }

    private fun loadRecipeList(){

        recipeArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Recipes")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    recipeArrayList.clear()
                    for (ds in snapshot.children){

                        val model = ds.getValue(ModelRecipe::class.java)

                        if (model != null) {
                            recipeArrayList.add(model)
                        }
                    }

                    adapterRecipeAdmin = AdapterRecipeAdmin(this@RecipeListAdminActivity, recipeArrayList)
                    binding.recipeRv.adapter = adapterRecipeAdmin
                }

                override fun onCancelled(error: DatabaseError) {

                }



            })


    }
}