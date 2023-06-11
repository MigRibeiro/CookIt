package com.example.cookit.filters

import android.widget.Filter
import com.example.cookit.adapters.AdapterRecipeUser
import com.example.cookit.models.ModelRecipe

class FilterRecipeUser: Filter{

    var filterList: ArrayList<ModelRecipe>

    var adapterRecipeUser: AdapterRecipeUser


    constructor(filterList: ArrayList<ModelRecipe>, adapterRecipeUser: AdapterRecipeUser) {
        this.filterList = filterList
        this.adapterRecipeUser = adapterRecipeUser
    }


    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint
        val results = FilterResults()

        if(constraint != null && constraint.isNotEmpty()){

            constraint = constraint.toString().lowercase()
            var filteredModels = ArrayList<ModelRecipe>()
            for (i in filterList.indices){

                if(filterList[i].title.lowercase().contains(constraint)){
                    filteredModels.add(filterList[i])

                }

            }
            results.count = filteredModels.size
            results.values = filteredModels
        }
        else{
            results.count = filterList.size
            results.values = filterList
        }

        return results
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {

        adapterRecipeUser.recipeArrayList = results.values as ArrayList<ModelRecipe>

        adapterRecipeUser.notifyDataSetChanged()


    }
}