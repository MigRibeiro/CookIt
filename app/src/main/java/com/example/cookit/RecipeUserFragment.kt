package com.example.cookit

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.example.cookit.adapters.AdapterRecipeUser
import com.example.cookit.databinding.FragmentRecipeUserBinding
import com.example.cookit.models.ModelRecipe
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

class RecipeUserFragment : Fragment {

    private lateinit var binding: FragmentRecipeUserBinding

    companion object {
        fun newInstance(categoryId: String, category: String, uid: String): RecipeUserFragment {
            val fragment = RecipeUserFragment()
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""

    private lateinit var recipeArrayList: ArrayList<ModelRecipe>
    private lateinit var adapterRecipeUser: AdapterRecipeUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments
        if (args != null) {
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecipeUserBinding.inflate(inflater, container, false)

        if (category == "Todos") {
            loadAllRecipes()
        } else {
            loadCategorizedRecipes()
        }

        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterRecipeUser.filter.filter(s)
                } catch (e: Exception) {
                    // Handle exception
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        return binding.root
    }

    private fun loadAllRecipes() {
        recipeArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Recipes")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                recipeArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelRecipe::class.java)
                    model?.let {
                        val ingredientsString = ds.child("ingredients").getValue(String::class.java)
                        it.ingredients = ingredientsString ?: ""
                        recipeArrayList.add(it)
                    }
                }
                adapterRecipeUser = AdapterRecipeUser(context!!, recipeArrayList)
                binding.recipeRv.adapter = adapterRecipeUser
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle cancellation error
            }
        })
    }

    private fun loadCategorizedRecipes() {
        recipeArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Recipes")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    recipeArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(ModelRecipe::class.java)
                        model?.let {
                            val ingredientsString = ds.child("ingredients").getValue(String::class.java)
                            it.ingredients = ingredientsString ?: ""
                            recipeArrayList.add(it)
                        }
                    }
                    adapterRecipeUser = AdapterRecipeUser(context!!, recipeArrayList)
                    binding.recipeRv.adapter = adapterRecipeUser
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle cancellation error
                }
            })
    }

}
