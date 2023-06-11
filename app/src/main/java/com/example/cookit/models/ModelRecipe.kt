package com.example.cookit.models

class ModelRecipe {

    var uid:String = ""
    var id: String = ""
    var title: String = ""
    var description: String = ""
    var categoryId: String = ""
    var ingredients: String = ""
    var time: String = ""
    var equipment: String = ""
    var steps: String = ""
    var url: String = ""
    var youtube: String = ""
    var timestamp: Long = 0
    var viewscount: Long = 0
    var isFavorite = false

    constructor()


    constructor(
        uid: String,
        id: String,
        title: String,
        description: String,
        categoryId: String,
        ingredients: String,
        time: String,
        equipment: String,
        steps: String,
        url: String,
        youtube: String,
        timestamp: Long,
        viewscount: Long,
        isFavorite: Boolean
    ) {
        this.uid = uid
        this.id = id
        this.title = title
        this.description = description
        this.categoryId = categoryId
        this.ingredients = ingredients
        this.time = time
        this.equipment = equipment
        this.steps = steps
        this.url = url
        this.youtube = youtube
        this.timestamp = timestamp
        this.viewscount = viewscount
        this.isFavorite = isFavorite
    }


}